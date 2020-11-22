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

(defn- set-selection [state index]
  (assoc state :selected-index index))

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

(defn- handle-mouse-over [state results result]
  (let [index (.indexOf results result)]
    (swap! state set-selection index)))

(defn- handle-click-outside [state ref ev]
  (let [expanded (.contains @ref (.-target ev))]
    (swap! state (fn [s] (-> s
                             (assoc :expanded expanded)
                             (set-selection nil))))))

(defn- search-dropdown [state results selected-index]
  [:ul.dropdown
   (for [[result i] (map vector results (range))]
     ^{:key (str "search-result-" (:id result))}
     [:li.dropdown-content
      {:style {:background-color
               (if (= i selected-index)
                 "#ff0000")}
       :on-mouse-over (partial handle-mouse-over state results result)
       :on-click (partial handle-click result)}
      (:label result)])])

(defn search-bar []
  (let [state (r/atom (initial-state))
        ref (clojure.core/atom nil)
        click-listener (partial handle-click-outside state ref)]
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
                         :flex-direction :row}
                 :ref (fn [el] (reset! ref el))}
           [:div.search-bar-container
            [:i.fa.fa-search {:style {:display :flex}}]
            [:input.search-bar
             {:value input
              :placeholder "Search anime"
              :on-key-down (partial handle-keydown state search-results)
              :on-change (partial handle-change state)}]]
           (when (and expanded search-results)
             [search-dropdown state search-results selected-index])]))})))
