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
                            throw Error("$name $operands")
                        }
                        val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
                        val lst = quotedLst.value as? Datum.Lst ?: throw Error("$quotedLst")
                        if(name == "car") {
                            return Exp.Quote(lst.lst.head)
                        } else {
                            return Exp.Quote(Datum.Lst(lst.lst.tail))
                        }
                    } else if (name == "cons") {
                        if(operands.count() != 2) {
                            throw Error("$name $operands")
                        }
                        val car = evalExp(operands.head) ?: throw Error("carがnull $operands")
                        val cadr = operands.tail.head as? Exp.Quote ?: throw Error("$name $operands")
                        val cdrlst = cadr.value as? Datum.Lst ?: throw Error("$name, ${cadr.value}")
                        val carDatum = converterExpToDatum(car)
                        val cdrDatum = cdrlst.lst
                        return Exp.Quote(Datum.Lst(listOf(carDatum) + cdrDatum))
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

fun convertDatumToExp(datum: Datum) : Exp {
    return if(datum is Datum.Num) {
        Exp.Num(datum.value)
    } else if (datum is Datum.Bool){
        Exp.Bool(datum.b)
    } else if (datum is Datum.Symbol) {
        Exp.Symbol(datum.s)
    } else if (datum is Datum.Lst) {
        Exp.Quote(Datum.Lst(datum.lst))
    } else {
        throw Error("変換できない $datum")
    }
}

fun converterExpToDatum(exp: Exp): Datum {
    return if(exp is Exp.Num) {
        Datum.Num(exp.value)
    } else if (exp is Exp.Var && exp is Exp.Symbol) {
        Datum.Symbol(exp.name)
    } else if (exp is Exp.Bool ) {
        Datum.Bool(exp.b)
    } else if(exp is Exp.Quote) {
        exp.value
    } else {
        throw Error("変換できない $exp")
    }
}