package com.fishgo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 활동 기록")
@Builder
public class UserRecordsDto {

    @Schema(description = "잡은 물고기 중 가장 큰 생선 정보")
    private MaximumFishDto gold;

    @Schema(description = "잡은 물고기 중 두번째로 큰 생선 정보")
    private MaximumFishDto silver;

    @Schema(description = "잡은 물고기 중 세번째 생선 정보")
    private MaximumFishDto bronze;

    @Schema(description = "총 잡은 마릿 수")
    private long totalCatchCount;

    @Schema(description = "가장 많이 잡은 어종")
    private String topFishType;

    @Schema(description = "총 낚시 횟수")
    private long fishingCount;

    @Schema(description = "가장 많이 방문한 곳")
    private String mostVisitedPlace;
}
