(ns app.main
  (:require [app.db :as db]
            [app.api :as api]
            [app.search :as search]
            [app.vote :as vote]
            [app.ui :as ui]
            [app.utils :as utils]
            [re-frame.core :as rf]
            [cljs.core.async :as async :refer [pipeline]]
            [clojure.spec.alpha :as spec]))

(spec/check-asserts true)

(defn main! []
  (println "[main]: reloaded")
  (ui/render-app))

(defn init! []
  (rf/dispatch-sync [:init-db])
  (ui/render-app))

(defn ^:dev/after-load start []
  (main!))
