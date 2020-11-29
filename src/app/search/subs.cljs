(ns app.search.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :search-results
 (fn [db v] (:search-results db)))
