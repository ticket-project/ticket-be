package com.ticket.core.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}