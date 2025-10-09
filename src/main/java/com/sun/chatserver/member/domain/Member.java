package com.sun.chatserver.member.domain;


import com.sun.chatserver.member.dto.MemberSaveRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;




    //생성자
    public static Member createMember(MemberSaveRequestDto dto) {
        return Member.builder()
                .name(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .role(Role.USER)
                .build();
    }

}
