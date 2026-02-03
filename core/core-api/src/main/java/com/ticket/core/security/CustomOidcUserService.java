package com.ticket.core.security;

import com.ticket.core.domain.auth.OAuth2MemberService;
import com.ticket.core.domain.auth.oauth.OAuth2UserInfo;
import com.ticket.core.domain.auth.oauth.OAuth2UserInfoFactory;
import com.ticket.core.domain.member.Member;
import com.ticket.core.enums.SocialProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2MemberService oauth2MemberService;

    public CustomOidcUserService(final OAuth2MemberService oauth2MemberService) {
        this.oauth2MemberService = oauth2MemberService;
    }

    @Override
    public OidcUser loadUser(final OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        final OidcUser oidcUser = super.loadUser(userRequest);
        final SocialProvider provider = SocialProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );
        final OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(provider, oidcUser.getAttributes());
        final Member member = oauth2MemberService.upsertMember(userInfo);
        return new CustomOidcUser(oidcUser, member.getId(), member.getRole());
    }
}
