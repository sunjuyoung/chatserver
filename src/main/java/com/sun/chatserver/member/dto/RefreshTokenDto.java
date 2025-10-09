package com.sun.chatserver.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDto {

    private String email;
    private String refreshToken;
    private String accessToken;
}
