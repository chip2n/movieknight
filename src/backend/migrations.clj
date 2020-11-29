(ns backend.migrations
  (:require [migratus.core :as migratus]))

(def config {:store :database
             :migration-dir "migrations/"
             :init-script "init.sql"
             :db {:dbtype "postgresql"
                  :dbname "movieknight"}})

(comment
  (migratus/init config))
