package com.ticket.core.domain.auth;

import com.ticket.core.domain.member.AddMember;
import com.ticket.core.enums.Role;
import com.ticket.storage.db.core.MemberEntity;
import com.ticket.storage.db.core.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 회원가입에_성공한다() {
        //given
        final AddMember addMember = new AddMember("test@test.com", "1234", "test", Role.MEMBER);
        //when
        authService.register(addMember);
        //then
        final boolean isExist = memberRepository.existsByEmail(addMember.getEmail());
        assertThat(isExist).isTrue();

        final MemberEntity memberEntity = memberRepository.findByEmail(addMember.getEmail()).orElseThrow();
        assertThat(memberEntity.getEmail()).isEqualTo(addMember.getEmail());
        assertThat(memberEntity.getPassword()).isNotEqualTo("1234");
    }

}
