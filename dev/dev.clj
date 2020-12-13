(ns dev
  (:require [backend.system :as system]
            [backend.db :as db]
            [clojure.spec.alpha :as s]
            [orchestra.spec.test :as stest]
            [com.stuartsierra.component.repl :refer [set-init system]]))

(defn db [] (:database system))

(set-init (fn [_] (system/create)))

(s/check-asserts true)
