package com.ticket.member.controller;

import com.ticket.member.dto.MemberCreateRequest;
import com.ticket.member.dto.MemberResponse;
import com.ticket.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> register(@RequestBody @Valid MemberCreateRequest memberCreateRequest) {
        return ResponseEntity.ok().body(memberService.register(memberCreateRequest.toCommand()));
    }
}
