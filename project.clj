(defproject cljs-repl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.clojure/tools.reader "0.9.2"]
                 [com.stuartsierra/component "0.2.3"]]

  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[org.clojure/tools.namespace "0.2.10"]]}})
