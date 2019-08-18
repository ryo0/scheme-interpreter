;16:36->16:47
;17:03->18:00
(define (entry tree) (car tree))
(define (left-branch tree) (cadr tree))
(define (right-branch tree) (caddr tree))
(define (make-tree entry left right)
  (list entry left right))

(define (element-of-set x set)
    (cond
        ((null? set) #f)
        ((= x (entry set)) true)
        ((> x (entry set))
         (element-of-set x (right-branch set)))
         ((< x (entry set))
          (element-of-set x (left-branch set)))
      )
  )
(define (adjoin-set x set)
  (cond
      ((null? set) (make-tree x nil nil))
      ((= x (entry set)) set)
      ((> x (entry set))
       (make-tree (entry set)
                  (left-branch set)
        (adjoin-set x (right-branch set))))
      ((< x (entry set))
       (make-tree (entry set)
        (adjoin-set x (left-branch set))
        (right-branch set)))
    )
  )

(define (tree->list-1 tree)
    (if (null? tree)
      nil
      (append (tree->list-1 (left-branch tree))
              (cons (entry tree)
                    (tree->list-1
                     (right-branch tree)))))
  )
(define (tree->list-2 tree)
    (define (copy-to-list tree result-list)
      (if (null? tree)
        result-list
        (copy-to-list (left-branch tree)
                      (cons (entry tree)
                            (copy-to-list
                             (right-branch tree)
                             result-list)))))
  (copy-to-list tree '())
  )
(define tree1 (adjoin-set 5 (adjoin-set 1 (adjoin-set 11 (adjoin-set 9 (adjoin-set 3 (make-tree 7 nil nil)))))))
(print tree1)

(define tree2  (adjoin-set 5 (adjoin-set 9 (adjoin-set 7 (adjoin-set 1 (make-tree 3 nil nil))))))
(print tree2)

(define tree3  (adjoin-set 11 (adjoin-set 7 (adjoin-set 9 (adjoin-set 1 (adjoin-set 3 (make-tree 5 nil nil)))))))
(print tree3)

;tree->list-1は、左の木を処理した結果+真ん中+右の木を処理した結果
;とシンプルに計算してるだけ
(print (tree->list-1 tree1))
;(1 3 5 7 9 11)->正解

(print (tree->list-1 tree2))
;(1 3 5 7 9)->正解

(print (tree->list-1 tree3))
;(1 3 5 7 9 11)->正解

;大雑把に、tree->list-2は
;木の真ん中（節）に木の右を処理したものをくっつけつつ、左に進む
;という動きだということがトレースでわかった
;1, 2でどちらも動きは同じ！だと思う。
(print (tree->list-2 tree1))
(tree->list-2 tree1)
;トレース
;= (copy tree1 nil)
;= (copy t(3 1 5) (cons 7 (copy t(9 nil 11) nil)))
;  (copy t(9 nil 11) nil)
;  = (copy nil (cons 9 (copy t(11) nil)))
;    (copy 11 nil)
;    = (copy t(nil) (cons 11 (copy t(nil) nil)))
;    = (copy t(nil) (cons 11 nil))
;    = (11)
;  = (copy t(nil) (cons 9 11))
;  = (copy t(nil) (9 11))
;= (copy t(3 1 5) (cons 7 (9 11)))
;= (copy t(3 1 5) (7 9 11))
;= (copy t(1) (cons 3 (copy t(5) (7 9 11))))
;  (copy t(5) (7 9 11))
;  = (copy t(nil) (cons 5 (copy t(nil) (7 9 11))))
;  = (copy t(nil) (cons 5 (7 9 11)))
;  = (copy t(nil) (5 7 9 11))
;  = (5 7 9 11)
;= (copy t(1) (cons 3 (5 7 9 11)))
;= (copy t(1) (3 5 7 9 11))
;= (copy t(nil) (cons 1 (copy t(nil) (3 5 7 9 11))))
;= (copy t(nil) (1 3 5 7 9 11))
;= (1 3 5 7 9 11)


(print (tree->list-2 tree2))

(print (tree->list-2 tree3))
