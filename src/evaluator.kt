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
                when(operator.op) {
                    Ops.Plus -> {
//                        .foldRight(0f, { acm, value  -> acm + value })
                        val result = operands.map {
                            if(it is Exp.Num) {
                                it.value
                            } else {
                                throw Error()
                            }
                        }
                            .foldRight(0f, {acm, value -> acm + value})
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