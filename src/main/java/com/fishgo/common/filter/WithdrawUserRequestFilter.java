package com.fishgo.common.filter;

import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.exception.CustomException;
import com.fishgo.users.domain.UserStatus;
import jakarta.servlet.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class WithdrawUserRequestFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            // `WITHDRAW_REQUEST` 상태 확인
            boolean isWithdrawRequest = auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(UserStatus.WITHDRAW_REQUEST.name()));

            // 상태가 `WITHDRAW_REQUEST`이면 예외 발생
            if (isWithdrawRequest) {
                throw new CustomException(ErrorCode.FORBIDDEN.getCode(), "회원 탈퇴 신청 중인 유저는 탈퇴 철회 신청만 가능합니다.");
            }
        }

        chain.doFilter(request, response);
    }

}
