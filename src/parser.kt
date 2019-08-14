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

fun isPair(node: Node): Boolean {
    return node is Node.Nodes
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
                    throw Error()
                }
            }
        }
        is Node.Nodes -> {
            when (val first = node.ns[0]) {
                is Node.Leaf -> {
                    when (val l = first.l) {
                        is Token.If -> {
                            return parseIf(node)
                        }
                        is Token.Lambda -> {
                            return parseLambda(node)
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