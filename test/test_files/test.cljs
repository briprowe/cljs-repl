(ns test)

(defn test-fn
  [num]
  (+ 1 num))

(defn broken-fn
  [num]
  (+ 1 numf))
