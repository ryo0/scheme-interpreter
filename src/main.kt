import java.io.File

fun interpret(code: String): String {
    val stdlib = File("src/scm/stdlib.scm").readText()
    val exp = eval(parseProgram(parseNodeList(tokenize(stdlib + "\n" + code))))
    if (exp == null) {
        return ""
    } else {
        return convertExpToString(exp)
    }
}

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(getQuoteString0("(x + 3) 'x))"))
            println(getQuoteString("(x + 3) 'x))"))
            println(getQuoteString("x))"))
            println(getQuoteString("((x + 1 + (2)))nyaa"))
            println(getQuoteString("xxxx))"))
            println("^^^^^^^^^^^^^^^^^^^")
//            val testCode00 = """
//                (if #t
//                    (/ 1 3 2 2)
//                    (* 6 6 6 2))
//                """.trimIndent()
//
//            val nodes0 = parseNodeList(tokenize(testCode00))
//            println(eval(parseProgram(nodes0)))
//
//            val testCode01 = """
//                (if #t
//                    (- 3 1 1 1 3)
//                    (- 1 1))
//                """.trimIndent()
//
//            val nodes01 = parseNodeList(tokenize(testCode01))
//            println(eval(parseProgram(nodes01)))
//
//            val testCode02 = """
//                (car '(1 2 3))
//                """.trimIndent()
//
//            val nodes02 = parseNodeList(tokenize(testCode02))
//            println(eval(parseProgram(nodes02)))
//
//            val testCode03 = """
//                (cdr '(1 2 3))
//                """.trimIndent()
//
//            val nodes03 = parseNodeList(tokenize(testCode03))
//            println(eval(parseProgram(nodes03)))
//
//            val testCode04 = """
//                (cons 0 '(1 2 3))
//                """.trimIndent()
//
//            val nodes04 = parseNodeList(tokenize(testCode04))
//            println(eval(parseProgram(nodes04)))
//
//            val testCode05 = """
//                (cons '(0 1) '(2 3))
//                """.trimIndent()
//
//            val nodes05 = parseNodeList(tokenize(testCode05))
//            println(eval(parseProgram(nodes05)))
//
//            val testCode06 = """
//                (define x (+ 0 1))
//                (define y (+ 1 1))
//                (define z (+ 1 2))
//                (cons x (cons y (cons z '())))
//                """.trimIndent()
//
//            val nodes06 = parseNodeList(tokenize(testCode06))
//            println(eval(parseProgram(nodes06)))
//
//            val testCode07 = """
//                (define x3 (+ 1 1 1))
//                (define (plus1 x) (+ x 1))
//                (plus1 x3)
//                """.trimIndent()

//            val nodes07 = parseNodeList(tokenize(testCode07))
//            println(eval(parseProgram(nodes07)))

//            val testCode08 = """
//                (define (len lst)
//                    (if (null? lst)
//                    0
//                    (+ 1 (len (cdr lst))))
//                )
//                (print '(1 2 (3 4) 5 6 (7 ((8)))))
//                (print (len '(1.0 2.0 (3.0 4.0) 5.0 6.0 (7.0 ((8.0))))))
//                (print (null? 'x))
//                (print (null? '(1)))
//                (print (null? '()))
//                """.trimIndent()
//
//            interpret(testCode08)

//            val testCode09 = """
//                (print (eq? '(- 3 2) '(- 3 2)))
//                """.trimIndent()
//
//            interpret(testCode09)
//
//            val testCode10 = """
//                (print (and #f (error "aaa")))
//                (print (and #t #f (error "bbb")))
//                (cond (#t "aaa") (else (error "bbb")))
//                """.trimIndent()
//
//            interpret(testCode10)
//
            val testCode11 = """
                (print (cdr '((1))))
                """.trimIndent()

            interpret(testCode11)

            val deriv = File("src/scm/deriv.scm").readText()
            interpret(deriv)

//            1.0
//            'y
//            ((x * y) + (y * (x + 3.0)))
//            4.0
//            13.0
//            (1.0 + (3.0 + (1.0 + y)))
//            (((x * (3.0 * (x + x))) + (3.0 * x * x)) + ((y * y) + y))
//            (1.0 + (1.0 + (((x * (3.0 * y * 5.0)) + (x * 3.0 * y * 5.0)) + (x + x))))
//            (1.0 + ((3.0 * (x + x)) + (y + 2.0)))
//            (1.0 + ((3.0 * (x + x)) + (1.0 + ((x + x) + 1.0))))
//            (1.0 + ((3.0 * (x + x)) + (1.0 + ((x * (((x + x) * y) + (2.0 * (y * x)))) + ((x + x) * y * x)))))
//            (x * ((x + x) * x))
//
            val tree = File("src/scm/tree.scm").readText()
            interpret(tree)
//
//            (7.0 (3.0 (1.0 () ()) (5.0 () ())) (9.0 () (11.0 () ())))
//            (3.0 (1.0 () ()) (7.0 (5.0 () ()) (9.0 () ())))
//            (5.0 (3.0 (1.0 () ()) ()) (9.0 (7.0 () ()) (11.0 () ())))
//            (1.0 3.0 5.0 7.0 9.0 11.0)
//            (1.0 3.0 5.0 7.0 9.0)
//            (1.0 3.0 5.0 7.0 9.0 11.0)
//            (1.0 3.0 5.0 7.0 9.0 11.0)
//            (1.0 3.0 5.0 7.0 9.0)
//            (1.0 3.0 5.0 7.0 9.0 11.0)
//
//            val testCode11 = """
//                (let ((x (+ 1 2)) (y (+ 3 4))) (+ x y))
//                """.trimIndent()
//
//            val nodes11 = parseNodeList(tokenize(testCode11))
//            println(eval(parseProgram(nodes11)))
////
//            val testCode12 = """
//                (begin #t #f 1)
//                """.trimIndent()
//
//            val nodes12 = parseNodeList(tokenize(testCode12))
//            println(eval(parseProgram(nodes12)))
//
//            val huffman = File("src/scm/huffman.scm").readText()
//            interpret(huffman)
//
//            val bank = File("src/scm/bank.scm").readText()
//            interpret(bank)
//
//            val testCode01 = """
//                (print ((lambda (a) a) "b"))
//                """.trimIndent()
//
//            interpret(testCode01)

//            val testCode0 = """
//                (define x 0)
//            """.trimIndent()
//
//            val node2 = parseNodeList(tokenize(testCode0))
//            println(parseProgram(node2))
//
//            val testCode1 = """
//                (define x (+ 1 1))
//            """.trimIndent()
//
//            val node3 = parseNodeList(tokenize(testCode1))
//            println(parseProgram(node3))
//
//            val testCode2 = """
//                (define (x v1 v2 v3) 1)
//            """.trimIndent()
//
//            val node4 = parseNodeList(tokenize(testCode2))
//            println(parseProgram(node4))
//
//            val testCode3 = """
//                 (define x 1)
//                (+ 2 1)
//            """.trimIndent()
//
//            val node5 = parseNodeList(tokenize(testCode3))
//            println(parseProgram(node5))
//
//            val testCode4 = """
//                (define (x v1 v2 v3) (+ 1 (* 2 3)))
//            """.trimIndent()
//            val node6 = parseNodeList(tokenize(testCode4))
//            println(parseProgram(node6))
//
//            val testCode5 = """
//                (lambda (v1 v2 v3) (+ 1 (* 2 3)))
//            """.trimIndent()
//            val node7 = parseNodeList(tokenize(testCode5))
//            println(parseProgram(node7))
//
//            val testCode6 = """
//                ((lambda (v1 v2 v3) (+ 1 (* 2 3))) 1 2 3)
//            """.trimIndent()
//            val node8 = parseNodeList(tokenize(testCode6))
//            println(parseProgram(node8))
//
//            val testCode9 = """
//                (quote (1 2 3))
//            """.trimIndent()
//            val node9 = parseNodeList(tokenize(testCode9))
//            println(parseProgram(node9))
//
//            val testCode10 = """
//                (cond ((= a (+ 1 2)) 1)
//                    (b 2)
//            (else #f))
//            """.trimIndent()
//            val node10 = parseNodeList(tokenize(testCode10))
//            println(parseProgram(node10))
//
            val test = """
;;;; クォート式は (quote <text-of-quotation>) の形
(define (a b)
  (c exp 'quote))
(define (text-of-quotation exp) (cadr exp))
            """.trimIndent()
            println(tokenize(test))
            val node11 = parseNodeList(tokenize(test))
            println(node11)
            println(parseProgram(node11))

            val test12 = """
                (define (a x) 2) (define (b y) 4)
            """.trimIndent()
            val node12 = parseNodeList(tokenize(test12))
            println(node12)
            println(parseProgram(node12))

            val test14 = """
(define (mul-interval x y)
  (let ((p1 (* (lower-bound x) (lower-bound y)))
        (p2 (* (lower-bound x) (upper-bound y)))
        (p3 (* (upper-bound x) (lower-bound y)))
        (p4 (* (upper-bound x) (upper-bound y))))
    (make-interval (min p1 p2 p3 p4)
                   (max p1 p2 p3 p4)))
            """.trimIndent()
            println(tokenize(test14))
            val node14 = parseNodeList(tokenize(test14))
            println(node14)
            println(parseProgram(node14))

            val test15 = """
                (begin (+ 1 2) (+ 3 4) 5)
            """.trimIndent()
            println(tokenize(test15))
            val node15 = parseNodeList(tokenize(test15))
            println(node15)
            println(parseProgram(node15))
            println(getQuoteString("((())))hoge"))
        }
    }
}
