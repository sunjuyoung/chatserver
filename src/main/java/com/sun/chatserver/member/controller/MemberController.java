package com.sun.chatserver.member.controller;

import com.sun.chatserver.member.dto.*;
import com.sun.chatserver.member.security.jwt.JwtTokenProvider;
import com.sun.chatserver.member.service.MemberService;
import com.sun.chatserver.member.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sun.chatserver.member.dto.ApiResponse.success;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> createMember(@RequestBody MemberSaveRequestDto dto) {
        Long id = memberService.createMember(dto);
        ApiResponse<Long> response = ApiResponse.<Long>success("success", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse> doLogin(@RequestBody MemberLoginDto dto) {
        MemberResponseDto responseDto = memberService.doLogin(dto);
        log.info(dto.toString());

        String token = jwtTokenProvider.createToken(responseDto.getEmail(), responseDto.getRole(),
                responseDto.getId(), responseDto.getName());
        String refreshToken = jwtTokenProvider.createRefreshToken(responseDto.getEmail(), responseDto.getRole(),
                responseDto.getId(), responseDto.getName());
        responseDto.setAccessToken(token);
        responseDto.setRefreshToken(refreshToken);
        // Save refresh token
        refreshTokenService.saveRefreshToken(refreshToken, responseDto.getId());

        ApiResponse<MemberResponseDto> response = success("success", responseDto);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/list")
    public ResponseEntity<ApiResponse> memberList() {
      List<MemberListDto> listDtoList =   memberService.memberList();
        ApiResponse<List<MemberListDto>> response = success("success", listDtoList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody LogoutRequestDto logoutRequestDto) {
        log.info("Logout request received: {}", logoutRequestDto);
        String refreshToken = logoutRequestDto.getRefreshToken();
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }
        return ResponseEntity.ok(success("Logged out successfully", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(@RequestBody RefreshTokenDto refreshDTO) {
        RefreshTokenDto refreshTokenDto = memberService.refreshAccessToken(refreshDTO.getRefreshToken());
        ApiResponse<RefreshTokenDto> response = success("success", refreshTokenDto);
        return ResponseEntity.ok(response);
    }
}
