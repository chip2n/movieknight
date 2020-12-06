(ns core.vote
  (:require [clojure.spec.alpha :as s]))

(s/def :vote/user-id :user/id)
(s/def :vote/movie-id :movie/id)
(s/def :vote/answer boolean?)
