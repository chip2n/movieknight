(ns app.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [chan <! go-loop]]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [app.api :as api]
            [app.utils :as utils]))

;;;; Event handlers

(rf/reg-event-fx
 :search
 (fn [_ [_ query]]
   {:search query}))

(rf/reg-event-db
 :set-search-results
 (fn [db [_ results]]
   (-> db
       (update-in [:movies] merge (into {} (map (fn [x] [(:id x) x]) results)))
       (assoc :search-results (map (fn [x] {:id (:id x) :label (:title x)}) results)))))

;;;; Effect handlers

;; TODO debounce
(rf/reg-fx
 :search
 (fn [query]
   (if (<= (count query) 2)
     (do
       (println "Clearing search results")
       (rf/dispatch [:set-search-results []]))
     (go
       (println "Searching for" query)
       (let [result (<! (api/search query))]
         (rf/dispatch [:set-search-results result]))))))

;;;; Subscriptions

(rf/reg-sub
 :search-results
 (fn [db v] (:search-results db)))

;;;; UI

(defn search-bar []
  (let [state (r/atom {:input "" :expanded false})
        ref (clojure.core/atom nil)
        click-listener (fn [ev] (swap! state assoc :expanded (.contains @ref (.-target ev))))]
    (r/create-class
     {:display-name "search-bar"

      :component-did-mount
      (fn [this]
        (.addEventListener js/document "click" click-listener))

      :component-will-unmount
      (fn [this]
        (.removeEventListener js/document "click" click-listener))

      :reagent-render
      (fn []
        (let [search-results @(rf/subscribe [:search-results])
              {:keys [input expanded]} @state]
          [:div {:style {:position :relative
                         :display :flex
                         :flex-direction :column}
                 :ref (fn [el] (reset! ref el))}
           [:input {:style {:height 32}
                    :value input
                    :on-key-down (fn [ev] (println "key down"))
                    :on-change (fn [ev]
                                 (let [value (-> ev .-target .-value)]
                                   (swap! state assoc :input value)
                                   (rf/dispatch [:search value])))}]
           (when (and expanded search-results)
             [:ul {:style {:position :absolute
                           :width "100%"
                           :margin 0
                           :top 32
                           :padding 0
                           :list-style :none
                           :box-shadow "0px 8px 8px 0px rgba(0,0,0,0.1)"
                           :z-index 1
                           :background-color "#ffffff"}}
              (for [result search-results]
                ^{:key (str "search-result-" (:value result))}
                [:li.dropdown-content
                 {:on-click (fn [] (rf/dispatch [:suggest-movie (:id result)]))}
                 (:label result)])])]))})))
