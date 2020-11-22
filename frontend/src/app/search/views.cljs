(ns app.search.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn- initial-state []
  {:input ""
   :expanded false
   :selected-index nil})

(defn- event->action [ev]
  (case (.-key ev)
    "ArrowUp" [:move-selection -1]
    "ArrowDown" [:move-selection 1]
    "Enter" [:accept-selection]
    nil))

(defn- move-selection [state delta]
  (update state :selected-index
          (fn [index]
            (cond
              (nil? index) 0
              (and (= index 0) (= delta -1)) nil
              :else (+ index delta)))))

(defn- accept-selection [state]
  (-> state
      (assoc :input "")
      (assoc :expanded false)
      (assoc :selected-index nil)))

(defn- selected-movie [state search-results]
  (let [index (:selected-index state)]
    (when (and index (< index (count search-results)))
      (nth search-results index))))

(defn- handle-keydown [state search-results ev]
  (let [[action arg] (event->action ev)]
    (case action
      :move-selection (swap! state move-selection arg)
      :accept-selection (let [movie (selected-movie @state search-results)]
                          (swap! state accept-selection)
                          (when movie
                            (rf/dispatch [:suggest-movie (:id movie)])))
      nil)))

(defn- handle-change [state ev]
  (let [value (-> ev .-target .-value)]
    (swap! state assoc :input value)
    (rf/dispatch [:search value])))

(defn- handle-click [result]
  (rf/dispatch [:suggest-movie (:id result)]))

(defn- search-dropdown [results selected-index]
  [:ul {:style {:position :absolute
                :width "100%"
                :margin 0
                :top 32
                :padding 0
                :list-style :none
                :box-shadow "0px 8px 8px 0px rgba(0,0,0,0.1)"
                :z-index 1
                :background-color "#ffffff"}}
   (for [[result i] (map vector results (range))]
     ^{:key (str "search-result-" (:id result))}
     [:li.dropdown-content {:style {:background-color
                                    (if (= i selected-index)
                                      "#ff0000")}
                            :on-click (partial handle-click result)}
      (:label result)])])

(defn search-bar []
  (let [state (r/atom (initial-state))
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
              {:keys [input expanded selected-index]} @state]
          [:div {:style {:position :relative
                         :display :flex
                         :flex-direction :column}
                 :ref (fn [el] (reset! ref el))}
           [:input {:style {:height 32}
                    :value input
                    :on-key-down (partial handle-keydown state search-results)
                    :on-change (partial handle-change state)}]
           (when (and expanded search-results)
             [search-dropdown search-results selected-index])]))})))