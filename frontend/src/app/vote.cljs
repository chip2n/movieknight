(ns app.vote
  (:require [cljs.core.async :as async]
            [app.api :as api]
            [app.state :as state]
            [app.system :as system]))

(defonce request-chan (async/chan))

(defn handle-vote-request [{:keys [id answer]}]
  (state/remove-vote-prompt id))

(defn make-vote-component []
  (system/make-component
   :name "vote-component"
   :input-ch request-chan
   :handler handle-vote-request))
