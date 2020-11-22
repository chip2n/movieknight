(ns app.vote.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :user-votes
 (fn [db v] (:user-votes db)))

(rf/reg-sub
 :vote-prompts
 (fn [db v]
   (let [user-id (get-in db [:session :user-id])
         voted-movies (keys (get-in db [:user-votes user-id]))
         movies (:movies db)
         unvoted-movies (clojure.set/difference (into #{} (keys movies))
                                                (into #{} voted-movies))]
     (map (partial get movies) unvoted-movies))))

(defn- sort-vote-list [rows]
  "Sort the vote list first by # positive answers, then by # unanswered."
  (sort-by
       (juxt
        (comp count (partial filter true?))
        (comp count (partial filter nil?)))
       #(compare %2 %1)
       rows))

(rf/reg-sub
 :vote-list
 (fn [db v]
   (let [votes (:user-votes db)
         movies (:movies db)
         suggested-movies (:suggested-movies db)
         users (:users db)]
     {:users users
      :movies
      (->> movies
           (filter (fn [[id m]] (some (partial = id) suggested-movies)))
           (map (fn [[id m]] (concat [(:title m)]
                                     (map #(get-in votes [(:id %) id]) users))))
           (sort-vote-list))})))

(rf/reg-sub
 :movie-suggestions
 (fn [db v]
   (let [prompts (:suggested-movies db)
         movies (:movies db)]
     (->> prompts
          (map #(get movies %))
          (filter (comp not nil?))
          (into [])))))
