data class Env(val lst: List<MutableMap<String, Exp>>)

val initialEnv =  mutableMapOf<String, Exp>(
    "car" to Exp.Procedure{args : List<Exp> -> applyCar(args)},
    "cdr" to Exp.Procedure{args : List<Exp> -> applyCdr(args)},
    "cons" to Exp.Procedure{args : List<Exp> -> applyCons(args)},
    "null?" to Exp.Procedure{args : List<Exp> -> applyNullCheck(args)}
)

fun eval(p: Program): Exp? {
    return evalProgram(p, Env(listOf(initialEnv)))
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
        is Exp.Lambda -> {
            Exp.Procedure{args: List<Exp> ->
                val args = args.map { evalExp(it, env) ?: throw Error() }
                evalProgram(exp.body, extendEnv(exp.params, args, env))
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
                    val procedure = findFromEnv(operator.name, env) as? Exp.Procedure ?: throw Error()
                    procedure.p(operands.map { evalExp(it, env) ?: throw Error("引数がnull $it") })
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

fun extendEnv(params: List<Exp.Var>, args: List<Exp>, env: Env): Env {
    val thisEnv = mutableMapOf<String, Exp>()
    if(params.count() != args.count()) {
        throw Error("lambdaに渡された引数がパラメータの数に合わない")
    }
    for (i in 0 until params.count()) {
        thisEnv[params[i].name] = args[i]
    }
    return Env(listOf(thisEnv) + env.lst)
}

fun applyCalculate(op: Ops, operands: List<Exp>, env: Env) : Exp {
    val opLambda = OpHash[op] ?: throw Error()
    val head = evalExp(operands.head, env) as? Exp.Num ?: throw Error()
    val result = operands.tail.map {
        val valueExp = evalExp(it, env) as? Exp.Num ?: throw Error()
        valueExp.value
    } .foldRight(head.value, opLambda)
    return Exp.Num(result)
}

//fun applyBoolCalc(op: Ops, operands: List<Exp>, env: Env) : Exp {
//    val opLambda = OpHash[op] ?: throw Error()
//    val head = getValue(operands.head, env) as? Exp.Bool ?: throw Error()
//    val result = operands.tail.map {
//        val valueExp = getValue(it, env) as? Exp.Bool ?: throw Error()
//        valueExp.value
//    } .foldRight(head.value, opLambda)
//    return Exp.Bool(result)
//}

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
    val car = operands.head
    val cadr = operands.tail.head as? Exp.Quote ?: throw Error("cons $operands")
    val cdrlst = cadr.value as? Datum.Lst ?: throw Error("cons, ${cadr.value}")
    val carDatum = converterExpToDatum(car)
    val cdrDatum = cdrlst.lst
    return Exp.Quote(Datum.Lst(listOf(carDatum) + cdrDatum))
}

fun applyNullCheck(operands: List<Exp>): Exp {
    if(operands.count() != 1) {
        throw Error("null $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
    val lst = quotedLst.value as? Datum.Lst ?: throw Error("$quotedLst")
    if(lst.lst.count() == 0) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
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