(ns app.search.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

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
                ^{:key (str "search-result-" (:id result))}
                [:li.dropdown-content
                 {:on-click (fn [] (rf/dispatch [:suggest-movie (:id result)]))}
                 (:label result)])])]))})))
