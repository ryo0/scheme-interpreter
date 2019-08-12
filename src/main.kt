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
        }
    }
}