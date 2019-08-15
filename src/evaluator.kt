fun evalProgram(p: Program): Exp?{
    var result: Exp? = null
    for (form in p.p) {
        when(form) {
            is Form._Exp -> {
                result = evalExp(form.e)
            }
            else -> {
                result = null
            }
        }
    }
    return result
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
    return when(exp) {
        is Exp.Num, is Exp.Var, is Exp.Bool, is Exp.Symbol, is Exp.Quote -> {
            exp
        }
        is Exp.If -> {
            if(isTrue(evalExp(exp.cond))) {
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
                            applyCalculate(op, operands)
                        }
                        else -> {
                            throw Error()
                        }
                    }
                }
                is Exp.Var -> {
                    when(val name = operator.name) {
                        "car" -> applyCar(operands)
                        "cdr" -> applyCdr(operands)
                        "cons"  -> applyCons(operands)
                        else -> throw Error("未対応な関数 $name")
                    }
                }
                else -> {
                    throw Error()
                }
            }
        }
        else -> {
            null
        }
    }
}

fun applyCalculate(op: Ops, operands: List<Exp>) : Exp {
    val opLambda = OpHash[op] ?: throw Error()
    val head = operands.head as? Exp.Num ?: throw Error()
    val result = operands.tail.map {
        if (it is Exp.Num) {
            it.value
        } else {
            throw Error()
        }
    } .foldRight(head.value, opLambda)
    return Exp.Num(result)
}
fun applyCar(operands: List<Exp>): Exp {
    if(operands.count() != 1) {
        throw Error("car $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
    val lst = quotedLst.value as? Datum.Lst ?: throw Error("$quotedLst")
    return Exp.Quote(lst.lst.head)
}

fun applyCdr(operands: List<Exp>): Exp {
    if(operands.count() != 1) {
        throw Error("cdr $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
    val lst = quotedLst.value as? Datum.Lst ?: throw Error("$quotedLst")
    return Exp.Quote(Datum.Lst(lst.lst.tail))
}

fun applyCons(operands: List<Exp>) : Exp {
    if(operands.count() != 2) {
        throw Error("cons $operands")
    }
    val car = evalExp(operands.head) ?: throw Error("carがnull $operands")
    val cadr = operands.tail.head as? Exp.Quote ?: throw Error("cons $operands")
    val cdrlst = cadr.value as? Datum.Lst ?: throw Error("cons, ${cadr.value}")
    val carDatum = converterExpToDatum(car)
    val cdrDatum = cdrlst.lst
    return Exp.Quote(Datum.Lst(listOf(carDatum) + cdrDatum))
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