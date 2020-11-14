(ns app.main
  (:require [app.state :as state]
            [app.api :as api]
            [app.search :as search]
            [app.system :as system]
            [app.ui :as ui]
            [app.vote :as vote]
            [app.utils :as utils]
            [cljs.core.async :as async :refer [pipeline]]
            [clojure.spec.alpha :as spec]))

(spec/check-asserts true)

(def test-pub (async/pub ui/event-chan :type))
(async/sub test-pub :search search/request-chan)
(async/sub test-pub :vote vote/request-chan)

(defn main! []
  (println "[main]: reloaded")
  (system/start! [(search/make-search-component)
                  (vote/make-vote-component)])
  (ui/render-app))

(defn ^:dev/before-load stop []
  (system/stop!))

(defn ^:dev/after-load start []
  (main!))
