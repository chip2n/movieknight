(ns app.system
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.core.async :as async]
            [app.utils :as utils]))

(defonce system (atom nil))

(defn make-system [components]
  {:start (fn start! []
            (println "[system] starting")
            (doseq [component components]
              ((:start component))))
   :stop (fn stop! []
           (println "[system] stopping")
           (doseq [component components]
             ((:stop component))))})

(defn make-component [& {:keys [name input-ch handler]}]
  (let [exit-ch (async/chan)]
    {:start (fn start! []
              (println (str "[" name "] starting"))
              (async/go-loop []
                (let [[v ch] (async/alts! [exit-ch input-ch])]
                  (when (not (= ch exit-ch))
                    (do
                      (handler v)
                      (recur))))))
     :stop (fn stop! []
             (println (str "[" name "] stopping"))
             (async/put! exit-ch :stop))}))

(defn stop! []
  (when-let [s @system]
    ((:stop s))))

(defn start! [components]
  (stop!)
  (let [s (make-system components)]
    (reset! system s)
    ((:start s))))
