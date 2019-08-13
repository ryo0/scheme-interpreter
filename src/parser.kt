val <T> List<T>.head: T
    get() = first()

val <T> List<T>.tail: List<T>
    get() = drop(1)

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