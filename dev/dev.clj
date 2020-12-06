(ns dev
  (:require
   [backend.system :as system]
   [backend.db :as db]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl
    :refer [reset set-init start stop system]]))

(defn db [] (:database system))

(set-init (fn [_] (system/create)))
