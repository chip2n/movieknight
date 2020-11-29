(ns backend.core
  (:require [com.stuartsierra.component :as component]
            [backend.system :as system]))

(defn -main [& args]
  (component/start (system/create)))

(comment
  (def system (system/create))
  (alter-var-root #'system component/start)
  (alter-var-root #'system component/stop))
