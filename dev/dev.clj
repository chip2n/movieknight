(ns dev
  (:require [backend.system :as system]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component.repl :refer [set-init system]]))

(defn db [] (:database system))

(set-init (fn [_] (system/create)))

(s/check-asserts true)
