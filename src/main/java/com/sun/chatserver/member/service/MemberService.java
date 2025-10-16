package com.sun.chatserver.member.service;

import com.sun.chatserver.member.domain.Member;
import com.sun.chatserver.member.dto.*;
import com.sun.chatserver.member.repository.MemberRepository;
import com.sun.chatserver.member.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public Long createMember(MemberSaveRequestDto dto) {

       if(memberRepository.existsByEmail(dto.getEmail())){
          throw new IllegalArgumentException("Email already exists");
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        Member member = Member.createMember(dto);
        Member saved = memberRepository.save(member);

        return saved.getId();
    }

    public MemberResponseDto doLogin(MemberLoginDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return MemberResponseDto.fromEntity(member);
    }

    public List<MemberListDto> memberList() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(member -> new MemberListDto(member.getId(), member.getName(), member.getEmail(), member.getRole().name()))
                .toList();
    }


    @Transactional
    public RefreshTokenDto refreshAccessToken(String refreshToken) {

        // 1. Refresh Token JWT 자체 유효성 검증 (서명, 만료시간)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            //throw new InvalidTokenException("Invalid refresh token");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 2. JWT에서 모든 클레임 추출
        Claims claims = jwtTokenProvider.getAllClaims(refreshToken);
        Long userId = claims.get("userId", Long.class);
        String name = claims.get("name", String.class);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        // 3. Redis에 해당 Refresh Token이 존재하는지 확인 (탈취 방지)
        Long storedUserId = refreshTokenService.getUserIdByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found in storage"));

        // 4. JWT의 userId와 Redis의 userId가 일치하는지 확인
        if (!userId.equals(storedUserId)) {
            log.error("User ID mismatch - JWT: {}, Redis: {}", userId, storedUserId);
           // throw new InvalidTokenException("Token user mismatch");
        }

        // 5. (선택) 사용자 존재 여부 및 계정 상태 확인
        // User user = userRepository.findById(userId)
        //         .orElseThrow(() -> new UserNotFoundException("User not found"));
        // if (!user.isActive()) {
        //     throw new AccountDisabledException("Account is disabled");
        // }

        // 6. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createToken(email, role, userId, name);

        // 7. (선택) Refresh Token Rotation - 보안 강화
        // String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        // refreshTokenService.rotateRefreshToken(refreshToken, newRefreshToken, userId);

        log.info("Access token refreshed for user: {}", userId);

        return RefreshTokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 refresh token 그대로 사용
                // .refreshToken(newRefreshToken) // Rotation 사용 시
                .email(email)
                .build();
    }
}
