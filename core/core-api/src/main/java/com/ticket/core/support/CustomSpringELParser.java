package com.ticket.core.support;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


public class CustomSpringELParser {

    /**
     * @param parameterNames : 메소드 파라미터 이름들 (예: {"seatId", "userId"})
     * @param args           : 실제 파라미터 값들 (예: {100L, 55L})
     * @param key            : 어노테이션에 적은 키 값 (예: "#seatId")
     */
    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return parser.parseExpression(key).getValue(context, Object.class);
    }
}
