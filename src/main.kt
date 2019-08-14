class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {

            val testCode00 = """
                (if (= a 2)
                    a
                    2)
                """.trimIndent()

            val nodes0 = parseNodeList(tokenize(testCode00))
            println(parseProgram(nodes0))

            val testCode01 = """
                (if (= a 2)
                    (+ a 1))
                """.trimIndent()

            val nodes1 = parseNodeList(tokenize(testCode01))
            println(parseProgram(nodes1))

            val testCode0 = """
                (define x 0)
            """.trimIndent()

            val node2 = parseNodeList(tokenize(testCode0))
            println(parseProgram(node2))

            val testCode1 = """
                (define x (+ 1 1))
            """.trimIndent()

            val node3 = parseNodeList(tokenize(testCode1))
            println(parseProgram(node3))

            val testCode2 = """
                (define (x v1 v2 v3) 1)
            """.trimIndent()

            val node4 = parseNodeList(tokenize(testCode2))
            println(parseProgram(node4))

            val testCode3 = """
                 (define x 1) 
                (+ 2 1)
            """.trimIndent()

            val node5 = parseNodeList(tokenize(testCode3))
            println(parseProgram(node5))

            val testCode4 = """
                (define (x v1 v2 v3) (+ 1 (* 2 3)))
            """.trimIndent()
            val node6 = parseNodeList(tokenize(testCode4))
            println(parseProgram(node6))
        }
    }
}