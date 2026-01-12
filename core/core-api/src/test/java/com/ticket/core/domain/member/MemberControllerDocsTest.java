package com.ticket.core.domain.member;

import com.ticket.core.api.controller.MemberController;
import com.ticket.core.domain.auth.SessionConst;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureRestDocs
class MemberControllerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void getMember() throws Exception {
        // given - 로그인 세션 설정
        Long memberId = 1L;
        MemberDetails memberDetails = new MemberDetails(memberId, Role.MEMBER);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, memberDetails);

        // given - Mock 설정
        given(memberService.findById(memberId))
                .willReturn(new Member(Email.create("test@test.com"), EncodedPassword.create("password"), "홍길동", Role.MEMBER));

        // when & then
        mockMvc.perform(get("/api/members/v1")
                        .session(session))
                .andExpect(status().isOk())
                .andDo(document("member-get",
                        responseFields(
                                fieldWithPath("result").description("API 결과 상태 (SUCCESS/ERROR)"),
                                fieldWithPath("data.memberId").description("회원 ID"),
                                fieldWithPath("data.email").description("이메일 주소"),
                                fieldWithPath("data.name").description("회원 이름"),
                                fieldWithPath("data.role").description("회원 권한 (MEMBER/ADMIN)"),
                                fieldWithPath("error").description("에러 정보 (성공 시 null)")
                        )
                ));
    }
}
