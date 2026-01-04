package com.ticket.core.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class CustomSpringELParserTest {

    @Test
    void 단일_SPEL_표현식이_단일_값이면_prefix가_붙은_키_1개를_반환한다() {
        //given
        String prefix = "LOCK:";
        String[] parameterNames = {"seatId"};
        Object[] args = {100L};
        String[] dynamicKey = {"#seatId"};
        //when
        final List<String> keys = CustomSpringELParser.getDynamicValue(prefix, parameterNames, args, dynamicKey);
        //then
        assertThat(keys).containsExactly("LOCK:100");
    }

    @Test
    void 여러_SPEL_표현식이면_각_평가된_결과에_prefix가_붙어_리스트로_반환된다_순서보장() {
        //given
        String prefix = "LOCK:";
        String[] parameterNames = {"seatId", "memberId"};
        Object[] args = {100L, 1L};
        String[] dynamicKey = {"#seatId", "#memberId"};
        //when
        final List<String> keys = CustomSpringELParser.getDynamicValue(prefix, parameterNames, args, dynamicKey);
        //then
        assertThat(keys).containsExactly("LOCK:100", "LOCK:1");

    }
}