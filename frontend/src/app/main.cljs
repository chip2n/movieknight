(ns app.main
  (:require-macros [app.macros :refer [pipe-events]])
  (:require [app.state :as state]
            [app.api :as api]
            [app.search :as search]
            [app.system :as system]
            [app.ui :as ui]
            [app.utils :as utils]
            [cljs.core.async :as async :refer [pipeline]]
            [clojure.spec.alpha :as spec]))

(spec/check-asserts true)

(pipe-events :search search/request-chan)

(defn main! []
  (println "[main]: reloaded")
  (system/start! [(search/make-search-component)])
  (ui/render-app))

(defn ^:dev/before-load stop []
  (system/stop!))

(defn ^:dev/after-load start []
  (main!))
