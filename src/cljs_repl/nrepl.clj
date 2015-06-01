(ns cljs-repl.nrepl
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.nrepl.server :as nrepl])
  (:import [clojure.tools.nrepl.server Server]))

(defrecord NREPLComponent
    [nrepl-server]
  component/Lifecycle
  (start [{:keys [port address] :as this}]
    (assoc this
           :nrepl-server (nrepl/start-server
                          :port port :bind address)))
  (stop [this]
    (nrepl/stop-server nrepl-server)
    (assoc this :nrepl-server nil)))

(defn ->nrepl
  [address port]
  (map->NREPLComponent
   {:address address
    :port port}))
