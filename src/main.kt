class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {

            val testCode00 = """
                (if (= a 2)
                    a
                    2)
                """.trimIndent()
            println(parseNodeList(tokenize(testCode00)))

            val testCode0 = """
                (define x 0)
            """.trimIndent()

            println(parseNodeList(tokenize(testCode0)))

            val testCode1 = """
                (define x (+ 1 1))
            """.trimIndent()

            println(parseNodeList(tokenize(testCode1)))

            val testCode2 = """
                (define (x v1 v2 v3) 1)
            """.trimIndent()

            println(parseNodeList(tokenize(testCode2)))

            val testCode3 = """
                (define (x v1 v2 v3) (+ 1 1))
                (+ 1 2 3)
            """.trimIndent()

            println(parseNodeList(tokenize(testCode3)))
        }
    }
}