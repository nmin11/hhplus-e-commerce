package kr.hhplus.be.server.support.spel

import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

object CustomSpringELParser {
    fun <T> getDynamicValue(parameterNames: Array<String>, args: Array<Any>, key: String, resultClass: Class<T>): T {
        val parser: ExpressionParser = SpelExpressionParser()
        val context = StandardEvaluationContext()

        parameterNames.forEachIndexed { index, name ->
            context.setVariable(name, args[index])
        }

        return parser.parseExpression(key).getValue(context, resultClass)
            ?: throw IllegalArgumentException("Failed to evaluate the expression: $key")
    }
}
