(ns app.vote
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [chan <! go-loop]]
            [app.api :as api]
            [app.state :as state]
            [app.system :as system]
            [app.utils :as utils]))

(defonce request-chan (chan))

(defn handle-vote-request [{:keys [answer]}]
  (println "voted" answer))

(defn make-vote-component []
  (system/make-component
   :name "vote-component"
   :input-ch request-chan
   :handler handle-vote-request))
