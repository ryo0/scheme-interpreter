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
            println(car(tokenize("((a b c) d e)")))
            println(cdr(tokenize("((a b c) d e)")))
            val testCode00 = """
                (define (x a) a)
                (set! x 2)
            """.trimIndent()
            println(car(tokenize(testCode00)))
            println(cdr(tokenize(testCode00)))
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
    }
}