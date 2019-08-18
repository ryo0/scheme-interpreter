(define (length lst)
  (if (null? lst)
    0
    (+ 1 (length (cdr lst))))
)

(define (cadr lst) (car (cdr lst)))

(define (cddr lst) (cdr (cdr lst)))

(define (caddr lst) (car (cddr lst)))

(define (cdddr lst) (cdr (cddr lst)))

(define (cadddr lst) (car (cdddr lst)))

(define nil '())

(define (append lst1 lst2)
  (if (null? lst1)
    lst2
    (cons (car lst1) (append (cdr lst1) lst2))))