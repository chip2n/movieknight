(ns core.user
  (:require [clojure.spec.alpha :as spec]))

(spec/def :user/user (spec/keys :req [:user/id :user/name]))

(spec/def :user/id int?)
(spec/def :user/name string?)
