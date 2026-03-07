//package com.ticket.core.domain.member.vo;
//
//import com.ticket.core.support.exception.CoreException;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.NullAndEmptySource;
//import org.junit.jupiter.params.provider.ValueSource;
//
//import static org.assertj.core.api.Assertions.assertThatCode;
//
//@SuppressWarnings("NonAsciiCharacters")
//class RawPasswordTest {
//
//    @ParameterizedTest
//    @NullAndEmptySource
//    @ValueSource(strings = {" ", "123", "abc", "1234", "abcd", "short1!", "onlyletters!", "12345678!", "noSpecial1a"})
//    void 유효하지_않은_비밀번호면_생성에_실패한다(final String value) {
//        //then
//        Assertions.assertThatThrownBy(() -> RawPassword.create(value)).isInstanceOf(CoreException.class);
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"Pa$$w0rd", "Test123!@", "MyP@ssw0rd"})
//    void 유효한_비밀번호면_Password_생성에_성공한다(final String value) {
//        assertThatCode(() -> RawPassword.create(value)).doesNotThrowAnyException();
//    }
//}
//
