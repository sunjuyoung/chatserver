package com.sun.chatserver.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSaveRequestDto {

    private String name;
    private String email;
    private String password;
}
