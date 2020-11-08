(ns app.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! timeout]]))

(defn do-request []
  (go (let [response (<! (http/get "http://localhost:8000"
                                   {:with-credentials? false
                                    :accept "application/json"}))]
        (prn (:body response)))))

(defn search [query]
  (go (<! (timeout 2000))
      [{:value "chocolate" :label "Chocolate"}
       {:value "strawberry" :label "Strawberry"}
       {:value "vanilla" :label "Vanilla"}]))
