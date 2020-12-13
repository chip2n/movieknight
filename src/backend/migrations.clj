(ns backend.migrations
  (:require [migratus.core :as migratus]))

(def base-config {:store :database
                  :migration-dir "migrations/"
                  :init-script "init.sql"})

(defn init [db-spec]
  (migratus/init (assoc base-config :db db-spec)))

(defn migrate [db-spec]
  (migratus/migrate (assoc base-config :db db-spec)))

(comment
  (require '[dev :refer [db]])

  (init (:db-spec (db)))

  (migratus/create config "test"))
