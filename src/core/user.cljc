(ns core.user
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::user (spec/keys :req [:user/id :user/name]))

(spec/def :user/id string?)
(spec/def :user/name string?)
