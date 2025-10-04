package com.sun.chatserver.member.service;

import com.sun.chatserver.member.domain.Member;
import com.sun.chatserver.member.dto.MemberLoginDto;
import com.sun.chatserver.member.dto.MemberResponseDto;
import com.sun.chatserver.member.dto.MemberSaveRequestDto;
import com.sun.chatserver.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
}
