class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(getAtom("()))"))
            println(getAtom("(a)b))"))
            println(getAtom("ab))"))
            println(getAtom("(a(b)))"))

            val testCode00 = """
                (if (= a 2)
                    a
                    2)
                """.trimIndent()
//
            val nodes0 = parseNodeList(tokenize(testCode00))
            println(nodes0)
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

            val testCode5 = """
                (lambda (v1 v2 v3) (+ 1 (* 2 3)))
            """.trimIndent()
            val node7 = parseNodeList(tokenize(testCode5))
            println(parseProgram(node7))

            val testCode6 = """
                ((lambda (v1 v2 v3) (+ 1 (* 2 3))) 1 2 3)
            """.trimIndent()
            val node8 = parseNodeList(tokenize(testCode6))
            println(parseProgram(node8))

            val testCode9 = """
                (quote (1 2 3))
            """.trimIndent()
            val node9 = parseNodeList(tokenize(testCode9))
            println(parseProgram(node9))

            val testCode10 = """
                (cond ((= a (+ 1 2)) 1)
                    (b 2)
            (else #f))
            """.trimIndent()
            val node10 = parseNodeList(tokenize(testCode10))
            println(parseProgram(node10))

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

            val test13 = """
;;;; _apply の定義
(define (_apply procedure arguments)
  (cond ((primitive-procedure? procedure)
         (apply-primitive-procedure procedure arguments))
        ((compound-procedure? procedure)
         (eval-sequence
           (procedure-body procedure)
           (extend-environment
             (procedure-parameters procedure)
             arguments
             (procedure-environment procedure))))
        (else
          (error
            "Unknown procedure type -- APPLY" procedure))))


;;;; _eval の定義
(define (_eval exp env)
  (cond ((self-evaluating? exp) exp)
        ((variable? exp) (lookup-variable-value exp env))
        ((quoted? exp) (text-of-quotation exp))
        ((assignment? exp) (eval-assignment exp env))
        ((definition? exp) (eval-definition exp env))
        ((if? exp) (eval-if exp env))
        ((lambda? exp)
         (make-procedure (lambda-parameters exp)
                         (lambda-body exp)
                         env))
        ((begin? exp)
         (eval-sequence (begin-actions exp) env))
        ((cond? exp) (_eval (cond->if exp) env))
        ((application? exp)
         (_apply (_eval (operator exp) env)
                (list-of-values (operands exp) env)))
        (else
          (error "Unknown expression type -- EVAL" exp))))


;;;; 手続きの引数
(define (list-of-values exps env)
  (if (no-operands? exps)
      '()
      (cons (_eval (first-operand exps) env)
            (list-of-values (rest-operands exps) env))))

;;;; 条件式
(define (eval-if exp env)
  (if (true? (_eval (if-predicate exp) env))
      (_eval (if-consequent exp) env)
      (_eval (if-alternative exp) env)))

;;;; 並び
(define (eval-sequence exps env)
  (cond ((last-exp? exps) (_eval (first-exp exps) env))
        (else (_eval (first-exp exps) env)
              (eval-sequence (rest-exps exps) env))))

;;;; 代入と定義
(define (eval-assignment exp env)
  (set-variable-value! (assignment-variable exp)
                       (_eval (assignment-value exp) env)
                       env)
  'ok)

(define (eval-definition exp env)
  (define-variable! (definition-variable exp)
                    (_eval (definition-value exp) env)
                    env)
  'ok)

;;;; 4.1.2 式の表現

;;;; 自己評価式は数と文字だけ
(define (self-evaluating? exp)
  (cond ((number? exp) #t)
        ((string? exp) #t)
        (else #f)))

;;;; 変数は記号で表現
(define (variable? exp) (symbol? exp))

;;;; クォート式は (quote <text-of-quotation>) の形
(define (quoted? exp)
  (tagged-list? exp 'quote))

(define (text-of-quotation exp) (cadr exp))

(define (tagged-list? exp tag)
  (if (pair? exp)
      (eq? (car exp) tag)
      #f))
;;;; 代入は (set! <var> <value>) の形
(define (assignment? exp)
  (tagged-list? exp 'set!))

(define (assignment-variable exp) (cadr exp))

(define (assignment-value exp) (caddr exp))

;;;; 定義
(define (definition? exp)
  (tagged-list? exp 'define))

(define (definition-variable exp)
  (if (symbol? (cadr exp))
      (cadr exp)
      (caadr exp)))

(define (definition-value exp)
  (if (symbol? (cadr exp))
      (caddr exp)
      (make-lambda (cdadr exp)      ; 仮パラメタ
                   (cddr exp))))    ; 本体

;;;; lambda 式は記号 lambda で始まるリスト
(define (lambda? exp) (tagged-list? exp 'lambda))

(define (lambda-parameters exp) (cadr exp))

(define (lambda-body exp) (cddr exp))

(define (make-lambda parameters body)
  (cons 'lambda (cons parameters body)))

;;;; 条件式
(define (if? exp) (tagged-list? exp 'if))

(define (if-predicate exp) (cadr exp))

(define (if-consequent exp) (caddr exp))

(define (if-alternative exp)
  (if (not (null? (cdddr exp)))
      (cadddr exp)
      #f))

(define (make-if predicate consequent alternative)
  (list 'if predicate consequent alternative))

;;;; begin
(define (begin? exp) (tagged-list? exp 'begin))

(define (begin-actions exp) (cdr exp))

(define (last-exp? seq) (null? (cdr seq)))

(define (first-exp seq) (car seq))

(define (rest-exps seq) (cdr seq))

(define (sequence->exp seq)
  (cond ((null? seq) seq)
        ((last-exp? seq) (first-exp seq))
        (else (make-begin seq))))

(define (make-begin seq) (cons 'begin seq))

;;;; 手続き作用
(define (application? exp) (pair? exp))

(define (operator exp) (car exp))

(define (operands exp) (cdr exp))

(define (no-operands? ops) (null? ops))

(define (first-operand ops) (car ops))

(define (rest-operands ops) (cdr ops))

;;;; cond 式
(define (cond? exp) (tagged-list? exp 'cond))

(define (cond-clauses exp) (cdr exp))

(define (cond-else-clause? clause)
  (eq? (cond-predicate clause) 'else))

(define (cond-predicate clause) (car clause))

(define (cond-actions clause) (cdr clause))

(define (cond->if exp)
  (expand-clauses (cond-clauses exp)))

(define (cond-=>-clause? clause)
  (eq? (cadr clause) '=>))

(define (cond-recipient clause)
  (caddr clause))

(define (cond-test clause)
  (car clause))

(define (expand-clauses clauses)
  (if (null? clauses)
      #f
      (let ((first (car clauses))
            (rest (cdr clauses)))
           (cond
              ((cond-else-clause? first)
               (if (null? rest)
                   (sequence->exp (cond-actions first))
                   (error "ELSE clause isn't last -- COND->IF"
                          clauses)))
              ((cond-=>-clause? first)
                (make-if (cond-test first)
                  (list (cond-recipient first) (cond-test first))
                  (expand-clauses rest)))
               (else (make-if (cond-predicate first)
                        (sequence->exp (cond-actions first))
                        (expand-clauses rest)))))))

;;;; 4.1.3 評価器のデータ構造

;;;; 述語のテスト
(define (true? x)
  (not (eq? x #f)))

(define (false? x)
  (eq? x #f))

;;;; 手続きの表現
(define (make-procedure parameters body env)
  (list 'procedure parameters body env))

(define (compound-procedure? p)
  (tagged-list? p 'procedure))

(define (procedure-parameters p) (cadr p))

(define (procedure-body p) (caddr p))

(define (procedure-environment p) (cadddr p))

;;;; 環境に対する操作
(define (enclosing-environment env) (cdr env))

(define (first-frame env) (car env))

(define the-empty-environment '())

(define (make-frame variables values)
  (cons variables values))

(define (frame-variables frame) (car frame))

(define (frame-values frame) (cdr frame))

(define (add-binding-to-frame! var val frame)
  (set-car! frame (cons var (car frame)))
  (set-cdr! frame (cons val (cdr frame))))

(define (extend-environment vars vals base-env)
  (if (= (length vars) (length vals))
      (cons (make-frame vars vals) base-env)
      (if (< (length vars) (length vals))
          (error "Too many arguments supplied" vars vals)
          (error "Too few arguments supplied" vars vals))))

(define (lookup-variable-value var env)
  (define (env-loop env)
    (define (scan vars vals)
      (cond ((null? vars)
             (env-loop (enclosing-environment env)))
            ((eq? var (car vars))
             (car vals))
            (else (scan (cdr vars) (cdr vals)))))
    (if (eq? env the-empty-environment)
        (error "Unbound variable" var)
        (let ((frame (first-frame env)))
             (scan (frame-variables frame)
                   (frame-values frame)))))
  (env-loop env))

(define (set-variable-value! var val env)
  (define (env-loop env)
    (define (scan vars vals)
      (cond ((null? vars)
             (env-loop (enclosing-environment env)))
            ((eq? var (car vars))
             (set-car! vals val))
            (else (scan (cdr vars) (cdr vals)))))
    (if (eq? env the-empty-environment)
        (error "Unbound variable -- SET!" var)
        (let ((frame (first-frame env)))
             (scan (frame-variables frame)
                   (frame-values frame)))))
  (env-loop env))

(define (define-variable! var val env)
  (let ((frame (first-frame env)))
       (define (scan vars vals)
         (cond ((null? vars)
                (add-binding-to-frame! var val frame))
               ((eq? var (car vars))
                (set-car! vals val))
               (else (scan (cdr vars) (cdr vals)))))
       (scan (frame-variables frame)
             (frame-values frame))))

;;;; 4.1.4 評価器をプログラムとして走らせる
(define primitive-procedures
  (list (list 'car car)
        (list 'cdr cdr)
        (list 'cons cons)
        (list 'null? null?)
        (list 'assoc assoc)
        (list 'cadr cadr)
        ;; 基本手続きが続く
        ))

(define (primitive-procedure-names)
  (map car
       primitive-procedures))

(define (primitive-procedure-objects)
  (map (lambda (proc) (list 'primitive (cadr proc)))
       primitive-procedures))

(define (setup-environment)
  (let ((initial-env
          (extend-environment (primitive-procedure-names)
                              (primitive-procedure-objects)
                              the-empty-environment)))
       (define-variable! 'true #t initial-env)
       (define-variable! 'false #f initial-env)
       initial-env))

(define the-global-environment (setup-environment))

(define (primitive-procedure? proc)
  (tagged-list? proc 'primitive))

(define (primitive-implementation proc) (cadr proc))

(define (apply-primitive-procedure proc args)
; これは実装言語側のapply
  (apply
    (primitive-implementation proc) args))


;;;; 基盤の Lisp システムの"読み込み-評価-印字"ループをモデル化する"駆動ループ(driver loop)"を用意する。
(define input-prompt ";;; M-Eval input:")
(define output-prompt ";;; M-Eval value:")

(define (driver-loop)
  (prompt-for-input input-prompt)
  (let ((input (read)))
       (let ((output (_eval input the-global-environment)))
            (announce-output output-prompt)
            (user-print output)))
  (driver-loop))

(define (prompt-for-input string)
  (newline) (newline) (display string) (newline))

(define (announce-output string)
  (newline) (display string) (newline))

(define (user-print object)
  (if (compound-procedure? object)
      (display (list 'compound-procedure
                     (procedure-parameters object)
                     (procedure-body object)
                     '<procedure-env>))
      (display object)))
            """.trimIndent()
            println(tokenize(test13))
            val node13 = parseNodeList(tokenize(test13))
            println(node13)
            println(parseProgram(node13))
        }
    }
}
