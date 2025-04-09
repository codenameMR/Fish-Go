package com.fishgo.common.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PageService {

    public long getRemainingCount(Page<?> page) {

        // 전체 개수
        long totalCount = page.getTotalElements();
        // 현재 페이지에서 조회된 개수
        int currentCount = page.getNumberOfElements();
        // 현재 페이지의 오프셋(몇 개 건너뛰고 조회했는지) → long 타입
        long offset = page.getPageable().getOffset();

        // 지금까지 조회한 수 = offset + currentCount
        long viewed = offset + currentCount;

        // 남은 개수 = totalCount - viewed
        long remainingCount = totalCount - viewed;
        if (remainingCount < 0) {
            remainingCount = 0; // 음수 방지
        }

        return remainingCount;

    }
}
