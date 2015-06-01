(ns dev
  (:require [com.stuartsierra.component :as component]

            [cljs-repl.start :as cljs-repl]
            [cljs-repl.system :as system]))

(def system nil)

(defn start
  ([] (start system/spec))
  ([system-spec]
   (alter-var-root #'system (constantly (cljs-repl/start system-spec)))))

(defn stop
  ([] system)
  ([running-system]
   (when running-system
     (alter-var-root #'system (constantly (cljs-repl/stop running-system))))))
