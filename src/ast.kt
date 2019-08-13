data class Program(val SExps: List<SExp>)

sealed class SExp {
//    data class DefineVar(val varName: String, val _var: SExp) : SExp()
//    data class DefineLambda(val varName :String, val lambda: Lambda) : SExp()
//    data class Lambda(val paramList: List<Var>, val body: List<SExp>): SExp()
    data class ProcedureCall(val op: String, val SExps: List<SExp>) : SExp()
    data class Var(val name: String) : SExp()
    data class Num(val value: Float) : SExp()
    data class If(val cond: SExp, val ifExp: SExp, val elseExp: SExp) : SExp()
//    data class Let(val names: List<Atom>, val values: List<SExp>, val body: List<SExp>)
}