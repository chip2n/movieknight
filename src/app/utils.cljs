(ns app.utils
  (:require [clojure.spec.alpha :as spec]
            [re-frame.core :as rf]))

(defn validate-db []
  (let [spec :app.db/app-state]
    (rf/->interceptor
     :id :validate-db
     :after (fn [{{:keys [:event :re-frame.std-interceptors/untrimmed-event]} :coeffects
                  {:keys [:db]} :effects :as context}]
              (when (and (spec/check-asserts?) db (not (spec/valid? spec db)))
                (rf/console :log db)
                (throw (js/Error. (str "DB is invalid after event "
                                       (or untrimmed-event event) "\n"
                                       (subs (spec/explain-str spec db) 0 1000)))))
              context))))
