//["define", ["a", "x"], "a" ]のようにList<Node>にマッピングしていく
sealed class Node {
    data class Leaf(val l: Token) : Node()
    data class Nodes(val ns: List<Node>) : Node()
}

data class Program(val p: List<Form>)

enum class Ops {
    Plus, Minus, Asterisk, Slash, Equal, LessThan, GreaterThan
}

enum class TF {
    True, False
}

sealed class Form {
    data class _Exp(val e: Exp) : Form()
    data class _Definition(val v: Exp.Var, val exp: Exp) : Form()
}

sealed class Exp {
    data class Op(val op: Ops) : Exp()
    data class Var(val name: String) : Exp()
    data class Bool(val b: TF) : Exp()
    data class Num(val value: Float) : Exp()
    data class ProcedureCall(val operator: Exp, val operands: List<Exp>) : Exp()
    data class If(val cond: Exp, val consequence: Exp, val alternative: Exp?) : Exp()
    data class Lambda(val vars: List<Var>, val body: Program) : Exp()
}
