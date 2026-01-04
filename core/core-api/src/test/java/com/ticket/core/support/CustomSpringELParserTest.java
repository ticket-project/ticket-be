package com.ticket.core.support;

import com.ticket.core.domain.hold.NewSeatHold;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void SPEL_결과가_Collection이면_Flatten되어_리스트로_반환된다() {
        //given
        String prefix = "LOCK:";
        String[] parameterNames = {"seatIds"};
        Object[] args = {List.of(1L, 2L, 3L)};
        String[] dynamicKey = {"#seatIds"};
        //when
        final List<String> keys = CustomSpringELParser.getDynamicValue(prefix, parameterNames, args, dynamicKey);
        //then
        assertThat(keys).containsExactly("LOCK:1", "LOCK:2", "LOCK:3");
    }

    @Test
    @DisplayName("SPEL에서 객체(Record/POJO)의 프로퍼티 접근이 가능하다")
    void nestedPropertyAccess() {
        // given
        String prefix = "LOCK:";
        NewSeatHold newSeatHold = new NewSeatHold(1L, 2L, List.of(1L, 2L));

        String[] parameterNames = {"newSeatHold"};
        Object[] args = {newSeatHold};
        String[] dynamicKey = {"#newSeatHold.getSeatIds()"};

        // when
        List<String> keys = CustomSpringELParser.getDynamicValue(prefix, parameterNames, args, dynamicKey);

        // then
        assertThat(keys).containsExactly("LOCK:1", "LOCK:2");
    }

    @Test
    void dynamicKey가_빈_배열이면_keys가_0개이므로_IllegalStateException을_던진다() {
        // given
        String prefix = "LOCK:";
        String[] parameterNames = {"memberId"};
        Object[] args = {1L};
        String[] dynamicKey = {};

        // when & then
        assertThatThrownBy(() ->
                CustomSpringELParser.getDynamicValue(prefix, parameterNames, args, dynamicKey)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("keys가 0개입니다.");
    }

    @Test
    void parameterNames와_args_길이가_다르면_context_세팅중_예외가_발생할_수_있다() {
        // given
        String prefix = "LOCK:";
        String[] parameterNames = {"a", "b"};
        Object[] args = {1}; // b에 해당하는 args[1]가 없음
        String[] dynamicKey = {"#a"};

        // when & then
        assertThatThrownBy(() ->
                CustomSpringELParser.getDynamicValue(prefix, parameterNames, args, dynamicKey)
        ).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

}