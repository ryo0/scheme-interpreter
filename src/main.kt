class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            println(tokenize("""
                (define x 100)
                (define input-prompt ";;; M-Eval input:")
                (define output-prompt ";;; M-Eval value:")
                (define (expand-clauses clauses)
                    (if (null? clauses)
                    #f
                    (let ((first (car clauses))
                        (rest (cdr clauses)))
                    (if (cond-else-clause? first)
                        (if (null? rest)
                            (sequence->exp (cond-actions first))
                            (error "ELSE clause isn't last -- COND->IF"
                        clauses))
                        (make-if (cond-predicate first)
                            (sequence->exp (cond-actions first))
                            (expand-clauses rest))))))
            """.trimIndent()))

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
            val testCode00 = """
                (define (x a) a)
                (set! x 2)
            """.trimIndent()
            println(tokenize(testCode00))
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