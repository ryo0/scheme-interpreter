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
    if (nodes.count() <= 1) {
        throw Error("cadr $nodes")
    }
    return car(cdr(nodes))
}

fun cddr(nodes: List<Node>): List<Node>{
    if (nodes.count() <= 1) {
        throw Error("cddr $nodes")
    }
    return cdr(cdr(nodes))
}

fun caddr(nodes: List<Node>): Node {
    if (nodes.count() <= 2) {
        throw Error("cadr $nodes")
    }
    return car(cddr(nodes))
}

fun cdddr(nodes: List<Node>): List<Node>{
    if (nodes.count() <= 3) {
        throw Error("cddr $nodes")
    }
    return cdr(cddr(nodes))
}

fun cadddr(nodes: List<Node>): Node {
    if (nodes.count() <= 3) {
        throw Error("cadr $nodes")
    }
    return car(cdddr(nodes))
}

fun parseNodeList(tokens: List<Token>) : List<Node> {
    return parseNodeListSub(listOf(), tokens).first
}

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