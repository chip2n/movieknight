(ns app.system)

(defmacro defcomponent [name {:keys [input handler]}]
  (let [exit-ch (gensym 'exit-fn)
        input-ch (gensym 'input-fn)
        v (gensym 'v)
        ch (gensym 'ch)]
    `(defn ~name []
       (println "[test-component] starting")
       (let [~exit-ch (clojure.core.async/chan)
             ~input-ch ~input]
         (clojure.core.async/go-loop []
           (let [[~v ~ch] (clojure.core.async/alts! [~exit-ch ~input-ch])]
             (when (not (= ~ch ~exit-ch))
               (do
                 (~handler ~v)
                 (recur)))))
         (fn stop! []
           (println "[test-component] stopping")
           (clojure.core.async/put! ~exit-ch :stop))))))
