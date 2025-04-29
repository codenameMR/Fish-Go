package com.fishgo.posts.respository;

import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PinpointDto;
import com.fishgo.posts.dto.PostStatsDto;
import com.fishgo.users.dto.MaximumFishDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostsRepository extends JpaRepository<Posts, Long> {

    @Query(
            "SELECT p FROM Posts p WHERE " +
                "p.title LIKE %:query% OR " +
                "p.contents LIKE %:query%"
        )
    Page<Posts> searchPosts(@Param("query") String query, Pageable pageable);

    @Query("SELECT NEW com.fishgo.posts.dto.PostStatsDto(" +
            "COUNT(p), COALESCE(SUM(p.likeCount), 0)) " +
            "FROM Posts p WHERE p.users.id = :userId")
    PostStatsDto findPostStatsByUserId(@Param("userId") long userId);

    @Query("SELECT NEW com.fishgo.posts.dto.PinpointDto(" +
            "p.id, p.lat, p.lon) " +
            "FROM Posts p " +
            "WHERE p.lat BETWEEN :minLat AND :maxLat " +
            "  AND p.lon BETWEEN :minLon AND :maxLon")
    Optional<List<PinpointDto>> findPostsInRange(
            @Param("minLat") Double minLat, @Param("minLon") Double minLon,
            @Param("maxLat") Double maxLat, @Param("maxLon") Double maxLon,
            Pageable pageable);

    @Query("SELECT new com.fishgo.users.dto.MaximumFishDto(p.fishType, p.fishSize) "
            + "FROM Posts p "
            + "WHERE p.users.id = :userId "
            + "ORDER BY p.fishSize DESC")
    List<MaximumFishDto> findTop3FishByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p.fishType "
            + "FROM Posts p "
            + "WHERE p.users.id = :userId "
            + "GROUP BY p.fishType "
            + "ORDER BY COUNT(p.fishType) DESC")
    List<String> findTopFishType(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p.location "
            + "FROM Posts p "
            + "WHERE p.users.id = :userId "
            + "GROUP BY p.location "
            + "ORDER BY COUNT(p.location) DESC")
    List<String> findMostVisitedPlace(@Param("userId") Long userId, Pageable pageable);

    long countByUsers_Id(Long userId);

    Page<Posts> findAllByUsers_Id(Long usersId, Pageable pageable);

    @Query("SELECT NEW com.fishgo.posts.dto.PinpointDto(" +
            "p.id, p.lat, p.lon) " +
            "FROM Posts p " +
            "WHERE p.users.id = :userId " +
            "AND p.lat IS NOT NULL " +
            "AND p.lon IS NOT NULL")
    Optional<List<PinpointDto>> findMyPinpoint(@Param("userId") Long userId);

    Long countByUsersId(Long userId);

}
