package com.sun.chatserver.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MemberListDto {

    private Long id;
    private String name;
    private String email;
    private String role;
}
