package com.fishgo.posts.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.dto.PinpointDto;
import com.fishgo.posts.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "지도에서 사용 되는 API")
@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final MapService mapService;

    @Operation(summary = "핀포인트 목록 조회", description = "최소 위경도와 최대 위경도로 해당 구간 내의 게시글을 검색합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PinpointDto>>> getPinpoint(@RequestParam Double minLat, @RequestParam Double minLon,
                                                                      @RequestParam Double maxLat, @RequestParam Double maxLon,
                                                                      @Parameter(description = "보여 줄 핀포인트의 개수")
                                                                        @RequestParam(required = false, defaultValue = "20") Integer limit) {

        List<PinpointDto> pinpoints = mapService.getPinpoints(minLat, minLon, maxLat, maxLon, limit);

        return ResponseEntity.ok().body(new ApiResponse<>("핀포인트 조회 성공.", HttpStatus.OK.value(), pinpoints));
    }
}
