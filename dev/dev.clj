(ns dev
  (:require [com.stuartsierra.component :as component]

            [cljs-repl.system :as cljs-repl]))

(def system nil)

(defn start
  ([] (start cljs-repl/system-spec))
  ([system-spec]
   (alter-var-root #'system (constantly (cljs-repl/start system-spec)))
   :started))

(defn stop
  ([] system)
  ([running-system]
   (when running-system
     (alter-var-root #'system (constantly (cljs-repl/stop running-system))))
   :stopped))
