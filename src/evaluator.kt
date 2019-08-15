fun evalProgram(p: Program): Exp?{
    for (form in p.p) {
        when(form) {
            is Form._Exp -> {
                return evalExp(form.e)
            }
            else -> {
                return null
            }
        }
    }
    return null
}

fun isTrue(exp: Exp?): Boolean {
    return exp != null && exp is Exp.Bool && exp.b == TF.True
}

val OpHash = mapOf(
    Ops.Plus to {a: Float, b: Float -> b + a},
    Ops.Minus to {a: Float, b: Float -> b - a},
    Ops.Asterisk to {a: Float, b: Float -> b * a},
    Ops.Slash to {a: Float, b: Float -> b / a}
)

fun evalExp(exp: Exp): Exp? {
    when(exp) {
        is Exp.Num, is Exp.Var, is Exp.Bool -> {
            return exp
        }
        is Exp.If -> {
            return if(isTrue(evalExp(exp.cond))) {
                evalExp(exp.consequence)
            } else {
                val alt = exp.alternative
                if(alt != null) {
                    evalExp(alt)
                } else {
                    null
                }
            }
        }
        is Exp.ProcedureCall -> {
            val operator = exp.operator
            val operands = exp.operands
            if(operator is Exp.Op) {
                when(val op = operator.op) {
                    in OpHash.keys -> {
                        val opLambda = OpHash[op] ?: throw Error()
                        val head = operands.head as? Exp.Num ?: throw Error()
                        val result = operands.tail.map {
                            if(it is Exp.Num) {
                                it.value
                            } else {
                                throw Error()
                            }
                        }
                            .foldRight(head.value, opLambda)
                        return Exp.Num(result)
                    }
                    else -> {
                        throw Error()
                    }
                }
            } else {
                throw Error()
            }
        }
        else -> {
            return null
        }
    }
}