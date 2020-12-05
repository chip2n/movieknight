(ns core.movie
  (:require [clojure.spec.alpha :as spec]))

(spec/def :movie/movie (spec/keys :req [:movie/id :movie/title :movie/synopsis :movie/rating :movie/image-url]))

(spec/def :movie/id int?)
(spec/def :movie/title string?)
(spec/def :movie/synopsis string?)
(spec/def :movie/rating float?)
(spec/def :movie/image-url string?)
