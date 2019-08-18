data class Env(val lst: List<MutableMap<String, Exp>>)

val initialEnv = mutableMapOf<String, Exp>(
    "car" to Exp.Procedure { args: List<Exp> -> applyCar(args) },
    "cdr" to Exp.Procedure { args: List<Exp> -> applyCdr(args) },
    "cons" to Exp.Procedure { args: List<Exp> -> applyCons(args) },
    "null?" to Exp.Procedure { args: List<Exp> -> applyCheckNukll(args) },
    "eq?" to Exp.Procedure { args: List<Exp> -> applyEqualCheck(args) },
    "equal?" to Exp.Procedure { args: List<Exp> -> applyEqualCheck(args) },
    "=" to Exp.Procedure { args: List<Exp> -> applyEqualCheck(args) },
    "print" to Exp.Procedure { args: List<Exp> -> applyPrint(args) },
    ">" to Exp.Procedure { args: List<Exp> -> applyGreaterThan(args) },
    "<" to Exp.Procedure { args: List<Exp> -> applyLessThan(args) },
    "error" to Exp.Procedure { args: List<Exp> -> applyError(args) },
    "list" to Exp.Procedure { args: List<Exp> -> applyList(args) },
    "number?" to Exp.Procedure { args: List<Exp> -> applyCheckNumber(args) },
    "symbol?" to Exp.Procedure { args: List<Exp> -> applyCheckSymbol(args) },
    "pair?" to Exp.Procedure { args: List<Exp> -> applyCheckPair(args) }
)

fun eval(p: Program): Exp? {
    return evalProgram(p, Env(listOf(initialEnv)))
}

fun evalProgram(p: Program, env: Env): Exp? {
    var result: Exp? = null
    var _env = env
    for (form in p.p) {
        when (form) {
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
    return !(exp == null || (exp is Exp.Bool && exp.b == TF.False))
}

val OpHash = mapOf(
    Ops.Plus to { a: Float, b: Float -> b + a },
    Ops.Minus to { a: Float, b: Float -> b - a },
    Ops.Asterisk to { a: Float, b: Float -> b * a },
    Ops.Slash to { a: Float, b: Float -> b / a }
)

fun evalDefinition(v: Exp.Var, valueExp: Exp, env: Env): Env {
    val value = evalExp(valueExp, env)
    if (value != null) {
        env.lst.head[v.name] = value
    }
    return env
}

fun findFromEnv(name: String, env: Env): Exp? {
    env.lst.forEach {
        val result = it[name]
        if (result != null) {
            return result
        }
    }
    return null
}

fun evalExp(exp: Exp, env: Env): Exp? {
    return when (exp) {
        is Exp.Num, is Exp.Bool, is Exp.Symbol, is Exp.Quote -> {
            exp
        }
        is Exp.Var -> {
            findFromEnv(exp.name, env)
        }
        is Exp.If -> {
            evalIf(exp, env)
        }
        is Exp.Cond -> {
            evalCond(exp, env)
        }
        is Exp.And -> {
            evalAnd(exp, env)
        }
        is Exp.Or -> {
            evalOr(exp, env)
        }
        is Exp.Lambda -> {
            Exp.Procedure { args: List<Exp> ->
                val evaledArgs = args.map { evalExp(it, env) ?: throw Error() }
                evalProgram(exp.body, extendEnv(exp.params, evaledArgs, env))
            }
        }
        is Exp.Let -> {
            evalLet(exp, env)
        }
        is Exp.Begin -> {
            evalBegin(exp, env)
        }
        is Exp.Set -> {
            evalSet(exp, env)
        }
        is Exp.ProcedureCall -> {
            val operator = exp.operator
            val operands = exp.operands
            when (operator) {
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
                    val procedure =
                        findFromEnv(operator.name, env) as? Exp.Procedure ?: throw Error("定義されてない手続き: ${operator.name}")
//                    println("${operator.name}, ${operands.map {
//                        convertExpToString(
//                            evalExp(it, env) ?: throw Error("$it")
//                        )
//                    }} ")
                    procedure.p(operands.map { evalExp(it, env) ?: throw Error("引数がnull $it") })
                }
                is Exp.ProcedureCall, is Exp.Lambda -> {
                    val procedure =
                        evalExp(operator, env) as? Exp.Procedure ?: throw Error("定義されてない手続き: ${operator}")
                    procedure.p(operands.map { evalExp(it, env) ?: throw Error("引数がnull $it") })
                }
                else -> {
                    throw Error("$exp")
                }
            }
        }
        else -> {
            null
        }
    }
}

fun evalBegin(exp: Exp.Begin, env: Env): Exp? {
    return evalProgram(exp.program, env)
}

fun evalSet(exp: Exp.Set, env: Env): Exp? {
    var found = false
    env.lst.forEach {
        val variable = it[exp.variable.name]
        if (variable != null) {
            it[exp.variable.name] = evalExp(exp.value, env) ?: throw Error("代入値がありません ${exp.value}")
            found = true
        }
    }
    if (!found) {
        throw Error("未定義の値に代入は出来ません ${exp.variable}")
    }
    return Exp.Quote(Datum.Symbol("ok"))
}

fun evalLet(exp: Exp.Let, env: Env): Exp? {
    val letEnv = mutableMapOf<String, Exp>()
    exp.varExps.forEach {
        letEnv[it.name] = evalExp(it.exp, env) ?: throw Error("letの値がnull ${it.exp}")
    }
    return evalProgram(exp.body, Env(listOf(letEnv) + env.lst))
}

fun evalIf(exp: Exp.If, env: Env): Exp? {
    return if (isTrue(evalExp(exp.cond, env))) {
        evalExp(exp.consequence, env)
    } else {
        val alt = exp.alternative
        if (alt != null) {
            evalExp(alt, env)
        } else {
            null
        }
    }
}

fun evalCond(exp: Exp.Cond, env: Env): Exp? {
    exp.cc.forEach {
        val evalTest = evalExp(it.test, env)
        if (isTrue(evalTest)) {
            return it.consequence.map { consExp ->
                evalExp(consExp, env)
            }.last()
        }
    }
    val elseExp = exp.elseExp ?: throw Error("cond: else節が無い")
    return evalExp(elseExp, env)


}

fun evalAnd(exp: Exp.And, env: Env): Exp? {
    val operands = exp.operands
    operands.forEach {
        val result = evalExp(it, env)
        if (result is Exp.Bool && result.b == TF.False) {
            return Exp.Bool(TF.False)
        }
    }
    return Exp.Bool(TF.True)
}

fun evalOr(exp: Exp.Or, env: Env): Exp? {
    val operands = exp.operands
    operands.forEach {
        val result = evalExp(it, env)
        if (result is Exp.Bool && result.b == TF.True) {
            return Exp.Bool(TF.True)
        }
    }
    return Exp.Bool(TF.False)
}

fun extendEnv(params: List<Exp.Var>, args: List<Exp>, env: Env): Env {
    val thisEnv = mutableMapOf<String, Exp>()
    if (params.count() != args.count()) {
        throw Error("lambdaに渡された引数がパラメータの数に合わない")
    }
    for (i in 0 until params.count()) {
        thisEnv[params[i].name] = args[i]
    }
    return Env(listOf(thisEnv) + env.lst)
}

fun applyCalculate(op: Ops, operands: List<Exp>, env: Env): Exp {
    val opLambda = OpHash[op] ?: throw Error()
    val head = evalExp(operands.head, env) ?: throw Error("$op に nullが渡されている")
    val headValue = convertQuoteToNum(head)

    val result = operands.tail.map {
        val valueExp = evalExp(it, env) ?: throw Error("$op に nullが渡されている")
        convertQuoteToNum(valueExp)
    }.foldRight(headValue, opLambda)
    return Exp.Num(result)
}

fun convertQuoteToNum(quote: Exp): Float {
    return if (quote is Exp.Num) {
        quote.value
    } else if (quote is Exp.Quote) {
        val exp = convertDatumToExp(quote.value)  as? Exp.Num ?: throw Error()
        exp.value
    } else {
        throw Error("$quote")
    }
}

fun applyCar(operands: List<Exp>): Exp {
    if (operands.count() != 1) {
        throw Error("car $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
    val lst = quotedLst.value as? Datum.Lst ?: throw Error("$quotedLst")
    return Exp.Quote(lst.lst.head)
}

fun applyCdr(operands: List<Exp>): Exp {
    if (operands.count() != 1) {
        throw Error("cdr $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: throw Error("$operands")
    val lst = quotedLst.value as? Datum.Lst ?: throw Error("$operands $quotedLst")
    return Exp.Quote(Datum.Lst(lst.lst.tail))
}

fun applyCons(operands: List<Exp>): Exp {
    if (operands.count() != 2) {
        throw Error("cons $operands")
    }
    val car = operands.head
    val cadr = operands.tail.head as? Exp.Quote ?: throw Error("cons $operands")
    val cdrlst = cadr.value as? Datum.Lst ?: throw Error("cons, ${cadr.value}")
    val carDatum = converterExpToDatum(car)
    val cdrDatum = cdrlst.lst
    return Exp.Quote(Datum.Lst(listOf(carDatum) + cdrDatum))
}

fun applyCheckNukll(operands: List<Exp>): Exp {
    if (operands.count() != 1) {
        throw Error("null $operands")
    }
    val quotedLst = operands.first() as? Exp.Quote ?: return Exp.Bool(TF.False)
    val lst = quotedLst.value
    if (lst is Datum.Lst && lst.lst.count() == 0) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
    }
}

fun applyCheckNumber(operands: List<Exp>): Exp {
    if (operands.count() != 1) {
        throw Error("number?の引数が1つでない $operands")
    }
    val v = operands.first()
    if (v is Exp.Num) {
        return Exp.Bool(TF.True)
    } else if (v is Exp.Quote && v.value is Datum.Num) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
    }
}

fun applyCheckSymbol(operands: List<Exp>): Exp {
    if (operands.count() != 1) {
        throw Error("symbol?の引数が1つでない $operands")
    }
    val v = operands.first()
    if (v is Exp.Symbol) {
        return Exp.Bool(TF.True)
    } else if (v is Exp.Quote && v.value is Datum.Symbol) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
    }
}

fun applyCheckPair(operands: List<Exp>): Exp {
    if (operands.count() != 1) {
        throw Error("pair?の引数が1つでない $operands")
    }
    val first = operands.first()
    if (first is Exp.Quote) {
        if (first.value is Datum.Lst) {
            return Exp.Bool(TF.True)
        } else {
            return Exp.Bool(TF.False)
        }
    } else {
        return Exp.Bool(TF.False)
    }
}


fun applyEqualCheck(operands: List<Exp>): Exp {
    if (operands.count() != 2) {
        throw Error("eq? 引数が2つでない $operands")
    }
    val first = operands[0]
    val second = operands[1]
    if (first == second) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
    }
}

fun applyGreaterThan(operands: List<Exp>): Exp {
    if (operands.count() != 2) {
        throw Error("> 引数が2つでない $operands")
    }
    val first = convertQuoteToNum(operands.head)
    val second = convertQuoteToNum(operands.tail.head)
    if (first > second) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
    }
}

fun applyLessThan(operands: List<Exp>): Exp {
    if (operands.count() != 2) {
        throw Error("> 引数が2つでない $operands")
    }
    val first = convertQuoteToNum(operands.head)
    val second = convertQuoteToNum(operands.tail.head)
    if (first < second) {
        return Exp.Bool(TF.True)
    } else {
        return Exp.Bool(TF.False)
    }
}

fun applyList(operands: List<Exp>): Exp {
    if (operands.count() == 0) {
        throw Error("list 引数が1つもない")
    }
    return Exp.Quote(Datum.Lst(operands.map {
        converterExpToDatum(it)
    }))
}

fun applyError(operands: List<Exp>): Exp? {
    if (operands.count() < 1) {
        throw Error("error 引数が1つもない $operands")
    }
    val first = operands.first() as? Exp.Symbol ?: throw Error("errorに渡された1つめの引数が文字列でない")
    val second = if (operands.count() == 2) {
        operands[1]
    } else {
        Exp.Var("")
    }
    throw Error("${first.s}  ${convertExpToString(second)}")
}

fun applyPrint(operands: List<Exp>): Exp? {
    if (operands.count() < 1) {
        throw Error("print 引数が1つもない $operands")
    }
    operands.forEach {
        println(convertExpToString(it))
    }
    return null
}

fun convertExpToString(exp: Exp): String {
    return when (exp) {
        is Exp.Num -> {
            exp.value.toString()
        }
        is Exp.Var -> {
            exp.name
        }
        is Exp.Bool -> {
            if (exp.b == TF.True) {
                "true"
            } else {
                "false"
            }
        }
        is Exp.Quote -> {
            if (exp.value is Datum.Lst) {
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
    lst.lst.forEach { datum ->
        val exp = convertDatumToExp(datum)
        when (datum) {
            is Datum.Lst -> {
                result += printLstToString(datum)
            }
            else -> {
                result += convertExpToString(exp)
            }
        }
        result += " "
    }
    result = result.slice(0 until result.length - 1)
    if (result.count() == 0) {
        result = "()"
    } else {
        result += ")"
    }
    return result
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

fun convertDatumToExp(datum: Datum): Exp {
    return if (datum is Datum.Num) {
        Exp.Num(datum.value)
    } else if (datum is Datum.Bool) {
        Exp.Bool(datum.b)
    } else if (datum is Datum.Symbol) {
        Exp.Symbol(datum.s)
    } else if (datum is Datum.Lst) {
        Exp.Quote(Datum.Lst(datum.lst))
    } else if (datum is Datum._Token) {
        val token = tokenHash[datum.t] ?: throw Error()
        Exp.Symbol(token.toString())
    } else {
        throw Error()
    }
}

fun converterExpToDatum(exp: Exp): Datum {
    return if (exp is Exp.Num) {
        Datum.Num(exp.value)
    } else if (exp is Exp.Var && exp is Exp.Symbol) {
        Datum.Symbol(exp.name)
    } else if (exp is Exp.Bool) {
        Datum.Bool(exp.b)
    } else if (exp is Exp.Quote) {
        exp.value
    } else {
        throw Error("変換できない $exp")
    }
}