package com.sun.chatserver.member.dto;


import com.sun.chatserver.member.domain.Member;
import lombok.Data;

@Data
public class MemberResponseDto {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;


    public static MemberResponseDto fromEntity(Member member) {
        MemberResponseDto dto = new MemberResponseDto();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        dto.setRole(member.getRole().name());
        return dto;
    }
}
