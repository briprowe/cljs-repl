(ns cljs-repl.system
  (:require [com.stuartsierra.component :as component]
            [cljs-repl.nrepl :refer [->nrepl]]))

(def system-spec
  (component/system-map
   :nrepl (->nrepl "127.0.0.1" 55000)))

(defn start
  [system-map]
  (component/start-system system-spec))

(defn stop
  [system]
  (component/stop-system system))
