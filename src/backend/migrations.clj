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
  (migratus/init config)
  (migratus/migrate config)

  (migratus/create config "test"))
