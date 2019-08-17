data class Env(val lst: List<MutableMap<String, Exp>>)

val initialEnv =  mutableMapOf<String, Exp>(
    "car" to Exp.Procedure{args : List<Exp> -> applyCar(args)},
    "cdr" to Exp.Procedure{args : List<Exp> -> applyCdr(args)},
    "cons" to Exp.Procedure{args : List<Exp> -> applyCons(args)},
    "null?" to Exp.Procedure{args : List<Exp> -> applyNullCheck(args)},
    "eq?" to Exp.Procedure{args : List<Exp> -> applyEqualCheck(args)},
    "and" to Exp.Procedure{args : List<Exp> -> applyAnd(args)},
    "or" to Exp.Procedure{args : List<Exp> -> applyOr(args)},
    "print" to Exp.Procedure{args : List<Exp> -> applyPrint(args)}
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
                val evaledArgs = args.map { evalExp(it, env) ?: throw Error() }
                evalProgram(exp.body, extendEnv(exp.params, evaledArgs, env))
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

fun applyEqualCheck(operands: List<Exp>): Exp {
    if(operands.count() != 2) {
        throw Error("eq? 引数が2つでない $operands")
    }
    val first = operands[0]
    val second = operands[1]
    if(first == second) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
    }
}

fun applyAnd(operands: List<Exp>): Exp {
    if(operands.count() < 2) {
        throw Error("and 引数が2つ未満 $operands")
    }
    val head = operands.head as? Exp.Bool ?: throw Error()
    val result = operands.tail.map {
        val valueExp = it as? Exp.Bool?: throw Error()
        valueExp.b
    } .foldRight(head.b, {a: TF, b: TF -> TFAnd(b, a)})
    return Exp.Bool(result)
}

fun applyOr(operands: List<Exp>): Exp {
    if(operands.count() < 2) {
        throw Error("and 引数が2つ未満 $operands")
    }
    val head = operands.head as? Exp.Bool ?: throw Error()
    val result = operands.tail.map {
        val valueExp = it as? Exp.Bool?: throw Error()
        valueExp.b
    } .foldRight(head.b, {a: TF, b: TF -> TFOr(b, a)})
    return Exp.Bool(result)
}

fun applyPrint(operands: List<Exp>): Exp? {
    if(operands.count() < 1) {
        throw Error("print 引数が1つもない $operands")
    }
    operands.forEach {
        println(convertExpToString(it))
    }
    return null
}

fun convertExpToString(exp: Exp): String {
    return when(exp) {
        is Exp.Num -> {
            exp.value.toString()
        }
        is Exp.Var -> {
            exp.name
        }
        is Exp.Bool -> {
            if(exp.b == TF.True) {
               "true"
            } else {
                "false"
            }
        }
        is Exp.Quote -> {
            if(exp.value is Datum.Lst) {
                printLstToString(exp.value)
            } else {
                "'" + convertExpToString(convertDatumToExp(exp.value))
            }
        }
        is Exp.Symbol -> {
            exp.s
        }
        else -> {
            exp.toString()
        }
    }
}

fun printLstToString(lst: Datum.Lst): String {
    var result = "("
    lst.lst.forEach {
        datum ->
        val exp = convertDatumToExp(datum)
        when(datum) {
            is Datum.Lst -> {
                result += printLstToString(datum)
            }
            else -> {
                result += convertExpToString(exp)
            }
        }
        result += " "
    }
    return "${result.slice(0 until result.length - 1)})"
}

fun TFAnd(a: TF, b: TF): TF {
    if (a == TF.True && b == TF.True) {
        return TF.True
    } else {
        return TF.False
    }
}

fun TFOr(a: TF, b: TF): TF {
    if (a == TF.False && b == TF.False) {
        return TF.False
    } else {
        return TF.True
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