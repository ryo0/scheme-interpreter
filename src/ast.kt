//["define", ["a", "x"], "a" ]のようにList<Node>にマッピングしていく
sealed class Node {
    data class Leaf(val l: Token) : Node()
    data class Nodes(val ns: List<Node>) : Node()
}

data class Program(val p: List<Form>)

enum class Ops {
    Plus, Minus, Asterisk, Slash, Equal, LessThan, GreaterThan, And, Or
}

enum class TF {
    True, False
}

sealed class Form {
    data class _Exp(val e: Exp) : Form()
    data class _Definition(val v: Exp.Var, val exp: Exp) : Form()
}

sealed class Datum {
    data class Bool(val b: TF) : Datum()
    data class Num(val value: Float) : Datum()
    data class Symbol(val s: String) : Datum()
    data class _Token(val t: Token) : Datum()
    data class Lst(val lst: List<Datum>) : Datum()
}

data class CondClause(val test: Exp, val consequence: List<Exp>)

data class VarExp(val name: String, val exp: Exp)

sealed class Exp {
    data class Op(val op: Ops) : Exp()
    data class Var(val name: String) : Exp()
    data class Bool(val b: TF) : Exp()
    data class Num(val value: Float) : Exp()
    data class Symbol(val s: String) : Exp()
    data class ProcedureCall(val operator: Exp, val operands: List<Exp>) : Exp()
    data class If(val cond: Exp, val consequence: Exp, val alternative: Exp?) : Exp()
    data class Cond(val cc: List<CondClause>, val elseExp: Exp?) : Exp()
    data class Set(val variable: Var, val value: Exp) : Exp()
    data class Let(val varExps: List<VarExp>, val body: Program) : Exp()
    data class Lambda(val params: List<Var>, val body: Program) : Exp()
    data class Procedure(val p: (List<Exp>) -> Exp?) : Exp()
    data class Quote(val value: Datum) : Exp()
    data class Begin(val program: Program) : Exp()
    data class And(val operands: List<Exp>) : Exp()
    data class Or(val operands: List<Exp>) : Exp()
}
