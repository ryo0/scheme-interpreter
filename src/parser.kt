fun car(tokens: List<Token>): Pair<List<Token>, List<Token>> {
    if(tokens.count() < 2 || tokens[0] == Token.LParen && tokens[1] == Token.RParen) {
        throw Error("carエラー $tokens")
    }
    if(tokens[1] !is Token.LParen) {
        // (a b c)みたいなの

        val rest = tokens.slice(2 until tokens.count())
        if(rest.count() <= 2) {
            // かっこだけの時
            return listOf(tokens[1]) to listOf()
        } else {
            return listOf(tokens[1]) to listOf(Token.LParen) + tokens.slice(2 until tokens.count())
        }
    }
    // ((a b c) d e)だったら(a b c)がほしい
    var parenCounter = 1
    var result = mutableListOf<Token>(Token.LParen)
    var i = 2 // aから読み始める
    while(i < tokens.count() && parenCounter != 0) {
        val token = tokens[i]
        when(token) {
            is Token.RParen -> {
                parenCounter--
            }
            is Token.LParen -> {
                parenCounter++
            }
        }
        result.add(token)
        i++
    }
    val rest = tokens.slice(i until tokens.count())
    if(rest.count() <= 2) {
        // かっこだけの時
        return listOf(tokens[1]) to listOf()
    } else {
        return result to listOf(Token.LParen) + tokens.slice(i until tokens.count())
    }
}

fun cdr(tokens: List<Token>): Pair<List<Token>, List<Token>>{
    if(tokens.count() < 3 && tokens.first() != Token.LParen) {
        throw Error("cdrエラー $tokens")
    }
    val restTokens = car(tokens).second
    if(restTokens.count() == 0) {
        return listOf(Token.LParen, Token.RParen) to listOf()
    }
    // (a b c)ならrestTokensは(b c)

    var parenCounter = 1
    var result = mutableListOf<Token>(Token.LParen)
    var i = 1// restTokensは(b c)。bから始める
    while(i < tokens.count() && parenCounter != 0) {
        val token = restTokens[i]
        when(token) {
            is Token.RParen -> {
                parenCounter--
            }
            is Token.LParen -> {
                parenCounter++
            }
        }
        result.add(token)
        i++
    }
    return result to restTokens.slice(i until restTokens.count())
}

fun cadr(tokens: List<Token>): Pair<List<Token>, List<Token>>{
    val cdrTokens = cdr(tokens)
    val cadr = car(cdrTokens.first)
    return cadr.first to cadr.second + cdrTokens.second
}

fun cddr(tokens: List<Token>): Pair<List<Token>, List<Token>> {
    val cdr = cdr(tokens)
    return cdr(cdr.first).first to cdr.second
}

fun cdddr(tokens: List<Token>): Pair<List<Token>, List<Token>> {
    val cddr = cddr(tokens)
    return cdr(cddr.first).first to cddr.second
}

fun caddr(tokens: List<Token>): Pair<List<Token>, List<Token>> {
    val cddr = cddr(tokens)
    val caddr = car(cddr.first)
    return caddr.first to caddr.second + cddr.second
}

fun cadddr(tokens: List<Token>): Pair<List<Token>, List<Token>> {
    val cdddr = cdddr(tokens)
    val cadddr = car(cdddr.first)
    return cadddr.first to cadddr.second + cdddr.second
}


fun is_self_evaluating(tokens: List<Token>): Boolean {
    val first = tokens.first()
    return first is Token.Num || first is Token.Var
}

fun is_pair(tokens: List<Token>): Boolean {
    return tokens.first() is Token.LParen
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


//fun parseIf(tokens: List<Token>): Pair<SExp.If, List<Token>> {
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