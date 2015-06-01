(ns user
  (:use [clojure.repl])
  (:require [clojure.tools.namespace.repl :refer [refresh]]))

(defn go
  []
  (require 'dev)
  ((ns-resolve 'dev 'start) {}))

(defn reset
  []
  (require 'dev)
  ((ns-resolve 'dev 'stop) {})
  (refresh :after 'user/go))
