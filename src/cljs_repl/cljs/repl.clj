(ns cljs-repl.cljs.repl
  (:require [com.stuartsierra.component :as component]
            [cljs.repl.rhino :as rhino]
            [cljs.repl :as repl]
            [clojure.tools.reader.reader-types :as readers])
  (:import [java.io PipedInputStream PipedInputStream OutputStream PrintWriter]))

(defn start-repl
  [{:keys [reader print print-no-newline prompt quit-prompt flush]}]
  (future
    (repl/repl (rhino/repl-env)
               :reader reader
               :print print
               :prompt prompt
               :flush flush
               :quit-prompt quit-prompt
               :print-no-newline print-no-newline)))

(defn tear-down
  [{:keys [input output output-writer]}]
  (doseq [item (into [output-writer] (concat (vals input) (vals output)))]
    (.close item)))

(defn write-repl
  [repl msg]
  (doto (get-in repl [:input :writer])
    (.print msg)
    (.flush)))

(defn ->piped-pair
  [buffer-size]
  (let [is (java.io.PipedInputStream. buffer-size)]
    {:read is
     :write (java.io.PipedOutputStream. is)}))

(defn repl-reader
  [input-stream]
  (fn []
    (readers/source-logging-push-back-reader
     (readers/input-stream-push-back-reader input-stream)
     1 "NO_SOURCE_FILE")))

(defrecord CLJSRepl
    []
  component/Lifecycle
  (start [{:keys [buffer-size] :as this}]
    (let [input (->piped-pair buffer-size)
          output (->piped-pair buffer-size)
          output-writer (java.io.PrintWriter. (:write output) true)
          reader (repl-reader (:read input))
          writern (fn [args] (.println output-writer args))
          writer (fn [args] (.print output-writer args))
          repl (start-repl {:reader reader
                            :print writern
                            :prompt #(binding [*out* output-writer]
                                       (repl/repl-prompt))
                            :quit-prompt #(binding [*out* output-writer]
                                            (repl/repl-quit-prompt))
                            :flush #(.flush output-writer)
                            :print-no-newline writer})]
      (assoc this
             :input (assoc input :writer (java.io.PrintWriter. (:write input) true))
             :output output
             :output-writer output-writer
             :reader reader
             :writern writern
             :writer writer
             :repl repl)))

  (stop [this]
    (write-repl this ":cljs/quit\n")
    (println "repl output:" @(:repl this))
    (tear-down this)
    (reduce #(assoc %1 %2 nil)
            this
            (keys this))))

(defn ->CLJSRepl
  ([] (->CLJSRepl (* 10 1024)))
  ([buffer-size]
   (map->CLJSRepl {:buffer-size buffer-size})))

(comment
  (do
    (def repl (component/start (->CLJSRepl)))
    (def repl-output (future (slurp (get-in repl [:output :read])))))

  (write-repl repl "(+ 1 1)\n")
  (write-repl repl "(enable-console-print!)")
  (write-repl repl "(println \"hi!\")")

  (.close (get-in repl [:output :read]))
  (component/stop repl)
  @repl-output
  
  
  )
