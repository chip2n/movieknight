(ns app.system
  (:require-macros [app.system]
                   [cljs.core.async.macros :refer [go]])
  (:require [clojure.core.async :as async]
            [app.utils :as utils]))

(defonce system (atom nil))

(defn make-system! [components]
  (fn stop! []
    (println "[system] stopping")
    (doseq [component components]
      (component))))

(defn make-component [& {:keys [name input-ch handler]}]
  (println (str "[" name "] starting"))
  (let [exit-ch (async/chan)]
    (async/go-loop []
      (let [[v ch] (async/alts! [exit-ch input-ch])]
        (when (not (= ch exit-ch))
          (do
            (handler v)
            (recur)))))
    (fn stop! []
      (println (str "[" name "] stopping"))
      (async/put! exit-ch :stop))))

(defn stop! []
  (when-let [stop-system! @system]
    (stop-system!)))

(defn start! [components]
  (stop!)
  (println "[system] starting")
  (reset! system (make-system! components)))
