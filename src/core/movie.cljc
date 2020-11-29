(ns core.movie
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::movie (spec/keys :req-un [::id ::title ::synopsis ::rating ::image-url]))

(spec/def ::id string?)
(spec/def ::title string?)
(spec/def ::synopsis string?)
(spec/def ::rating float?)
(spec/def ::image-url string?)
