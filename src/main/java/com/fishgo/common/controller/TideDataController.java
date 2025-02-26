package com.fishgo.common.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.common.service.TideDataFetcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "조석예보 API", description = "캐싱된 조석예보 데이터 GET")
@RestController
@RequestMapping("/tide")
public class TideDataController {

    // Singleton 인스턴스 의존성 주입
    private final TideDataFetcher tideDataFetcher = TideDataFetcher.getInstance();

    @Operation(summary = "조석예보 데이터 조회",
            description = "관측소 별 혹은 전체 관측소 조석예보 데이터를 가져옵니다.")
    @GetMapping("/{obsCode}")
    public ResponseEntity<ApiResponse<?>> getTideData(
            @Parameter(description = "특정 관측소 코드 혹은 문자열 \"ALL\"로 전체 관측소를 가지고 옵니다.",
                    example = "SO_0732", required = true)
            @PathVariable String obsCode
    ) {
        Map<String, Object> data;
        if (obsCode.equals("ALL")){
            data = tideDataFetcher.getAllCachedDataAsMap();
        } else {
            data = tideDataFetcher.getCachedDataAsMap(obsCode);
        }

        if (data == null || data.isEmpty()) {
            // 데이터가 없을 경우 에러 응답
            return ResponseEntity.ok(
                    new ApiResponse<>("데이터가 존재하지 않습니다. ", 404)
            );
        }

        // 성공적으로 데이터 반환
        return ResponseEntity.ok(
                new ApiResponse<>("데이터 반환에 성공했습니다.", 200,data)
        );
    }


}

