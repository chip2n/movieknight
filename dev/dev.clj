(ns dev
  (:require
   [backend.system :as system]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl
    :refer [reset set-init start stop system]]))

(set-init (fn [_] (system/create)))
