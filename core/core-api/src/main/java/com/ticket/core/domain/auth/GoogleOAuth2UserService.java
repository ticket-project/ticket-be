package com.ticket.core.domain.auth;

import com.ticket.core.config.CustomOAuth2User;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    private final MemberRepository memberRepository;
    private final PasswordService passwordService;

    public GoogleOAuth2UserService(
            final MemberRepository memberRepository,
            final PasswordService passwordService
    ) {
        this.memberRepository = memberRepository;
        this.passwordService = passwordService;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        
        final String email = oAuth2User.getAttribute("email");
        final String name = oAuth2User.getAttribute("name");
        final String providerId = oAuth2User.getAttribute("sub"); // 구글 ID
        
        if (email == null || name == null) {
            throw new OAuth2AuthenticationException("Google에서 이메일 또는 이름을 가져올 수 없습니다.");
        }

        // 기존 회원 조회 또는 신규 회원 생성
        final Member member = memberRepository.findByEmail_Email(email)
                .orElseGet(() -> createSocialMember(email, name));

        return new CustomOAuth2User(member, oAuth2User.getAttributes());
    }

    private Member createSocialMember(final String email, final String name) {
        // 소셜 로그인 사용자는 랜덤 비밀번호 생성 (실제로는 사용하지 않음)
        final String randomPassword = UUID.randomUUID().toString();
        final EncodedPassword encodedPassword = EncodedPassword.create(
                passwordService.encode(randomPassword)
        );

        final Member newMember = new Member(
                Email.create(email),
                encodedPassword,
                name,
                Role.MEMBER  // 소셜 로그인 사용자는 기본적으로 MEMBER 권한
        );

        return memberRepository.save(newMember);
    }
}
