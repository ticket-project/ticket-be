package com.ticket.core.domain.auth;

import com.ticket.core.domain.auth.oauth.OAuth2UserInfo;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.enums.Role;
import com.ticket.core.enums.SocialProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2MemberServiceTest {

    @InjectMocks
    private OAuth2MemberService oauth2MemberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void returnsExistingMemberWhenProviderIdMatches() {
        final OAuth2UserInfo userInfo = new TestUserInfo("123", "user@test.com", "user", "http://img");
        final Member existing = new Member(Email.create("user@test.com"), null, "old", Role.MEMBER, SocialProvider.GOOGLE, "123", null);
        when(memberRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "123")).thenReturn(Optional.of(existing));

        final Member result = oauth2MemberService.upsertMember(userInfo);

        assertThat(result).isSameAs(existing);
        assertThat(result.getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(result.getProviderId()).isEqualTo("123");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void linksSocialInfoWhenEmailMatches() {
        final OAuth2UserInfo userInfo = new TestUserInfo("999", "link@test.com", "linked", "http://img");
        final Member existing = new Member(Email.create("link@test.com"), null, "old", Role.MEMBER);
        when(memberRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "999")).thenReturn(Optional.empty());
        when(memberRepository.findByEmail_Email("link@test.com")).thenReturn(Optional.of(existing));

        final Member result = oauth2MemberService.upsertMember(userInfo);

        assertThat(result.getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(result.getProviderId()).isEqualTo("999");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void savesWhenMemberIsNew() {
        final OAuth2UserInfo userInfo = new TestUserInfo("abc", "new@test.com", "new", "http://img");
        when(memberRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "abc")).thenReturn(Optional.empty());
        when(memberRepository.findByEmail_Email("new@test.com")).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Member result = oauth2MemberService.upsertMember(userInfo);

        final ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        final Member saved = captor.getValue();
        assertThat(saved.getEmail().getEmail()).isEqualTo("new@test.com");
        assertThat(saved.getProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(saved.getProviderId()).isEqualTo("abc");
        assertThat(result).isSameAs(saved);
    }

    private static class TestUserInfo implements OAuth2UserInfo {
        private final String providerId;
        private final String email;
        private final String name;
        private final String imageUrl;

        private TestUserInfo(final String providerId, final String email, final String name, final String imageUrl) {
            this.providerId = providerId;
            this.email = email;
            this.name = name;
            this.imageUrl = imageUrl;
        }

        @Override
        public SocialProvider getProvider() {
            return SocialProvider.GOOGLE;
        }

        @Override
        public String getProviderId() {
            return providerId;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getImageUrl() {
            return imageUrl;
        }
    }
}
