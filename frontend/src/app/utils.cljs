(ns app.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.core.async :as async :refer [go-loop alts! <! >! close! timeout chan]]))

(defn debounce [in ms]
  (let [out (chan)]
    (go-loop [last-val nil]
      (let [val (if (nil? last-val) (<! in) last-val)
            timer (timeout ms)
            [new-val ch] (alts! [in timer])]
        (condp = ch
          timer (do (when-not
                        (>! out val)
                        (close! in))
                    (recur nil))
          in (if new-val (recur new-val)))))
    out))
