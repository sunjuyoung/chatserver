package com.sun.chatserver.member.controller;

import com.sun.chatserver.member.dto.ApiResponse;
import com.sun.chatserver.member.dto.MemberLoginDto;
import com.sun.chatserver.member.dto.MemberResponseDto;
import com.sun.chatserver.member.dto.MemberSaveRequestDto;
import com.sun.chatserver.member.security.jwt.JwtTokenProvider;
import com.sun.chatserver.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sun.chatserver.member.dto.ApiResponse.success;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createMember(@RequestBody MemberSaveRequestDto dto) {
        Long id = memberService.createMember(dto);
        ApiResponse<Long> response = ApiResponse.<Long>success("success", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<ApiResponse> doLogin(@RequestBody MemberLoginDto dto) {
        MemberResponseDto responseDto = memberService.doLogin(dto);

        String token = jwtTokenProvider.createToken(responseDto.getEmail(), responseDto.getRole());
        responseDto.setAccessToken(token);
        //responseDto.setRefreshToken(token);

        ApiResponse<MemberResponseDto> response = success("success", responseDto);
        return ResponseEntity.ok(response);
    }


}
