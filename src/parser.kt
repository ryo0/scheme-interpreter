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
    return nodes.tail
}

fun cadr(nodes: List<Node>): Node {
    return car(cdr(nodes))
}

fun cddr(nodes: List<Node>): List<Node>{
    return cdr(cdr(nodes))
}

fun caddr(nodes: List<Node>): Node {
    return car(cddr(nodes))
}

fun cdddr(nodes: List<Node>): List<Node>{
    return cdr(cddr(nodes))
}

fun cadddr(nodes: List<Node>): Node {
    return car(cdddr(nodes))
}

fun parseNodeList(tokens: List<Token>) : List<Node> {
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

fun parseNodeListSub(acm: List<Node>, tokens: List<Token>): Pair<List<Node>, List<Token>>{
    if(tokens.count() == 0)  {
        return acm to tokens
    }
    when(val first = tokens.head) {
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

fun parseProgram(nodes: List<Node>): Program{
    val acm = mutableListOf<Exp>()
    for (node in nodes) {
        acm.add(parseExp(node))
    }
    return Program(acm)
}

fun parseExp(node: Node): Exp {
    when(node) {
        is Node.Leaf -> {
            when(val l = node.l) {
                is Token.Num -> {
                    return Exp.Num(l.value)
                }
                is Token.Var -> {
                    return Exp.Var(l.name)
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
            when(val first = node.ns[0]) {
                is Node.Leaf ->  {
                    when(val l = first.l) {
                        is Token.If ->{
                            val ns = node.ns
                            val cadr = cadr(ns)
                            val caddr = caddr(ns)
                            if(cdddr(ns).count() != 0) {
                                return Exp.If(parseExp(cadr), parseExp(caddr), parseExp(cadddr(ns)))
                            } else {
                                return Exp.If(parseExp(cadr), parseExp(caddr), null)
                            }

                        }
                        else -> {
                            val ns = node.ns
                            val operator = car(ns)
                            val operands = cdr(ns)
                            return Exp.ProcedureCall(parseExp(operator), operands.map { parseExp(it) })
                        }
                    }
                }
                else -> {
                    throw Error()
                }
            }
        }
    }
}