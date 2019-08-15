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
        is Exp.Num, is Exp.Var, is Exp.Bool, is Exp.Symbol, is Exp.Quote -> {
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
            when(operator) {
                is Exp.Op -> {
                    when (val op = operator.op) {
                        in OpHash.keys -> {
                            val opLambda = OpHash[op] ?: throw Error()
                            val head = operands.head as? Exp.Num ?: throw Error()
                            val result = operands.tail.map {
                                if (it is Exp.Num) {
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
                }
                is Exp.Var -> {
                    val name = operator.name
                    if(name == "car" || name == "cdr") {
                        if(operands.count() != 1) {
                            throw Error("$operands")
                        }
                        val quotedLst = operands.first() as? Exp.Quote ?: throw Error()
                        val lst = quotedLst.value as? Datum.Lst ?: throw Error()
                        if(name == "car") {
                            return Exp.Quote(lst.lst.head)
                        } else {
                            return Exp.Quote(Datum.Lst(lst.lst.tail))
                        }
                    } else {
                        throw Error("未対応な関数 $name")
                    }
                }
                else -> {
                    throw Error()
                }
            }
        }
        else -> {
            return null
        }
    }
}