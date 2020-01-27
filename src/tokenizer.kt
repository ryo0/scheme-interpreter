import java.lang.Exception

sealed class Token {
    data class Num(val value: Float) : Token()
    data class Var(val name: String) : Token()
    data class Str(val name: String) : Token()
    object True : Token()
    object False : Token()
    object LParen : Token()
    object RParen : Token()
    object Plus : Token()
    object Minus : Token()
    object Asterisk : Token()
    object Slash : Token()
    object Equal : Token()
    object Quote : Token()
    object LessThan : Token()
    object GreaterThan : Token()
    object Define : Token()
    object Lambda : Token()
    object Set : Token()
    object Cond : Token()
    object If : Token()
    object Else : Token()
    object Let : Token()
    object Begin : Token()
    object And : Token()
    object Or : Token()

}

val symbolHash = mapOf(
    '+' to Token.Plus,
    '-' to Token.Minus,
    '*' to Token.Asterisk,
    '/' to Token.Slash,
    '\'' to Token.Quote,
    '(' to Token.LParen,
    ')' to Token.RParen
)

val tokenHash = mapOf(
    Token.Plus to '+',
    Token.Minus to '-',
    Token.Asterisk to '*',
    Token.Slash to '/',
    Token.Quote to '\'',
    Token.LParen to '(',
    Token.RParen to ')'
)

val x = tokenHash[Token.Plus]

val keywordHash = mapOf(
    "define" to Token.Define,
    "lambda" to Token.Lambda,
    "set!" to Token.Set,
    "cond" to Token.Cond,
    "if" to Token.If,
    "else" to Token.Else,
    "let" to Token.Let,
    "quote" to Token.Quote,
    "begin" to Token.Begin,
    "and" to Token.And,
    "or" to Token.Or
)

fun removeComments(str: String): String {
    var i = 0
    var result = ""
    var inDoubleQuote = false
    while (i < str.length) {
        val char = str[i]
        if (char == ';' && !inDoubleQuote) {
            val nextLine = str.slice(i until str.length).indexOfFirst { it == '\n' } + 1
            i += nextLine
        } else {
            if (char == '"') {
                inDoubleQuote = !inDoubleQuote
            }
            result += char
            i++
        }
    }
    return result
}

fun tokenize(inputStr: String): List<Token> {
    val str = removeComments(inputStr)
    var i = 0
    val tokens: MutableList<Token> = mutableListOf()
    while (i < str.count()) {
        val char = str[i]
        when (char) {
            in symbolHash.keys -> {
                if (char == '\'') {
                    val strFromIToLast = str.slice(i + 1 until str.length)
                    val quoteString = getQuoteString(strFromIToLast)
                    val quoteExpToken = tokenize(quoteString) // (1 2 3)
                    // (quote (1 2 3))にしたい
                    tokens.add(Token.LParen)
                    tokens.add(Token.Quote)
                    tokens += quoteExpToken
                    tokens.add(Token.RParen)
                    i += quoteString.length + 1 // quoteのぶん+1
                } else {
                    val token = symbolHash[char] ?: throw Error()
                    tokens.add(token)
                    i++
                }
            }
            ' ', '\n' -> {
                i++
            }
            '"' -> {
                val strFromIToLast = str.slice(i + 1 until str.length)
                val endString = strFromIToLast.indexOfFirst { it == '"' }
                val string = strFromIToLast.slice(0 until endString)
                tokens.add(Token.Str(string))
                i += string.length + 2
            }
            '#' -> {
                if (i + 1 < str.length) {
                    val next = str[i + 1]
                    if (next == 't') {
                        tokens.add(Token.True)
                    } else if (next == 'f') {
                        tokens.add(Token.False)
                    } else {
                        throw Error("#を最初につけて良いのは#tか#fだけ ${str.slice(i until str.length)}")
                    }
                    i += 2
                } else {
                    throw Error("プログラム末尾に#単体")
                }
            }
            else -> {
                val strFromIToLast = str.slice(i until str.length)
                val atom = getAtom(strFromIToLast)
                if (atom.filter { !it.isDigit() && it != '.' }.isEmpty()) {
                    if (atom.length >= 2 && atom[0] == '0') {
                        throw Error("読めないトークン: 0始まりの2桁以上の数 $atom")
                    } else {
                        try {
                            tokens.add(Token.Num(atom.toFloat()))
                        } catch (e: Exception) {
                            throw Error("数値に不正な値が渡されている $atom")
                        }
                    }
                } else if (atom in keywordHash.keys) {
                    val keyword = keywordHash[atom] ?: throw Error()
                    tokens.add(keyword)
                } else {
                    tokens.add(Token.Var(atom))
                }
                i += atom.length
            }
        }
    }
    return tokens
}

fun getAtom(str: String): String {
    val i = str.indexOfFirst { it == ' ' || it == '\n' || it == '(' || it == ')' }
    if (i == -1) {
        return str
    }
    return str.slice(0 until i)
}

fun getQuoteString(str: String): String {
    return getQuoteStringIter(str, "", 0)
}

fun getString(str: String): String {
    var i = 0
    var result = ""
    while (i < str.count()) {
        if (str[i] != ' ' && str[i] != '\n' && str[i] != '(' && str[i] != ')') {
            result += str[i]
            i++
        } else {
            return result
        }
    }
    return result
}

fun getQuoteStringIter(str: String, acm: String, counter: Int): String {
    if (str.count() == 0) {
        return acm
    }
    if (acm != "" && counter <= 0) {
        if (acm.last() == ')') {
            return acm
        }
        return acm + getString(str)
    }
    val c = str[0]
    val cdr = str.slice(1 until str.count())
    if (c == '(') {
        return getQuoteStringIter(cdr, acm + c, counter + 1)
    }
    if (c == ')') {
        return getQuoteStringIter(cdr, acm + c, counter - 1)
    }
    return getQuoteStringIter(cdr, acm + c, counter)
}

fun getQuoteString0(str: String): String {
    var i = 0
    var parenCounter = 0
    var result = ""
    loop@ while (true) {
        val char = str[i]
        when (char) {
            '(' -> {
                result += char
                parenCounter++
                i++
            }
            ')' -> {
                parenCounter--
                if (parenCounter < 0) {
                    break@loop
                } else if (parenCounter == 0) {
                    result += char
                    break@loop
                } else {
                    result += char
                }
                i++
            }
            ' ', '\n' -> {
                if (parenCounter != 0) {
                    i++
                    result += " "
                } else {
                    break@loop
                }
            }
            else -> {
                result += char
                i++
            }
        }
    }
    return result
}