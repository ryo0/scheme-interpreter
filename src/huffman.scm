(define (entry tree) (car tree))

(define (make-leaf symbol weight)
  (list 'leaf symbol weight))

(define (leaf? object)
  (eq? (car object) 'leaf))

(define (symbols tree)
  (if (leaf? tree)
    (list (symbol-leaf tree))
    (caddr tree)))

(define (weight tree)
  (if (leaf? tree)
    (weight-leaf tree)
    (cadddr tree)))


(define (symbol-leaf x) (cadr x))

(define (weight-leaf x) (caddr x))

(define (make-code-tree left right)
  (list left
    right
    (append (symbols left) (symbols right))
    (+ (weight left) (weight right))))

(define (left-branch tree) (car tree))

(define (right-branch tree) (cadr tree))

;; decoding
(define (decode bits tree)
  (define (decode-1 bits current-branch)
    (if (null? bits)
        '()
      (let ((next-branch
              (choose-branch (car bits) current-branch)))
        (if (leaf? next-branch)
          (cons (symbol-leaf next-branch)
            (decode-1 (cdr bits) tree))
          (decode-1 (cdr bits) next-branch)))))
  (decode-1 bits tree))

(define (choose-branch bit branch)
  (cond ((= bit 0) (left-branch branch))
    ((= bit 1) (right-branch branch))
    (else (error "bad bit -- CHOOSE-BRANCH" bit))))

(define (encode message tree)
  (if (null? message)
    nil
    (append (encode-symbol (car message) tree)
      (encode (cdr message) tree))))

(define (encode-symbol message tree)
  (cond
    ((leaf? tree) nil)
    (else
      (let ((right-symbols (symbols (right-branch tree)))
             (left-symbols (symbols (left-branch tree))))
        (cond
          ((element_of_symbols? message right-symbols)
            (cons 1 (encode-symbol message (right-branch tree))))
          ((element_of_symbols? message left-symbols)
            (cons 0 (encode-symbol message (left-branch tree))))
          (else
            (error "message not found in tree --ENCODE SYMBOL" message))
        ))
    )
  ))
(define (element_of_symbols? message symbols)
  (cond
    ((null? symbols) #f)
    ((equal? message (car symbols)) #t)
    (else
      (element_of_symbols? message (cdr symbols))
    )
  )
)

(define (adjoin-set x set)
  (cond ((null? set) (list x))
    ((< (weight x) (weight (car set))) (cons x set))
    (else (cons (car set)
            (adjoin-set x (cdr set))))))

(define (make-leaf-set pairs)
  (if (null? pairs)
      '()
    (let ((pair (car pairs)))
      (adjoin-set (make-leaf (car pair)
                    (cadr pair))
        (make-leaf-set (cdr pairs))))))


(define (successive-merge set)
  (if (null? (cdr set))
    (car set)
    (successive-merge
      (adjoin-set (make-code-tree
                    (car set)
                    (cadr set))
        (cddr set)))))

(define (generate-huffman-tree pairs)
  (successive-merge (make-leaf-set pairs)))

(define (insert x pairs)
  (cond
    ((null? pairs) (cons x nil))
    ((< (weight x) (weight (car pairs))) (cons x pairs))
    (else
      (cons (car pairs) (insert x (cdr pairs)))
    )
  )
)
(define (insert-sort pairs)
  (cond
    ((null? pairs) nil)
    (else
      (insert (car pairs) (insert-sort (cdr pairs))))
  )
)

(define (my-successive-merge leaves)
  (cond
    ((null? leaves) nil)
    ((= (length leaves) 2)
      (make-code-tree (car leaves) (cadr leaves)))
    (else
      (my-successive-merge
        (insert-sort
          (cons (make-code-tree (car leaves) (cadr leaves)) (cddr leaves))))
    )
  )
)
;変数が保存できればどうにかなると思うが、出来ないので思いつかない…
;->ネットの解答を見て得た理解を踏まえてリトライ。
(define (my-generate-huffman-tree pairs)
  (my-successive-merge (make-leaf-set pairs)))

(define huffman-tree (generate-huffman-tree '((A 1) (B 2) (C 4) (D 8) (E 16))))
(print (encode '(A) huffman-tree))

(print (encode '(B) huffman-tree))

(print (encode '(C) huffman-tree))

(print (encode '(D) huffman-tree))

(print (encode '(E) huffman-tree))


(define huffman-tree2 (generate-huffman-tree '((A 1) (B 2) (C 4) (D 8) (E 16) (F 32) (G 64) (H 128) (I 256) (J 512))))
(print (encode '(A) huffman-tree2))

(print (encode '(B) huffman-tree2))

(print (encode '(C) huffman-tree2))

(print (encode '(D) huffman-tree2))

(print (encode '(E) huffman-tree2))

(print (encode '(F) huffman-tree2))

(print (encode '(G) huffman-tree2))

(print (encode '(H) huffman-tree2))

(print (encode '(I) huffman-tree2))

(print (encode '(J) huffman-tree2))


(define huffman-tree (generate-huffman-tree '((B 2) (A 1) (E 16) (D 8) (C 4))))
(print huffman-tree)

(define (encode message tree)
  (if (null? message)
    nil
    (append (encode-symbol (car message) tree)
      (encode (cdr message) tree))))

(define (encode-symbol message tree)
  (cond
    ((leaf? tree) nil)
    (else
      (let ((right-symbols (symbols (right-branch tree)))
             (left-symbols (symbols (left-branch tree))))
        (cond
          ((element_of_symbols? message right-symbols)
            (cons 1 (encode-symbol message (right-branch tree))))
          ((element_of_symbols? message left-symbols)
            (cons 0 (encode-symbol message (left-branch tree))))
          (else
            (error "message not found in tree --ENCODE SYMBOL" message))
        ))
    )
  ))
(define (element_of_symbols? message symbols)
  (cond
    ((null? symbols) #f)
    ((equal? message (car symbols)) #t)
    (else
      (element_of_symbols? message (cdr symbols))
    )
  )
)
