val <T> List<T>.head: T
    get() = first()

val <T> List<T>.tail: List<T>
    get() = drop(1)

fun car(nodes: List<Node>): Node {
    if (nodes.count() == 0) {
        throw Error("car $nodes")
    }
    return nodes.head
}

fun cdr(nodes: List<Node>): List<Node> {
    if (nodes.count() == 0) {
        throw Error("cdr $nodes")
    }
    return nodes.tail
}

fun cadr(nodes: List<Node>): Node {
    return car(cdr(nodes))
}

fun cddr(nodes: List<Node>): List<Node> {
    return cdr(cdr(nodes))
}

fun caddr(nodes: List<Node>): Node {
    return car(cddr(nodes))
}

fun cdddr(nodes: List<Node>): List<Node> {
    return cdr(cddr(nodes))
}

fun cadddr(nodes: List<Node>): Node {
    return car(cdddr(nodes))
}

fun parseNodeList(tokens: List<Token>): List<Node> {
    return parseNodeListSub(listOf(), tokens).first
}

val OperandsHash = mapOf(
    Token.Plus to Ops.Plus,
    Token.Minus to Ops.Minus,
    Token.Asterisk to Ops.Asterisk,
    Token.Slash to Ops.Slash,
    Token.Equal to Ops.Equal,
    Token.LessThan to Ops.LessThan,
    Token.GreaterThan to Ops.GreaterThan
)

fun parseNodeListSub(acm: List<Node>, tokens: List<Token>): Pair<List<Node>, List<Token>> {
    if (tokens.count() == 0) {
        return acm to tokens
    }
    when (val first = tokens.head) {
        is Token.LParen -> {
            val (nodes, rest) = parseNodeListSub(listOf(), tokens.tail)
            return parseNodeListSub(acm + Node.Nodes(nodes), rest)
        }
        is Token.RParen -> {
            return acm to tokens.tail
        }
        else -> {
            return parseNodeListSub(acm + Node.Leaf(first), tokens.tail)
        }
    }
}

fun parseProgram(nodes: List<Node>): Program {
    val acm = mutableListOf<Form>()
    for (node in nodes) {
        acm.add(parseForm(node))
    }
    return Program(acm)
}

fun parseForm(node: Node): Form {
    when (node) {
        is Node.Leaf -> {
            return Form._Exp(parseExp(node))
        }
        is Node.Nodes -> {
            when (val first = node.ns[0]) {
                is Node.Leaf -> {
                    if (first.l is Token.Define) {
                        return parseDefine(node)
                    } else {
                        return Form._Exp(parseExp(node))
                    }
                }
                else -> {
                    return Form._Exp(parseExp(node))
                }
            }
        }
    }
}

// (define (x v1 v2 v3) 1)
// (define x 0)
fun parseDefine(node: Node): Form._Definition {
    if (node !is Node.Nodes) {
        throw Error("Leafがdefineに来てる $node")
    }
    val ns = node.ns
    val cadr = cadr(ns)
    if (cadr is Node.Nodes) {
        // (define (a x) x)
        val name = parseExp(car(cadr.ns))
        val params = parseVarList(cdr(cadr.ns))
        val body = parseProgram(cddr(ns))
        val lambda = Exp.Lambda(params, body)
        if (name !is Exp.Var) {
            throw Error("構文エラー、defineの名前が変数でない $name $node")
        }
        return Form._Definition(name, lambda)
    } else {
        // (define a 2)
        val name = parseExp(cadr(ns))
        val body = parseExp(caddr(ns))
        if (name !is Exp.Var) {
            throw Error("構文エラー、defineの名前が変数でない $name $node")
        }
        return Form._Definition(name, body)
    }

}


fun parseVarList(nodes: List<Node>): List<Exp.Var> {
//    (x y z)
    val acm = mutableListOf<Exp.Var>()
    nodes.forEach {
        if (it is Node.Leaf && it.l is Token.Var) {
            acm.add(Exp.Var(it.l.name))
        } else {
            throw Error()
        }
    }
    return acm
}

fun parseExp(node: Node): Exp {
    when (node) {
        is Node.Leaf -> {
            when (val l = node.l) {
                is Token.Num -> {
                    return Exp.Num(l.value)
                }
                is Token.Str -> {
                    return Exp.Symbol(l.name)
                }
                is Token.Var -> {
                    return Exp.Var(l.name)
                }
                is Token.True -> {
                    return Exp.Bool(TF.True)
                }
                is Token.False -> {
                    return Exp.Bool(TF.False)
                }
                in OperandsHash.keys -> {
                    val op = OperandsHash[l] ?: throw Error()
                    return Exp.Op(op)
                }
                else -> {
                    throw Error("$node")
                }
            }
        }
        is Node.Nodes -> {
            if (node.ns.count() == 0) {
                throw Error("$node")
            }
            if (node.ns.count() == 1) {
                when (val first = node.ns[0]) {
                    is Node.Leaf -> {
                        when (first.l) {
                            is Token.Num -> {
                                return Exp.Num(first.l.value)
                            }
                            is Token.Var -> {
                                return Exp.Var(first.l.name)
                            }
                            is Token.Str -> {
                                return Exp.Symbol(first.l.name)
                            }
                            else -> {
                                throw Error("構文エラー $node")
                            }
                        }
                    }
                }
            }
            when (val first = node.ns[0]) {
                is Node.Leaf -> {
                    when (first.l) {
                        is Token.If -> {
                            return parseIf(node)
                        }
                        is Token.Cond -> {
                            return parseCond(node)
                        }
                        is Token.Quote -> {
                            return parseQuote(node)
                        }
                        is Token.Set -> {
                            return parseSet(node)
                        }
                        is Token.Lambda -> {
                            return parseLambda(node)
                        }
                        is Token.Let -> {
                            return parseLet(node)
                        }
                        else -> {
                            return parseProceduteCall(node)
                        }
                    }
                }
                else -> {
                    return parseProceduteCall(node)
                }
            }
        }
    }
}

fun parseIf(node: Node.Nodes): Exp.If {
    val ns = node.ns
    val cadr = cadr(ns)
    val caddr = caddr(ns)
    if (cdddr(ns).count() != 0) {
        return Exp.If(parseExp(cadr), parseExp(caddr), parseExp(cadddr(ns)))
    } else {
        return Exp.If(parseExp(cadr), parseExp(caddr), null)
    }
}

fun parseCond(node: Node.Nodes): Exp.Cond {
//    (cond (a 1)
//          (b 2)
//          (else 3)
//          )
    var elseExp: Exp? = null
    val ccs = mutableListOf<CondClause>()
    val cdr = cdr(node.ns)
    cdr.forEach {
        it as? Node.Nodes ?: throw Error("構文エラー: cond $node")
        val car = car(it.ns)
        if (car is Node.Leaf && car.l is Token.Else) {
            elseExp = parseExp(cadr(it.ns))
        } else {
            ccs.add(parseCondClause(it))
        }
    }
    return Exp.Cond(ccs, elseExp)
}

fun parseCondClause(node: Node.Nodes): CondClause {
//    (cond (a 1)
//          (b 2)
//          (else 3)
//          )
//    の中の(a (+ 1 2))
    val car = parseExp(car(node.ns))
    val cdr = cdr(node.ns).map { parseExp(it) }
    return CondClause(car, cdr)
}

fun parseLet(node: Node.Nodes): Exp.Let {
//    (let ((a b) (c d)) (define x 2) 2)
//    (let () c)
    val ns = node.ns
    val cadr = cadr(ns) as? Node.Nodes ?: throw Error()
    val cddr = cddr(ns)
    val varExps = parseVarExpList(cadr) //((a b) (c d))
    val body = parseProgram(cddr)
    return Exp.Let(varExps, body)
}

fun parseVarExpList(node: Node.Nodes): List<VarExp> {
//    ((a b) (c d))
    val ns = node.ns
    return ns.map {
        if(it !is Node.Nodes) {
            throw Error("構文エラー let $node")
        }
        val car = car(it.ns)
        val cadr = cadr(it.ns)
        if(car is Node.Leaf && cadr is Node.Nodes) {
            val name = parseExp(car) as? Exp.Var ?: throw Error("構文エラー let $node")
            val exp = parseExp(cadr)
            VarExp(name, exp)
        } else {
            throw Error("構文エラー let $node")
        }
    }
}

fun parseSet(node: Node.Nodes): Exp.Set {
    // (set! x (+ 1 1))
    val variable = parseExp(cadr(node.ns)) as? Exp.Var ?: throw Error("set!の変数が変数じゃない $node")
    val value = parseExp(caddr(node.ns))
    return Exp.Set(variable, value)
}

fun parseQuote(node: Node.Nodes): Exp.Quote {
    val data = parseDatum(cadr(node.ns))
    return Exp.Quote(data)
}

fun parseDatum(node: Node): Datum {
    //    (quote '(1 2 3 (4 5) 6))
    when (node) {
        is Node.Leaf -> {
            return parseDatumNotLst(node)
        }
        is Node.Nodes -> {
            return parseDatumLst(node)
        }
    }
}

fun parseDatumNotLst(leaf: Node.Leaf): Datum {
    return when (val leaf = leaf.l) {
        is Token.Str -> {
            Datum.Symbol(leaf.name)
        }
        is Token.Var -> {
            Datum.Symbol(leaf.name)
        }
        is Token.True -> {
            Datum.Bool(TF.True)
        }
        is Token.False -> {
            Datum.Bool(TF.False)
        }
        is Token.Num -> {
            Datum.Num(leaf.value)
        }
        else -> {
            throw Error("構文エラー $leaf")
        }
    }
}

fun parseDatumLst(ns: Node.Nodes): Datum.Lst {
    //    (quote '(1 2 3 (4 5) 6))
    // ns(l1, l2, l3, ns(l4, l5), 6)
    return Datum.Lst(ns.ns.map { parseDatum(it) })
}

fun parseProceduteCall(node: Node.Nodes): Exp.ProcedureCall {
    val ns = node.ns
    val operator = car(ns)
    val operands = cdr(ns)
    return Exp.ProcedureCall(parseExp(operator), operands.map { parseExp(it) })
}

fun parseLambda(node: Node.Nodes): Exp.Lambda {
    //    (lambda (a x) x)
    val ns = node.ns
    val cadr = (cadr(ns)) as? Node.Nodes ?: throw Error("構文エラー: lambdaに変数/引数のカッコがない $node")
    val paramList = parseVarList(cadr.ns)
    val body = parseProgram(cddr(ns))
    return Exp.Lambda(paramList, body)
}