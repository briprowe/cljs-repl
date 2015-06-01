(ns cljs-repl.cljs.repl
  (:require [com.stuartsierra.component :as component]
            [cljs.repl.rhino :as rhino]
            [cljs.repl :as repl]
            [clojure.tools.reader.reader-types :as readers])
  (:import [java.io PipedInputStream PipedInputStream OutputStream PrintWriter]))

(defn start-repl
  [{:keys [reader print print-no-newline prompt quit-prompt flush]}]
  )

(defn tear-down
  [{:keys [input output]}]
  (doseq [item (concat (vals input) (vals output))]
    (.close item)))

(defn write-repl
  [repl msg]
  (doto (get-in repl [:input :writer])
    (.println msg)
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
          input-writer (java.io.PrintWriter. (:write input) true)
          reader (repl-reader (:read input))
          writern (fn [args] (.println output-writer args))
          writer (fn [args] (.print output-writer args))]
      (assoc this
             :input (assoc input :writer input-writer)
             :output (assoc output :writer output-writer)
             :cljs-reader reader
             :repl
             (future
               (repl/repl (rhino/repl-env)
                          :reader reader
                          :print writern
                          :prompt #(binding [*out* output-writer]
                                     (repl/repl-prompt))
                          :quit-prompt #(binding [*out* output-writer]
                                            (repl/repl-quit-prompt))
                          :flush #(.flush output-writer)
                          :print-no-newline  writer)))))

  (stop [this]
    (write-repl this ":cljs/quit")
    @(:repl this)
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

  (write-repl repl "(+ 1 1)")
  (write-repl repl "(enable-console-print!)")
  (write-repl repl "(println \"hi!\")")

  (component/stop repl)
  @repl-output
  
  
  )
