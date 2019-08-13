//data class Program(val SExps: List<SExp>)

//["define", ["a", "x"], "a" ]のようにList<Node>にマッピングしていく
sealed class Node {
    data class Leaf(val l: String): Node()
    data class Nodes(val ns: List<Node>): Node()
}