class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val testcode1111 = """
(define (fixed-point f first-guess)
  (define (close-enough? v1 v2)
    (< (abs (- v1 v2)) tolerance))
  (define (try guess)
    (let ((next (f guess)))
      (if (close-enough? guess next)
          next
          (try next))))
  (try first-guess))
  """

            val s = tokenize("((a b) c d e) (1 2)")
            println("car")
            printSExpTokens(car(s).first)
            printSExpTokens(car(s).second)
            println("cdr")
            printSExpTokens(cdr(s).first)
            printSExpTokens(cdr(s).second)
            println("cadr")
            printSExpTokens(cadr(s).first)
            printSExpTokens(cadr(s).second)
            println("cddr")
            printSExpTokens(cddr(s).first)
            printSExpTokens(cddr(s).second)
            println("caddr")
            printSExpTokens(caddr(s).first)
            printSExpTokens(caddr(s).second)
            println("cdddr")
            printSExpTokens(cdddr(s).first)
            printSExpTokens(cdddr(s).second)
            println("cadddr")
            printSExpTokens(cadddr(s).first)
            printSExpTokens(cadddr(s).second)
            val testCode00 = """
                (define (x a) a)
                (set! x 2)
            """.trimIndent()
//            println(car(tokenize(testCode00)))
//            println(cdr(tokenize(testCode00)))
//            println(car(cdr(tokenize(testCode00))))
            val testCode0 = """
                (define x 0)
            """.trimIndent()

            val testCode1 = """
                (define x (+ 1 1))
            """.trimIndent()

            val testCode2 = """
                (define (x v1 v2 v3) 1)
            """.trimIndent()

            val testCode3 = """
                (define (x v1 v2 v3) (+ 1 1))
            """.trimIndent()
        }
        fun printSExpTokens(tokens: List<Token>) {
            var result = ""
            tokens.forEach{
                when(it) {
                    is Token.LParen -> {
                        result += "( "
                    }
                    is Token.RParen -> {
                        result += " )"
                    }
                    is Token.Var -> {
                        result += it.name + " "
                    }
                    is Token.Num -> {
                        result += "${it.value} "
                    }
                }
            }
            println(result)
        }
    }
}