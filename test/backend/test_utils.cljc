(ns backend.test-utils
  (:require [clojure.core.async :refer [<!! take!]]))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 97))))))

(defn test-async
  "Asynchronous test awaiting ch to produce a value or close."
  [ch]
  #?(:clj
     (<!! ch)
     :cljs
     (async done
            (take! ch (fn [_] (done))))))
