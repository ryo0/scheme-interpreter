fun car(tokens: List<Token>): Token {
    if(tokens.count() < 2 && tokens.first() != Token.LParen) {
        throw Error("carエラー $tokens")
    }
    return tokens[1]
}

fun cdr(tokens: List<Token>): Pair<List<Token>, List<Token>>{
    if(tokens.count() < 3 && tokens.first() != Token.LParen) {
        throw Error("cdrエラー $tokens")
    }
    var parenCounter = 1
    var result = mutableListOf<Token>()
    var i = 2// (a b c)だったらbから始める
    while(parenCounter != 0) {
        val token = tokens[i]
        when(token) {
            is Token.RParen -> {
                parenCounter--
            }
            is Token.LParen -> {
                parenCounter++
            }
            else -> {
            }
        }
        result.add(token)
        i++
    }
    return result to tokens.slice(i until tokens.count())
}
//(+ 1 2 3)

//<expression> --> <variable>
//| <literal>
//| <procedure call>

//fun parseSExps(tokens: List<Token>, acm: List<SExp>): Pair<List<SExp>, List<Token>>{
//    when(val first = tokens.car) {
//        is Token.LParen -> {
//            return parseSExps(tokens.cdr, acm)
//        }
//        is Token.RParen -> {
//            return acm to tokens.cdr
//        }
//        is Token.Plus -> {
//
//        }
//        is Token.Var -> {
//            return parseSExps(tokens.cdr, acm + SExp.Var(first.name))
//        }
//        is Token.Num -> {
//            return parseSExps(tokens.cdr, acm + SExp.Num(first.value))
//        }
//        is Token.Define -> {
//            val (def, rest) = parseDefineLambda(tokens.cdr)
//            return parseSExps(rest.cdr, acm + def)
//        }
//        else -> {
//            throw Error()
//        }
//    }
//}
//
//
//fun parseProcedureCall(tokens: List<Token>, operator: SExp?, oprands: List<SExp>): Pair<SExp.ProcedureCall, List<Token>> {
//
//}

// (define (x a) a)なら (x a) a)が返ってくる

//// (define (x a) a)なら (x a) a)がtokensとして返ってくる
//fun parseDefineLambda(tokens: List<Token>): Pair<SExp.DefineLambda, List<Token>>{
//    when(val first = tokens.car) {
//        is Token.LParen -> {
//            parseDefineLambda(tokens.cdr)
//        }
//        is Token.Var -> {
//            val name = first.name
//            val (paramList, restTokens) = parseParamList(tokens.cdr, listOf())
//
//        }
//    }
//}
// a b c)みたいのが渡ってくる
//fun parseParamList(tokens: List<Token>, acm: List<Atom>): Pair<List<Atom>, List<Token>> {
//    when(val first = tokens.car) {
//        is Token.RParen -> {
//            return acm to tokens.cdr
//        }
//        is Token.Var -> {
//            return parseParamList(tokens.cdr, acm + Atom.Var(first.name))
//        }
//        else -> {
//            throw Error()
//        }
//    }
//}
//
//fun parseDefineVar(tokens: List<Token>): Pair<SExp, List<Token>>{
//
//}