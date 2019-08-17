data class Env(val lst: List<MutableMap<String, Exp>>)

fun eval(p: Program): Exp? {
    return evalProgram(p, Env(listOf(mutableMapOf())))
}

fun evalProgram(p: Program, env: Env): Exp?{
    var result: Exp? = null
    var _env = env
    for (form in p.p) {
        when(form) {
            is Form._Exp -> {
                result = evalExp(form.e, env)
            }
            is Form._Definition -> {
                _env = evalDefinition(form.v, form.exp, _env)
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

val primitiveProcedures = mapOf(
    "car" to {operands : List<Exp>, env: Env ->  applyCar(operands, env)},
    "cdr" to {operands : List<Exp>, env: Env  -> applyCdr(operands, env)},
    "cons" to {operands : List<Exp>, env: Env  -> applyCons(operands, env)}
)

fun evalDefinition(v: Exp.Var, valueExp: Exp, env: Env): Env {
    val value = evalExp(valueExp, env)
    if (value != null) {
        env.lst.head[v.name] = value
    }
    return env
}

fun findFromEnv(name: String, env: Env): Exp? {
    env.lst.forEach{
        val result = it[name]
        if(result != null) {
            return result
        }
    }
    return null
}

fun evalExp(exp: Exp, env: Env): Exp? {
    return when(exp) {
        is Exp.Num, is Exp.Bool, is Exp.Symbol, is Exp.Quote -> {
            exp
        }
        is Exp.Var -> {
            findFromEnv(exp.name, env)
        }
        is Exp.If -> {
            if(isTrue(evalExp(exp.cond, env))) {
                evalExp(exp.consequence, env)
            } else {
                val alt = exp.alternative
                if(alt != null) {
                    evalExp(alt, env)
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
                            applyCalculate(op, operands, env)
                        }
                        else -> {
                            throw Error()
                        }
                    }
                }
                is Exp.Var -> {
                    val procedure = primitiveProcedures[operator.name]
                    if(procedure != null ){
                        procedure(operands, env)
                    }
                    else {
                        throw Error("未対応な関数 ${operator.name}")
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

fun getValue(exp: Exp, env: Env): Exp {
    if(exp is Exp.Num) {
        return exp
    }
    else if(exp is Exp.Var) {
        return findFromEnv(exp.name, env) ?: throw Error()
    } else {
        throw Error()
    }
}

fun applyCalculate(op: Ops, operands: List<Exp>, env: Env) : Exp {
    val opLambda = OpHash[op] ?: throw Error()
    val head = getValue(operands.head, env) as? Exp.Num ?: throw Error()
    val result = operands.tail.map {
        val valueExp = getValue(it, env) as? Exp.Num ?: throw Error()
        valueExp.value
    } .foldRight(head.value, opLambda)
    return Exp.Num(result)
}

fun applyCar(operands: List<Exp>, env: Env): Exp {
    if(operands.count() != 1) {
        throw Error("car $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
    val lst = quotedLst.value as? Datum.Lst ?: throw Error("$quotedLst")
    return Exp.Quote(lst.lst.head)
}

fun applyCdr(operands: List<Exp>, env: Env): Exp {
    if(operands.count() != 1) {
        throw Error("cdr $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
    val lst = quotedLst.value as? Datum.Lst ?: throw Error("$quotedLst")
    return Exp.Quote(Datum.Lst(lst.lst.tail))
}

fun applyCons(operands: List<Exp>, env: Env) : Exp {
    if(operands.count() != 2) {
        throw Error("cons $operands")
    }
    val car = evalExp(operands.head, env) ?: throw Error("carがnull $operands")
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