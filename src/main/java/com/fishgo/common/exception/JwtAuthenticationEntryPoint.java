package com.fishgo.common.exception;

import com.fishgo.common.constants.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 인증 과정에서 발생하는 예외가 이 메서드로 전달된다.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("권한 없음 예외 - AuthenticationException : {}", authException.getMessage());

        // 401 Unauthorized 상태 코드로 응답
        handleException(response, "권한이 없습니다.", ErrorCode.AUTHENTICATION_FAILED.getCode());
    }

    private void handleException(HttpServletResponse response, String message, int errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String errorResponse = "{\"message\":\"" + message + "\"," +
                "\"status\":"+ errorCode +"}";
        response.getWriter().write(errorResponse);
    }
}
