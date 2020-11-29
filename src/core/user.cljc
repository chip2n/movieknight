(ns core.user
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::user (spec/keys :req-un [::id ::name]))

(spec/def ::id string?)
(spec/def ::name string?)
