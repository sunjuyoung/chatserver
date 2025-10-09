package com.sun.chatserver.member.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String authHeader = request.getHeader("Authorization");


            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                //1.공개 API 허용
                //2. Spring Security 위임      .requestMatchers("/api/members/create","/api/members/doLogin").permitAll() //회원가입, 로그인 API는 모두 접근 허용
                //                        .anyRequest().authenticated()

                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(7);
            Claims claims = jwtTokenProvider.getAllClaims(jwt);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            // 사용자명이 있고, 아직 인증되지 않은 경우
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // JWT 토큰 유효성 검증
                if (jwtTokenProvider.validateToken(jwt)) {
                    UserDetails userDetails = User.builder()
                            .username(email)
                            .password("")  // JWT 인증에서는 불필요
                            .authorities(role)
                            .build();

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT 인증 성공: {}", email);
                }
            }


        }catch (Exception e){
            log.error("JWT 인증 실패", e);
            // 예외 발생 시 SecurityContext를 비워서 인증 실패 처리
            SecurityContextHolder.clearContext();

        }


        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // JWT 검증을 제외할 경로들
        return path.equals("/api/members/doLogin") ||
                path.equals("/api/members/create");
    }
}



