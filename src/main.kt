class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val testcode000= """
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
                (if (= a 2)
                    a
                    2)
                """.trimIndent()
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