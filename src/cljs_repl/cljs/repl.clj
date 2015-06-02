(ns cljs-repl.cljs.repl
  (:require [com.stuartsierra.component :as component]
            [cljs.repl.rhino :as rhino]
            [cljs.repl :as repl]
            [clojure.tools.reader.reader-types :as readers])
  (:import [java.util.concurrent ArrayBlockingQueue SynchronousQueue]))

(defn write-repl
  [repl code]
  (let [result (promise)]
    (.put (:eval-queue repl) [code result])
    result))

(defn repl-reader
  [eval-queue result-queue]
  (fn []
    (let [[code return] (.take eval-queue)]
      (.put result-queue return)
      (readers/source-logging-push-back-reader
       (readers/string-push-back-reader code)
       1 "NO_SOURCE_FILE"))))

(defrecord CLJSRepl
    [eval-queue return-queue]
  component/Lifecycle
  (start [{:keys [buffer-size] :as this}]
    (let [reader (repl-reader eval-queue return-queue)
          error (fn [throwable js-env opts]
                  (let [p (.take return-queue)]
                    (deliver p throwable)))
          print (fn [arg] (let [p (.take return-queue)]
                           (deliver p arg)))]
      (assoc this
             :repl
             (future
               (repl/repl (rhino/repl-env)
                          :reader reader
                          :print print
                          :print-no-newline print
                          :caught error
                          :prompt (constantly :no-op)
                          :quit-prompt (constantly :no-op)
                          :flush (constantly :no-op))))))

  (stop [this]
    (when-not (realized? (:repl this))
      ;; Tell the cljs repl to stop.

      ;; The returned promise should nvr be written to, because the
      ;; repl will terminate after reading rather than continuing on
      ;; to evaluation.
      (write-repl this ":cljs/quit")

      ;; wait for the repl thread to terminate
      @(:repl this))

    (assoc this
           :eval-queue nil
           :return-queue nil)))

(defn ->CLJSRepl
  ([] (->CLJSRepl 1024))
  ([queue-size]
   (map->CLJSRepl {:eval-queue (SynchronousQueue.)
                   :return-queue (ArrayBlockingQueue. queue-size)})))

(comment
  (def repl (component/start (->CLJSRepl)))

  @(write-repl repl "(+ 1 1)")
  @(write-repl repl "(load-file \"./test/test_files/test.cljs\")")
  (let [result @(write-repl repl "(test/test-fn 3)")]
    (if (instance? Throwable result)
      (.getMessage result)
      result))
  (let [result @(write-repl repl "(test/boo-fn 3)")]
    (if (instance? Throwable result)
      (.getMessage result)
      result))
  @(write-repl repl "(enable-console-print!)")
  (let [result @(write-repl repl "(println \"hi!\")")]
    (if (instance? Throwable result)
      (.getMessage result)
      result))

  (component/stop repl)
  
  
  )
