package com.fishgo.common.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * FilterChain 은 DispatcherServlet 보다 먼저 실행 되기 때문에
 * FilterChain 내에서 던져진 예외를 GlobalExceptionHandler 에서 받지 못함
 * FilterChain 안에서 일어나는 예외를 GlobalExceptionHandler 로 전달하기 위한 필터
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@AllArgsConstructor
public class FilterChainExceptionHandler extends OncePerRequestFilter {

    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)  {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // GlobalExceptionHandler로 예외 전달
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }


}
