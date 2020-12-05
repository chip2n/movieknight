(ns backend.db
  (:require [next.jdbc :as jdbc]
            [com.stuartsierra.component :as component]
            [backend.migrations :as migrate]))

(def jdbc-opts next.jdbc/snake-kebab-opts)

(defrecord Database [dbname]
  component/Lifecycle

  (start [this]
    (println "Starting database")
    (let [db-spec {:dbtype "postgresql" :dbname dbname}
          datasource (jdbc/get-datasource db-spec)]
      (migrate/init db-spec)
      (migrate/migrate db-spec)
      (assoc this
             :db-spec db-spec
             :datasource datasource)))

  (stop [this]
    (println "Stopping database")
    (assoc this :datasource nil)))

(defn create [name]
  (map->Database {:dbname name}))

(defn execute [{:keys [datasource]} query]
  (jdbc/execute!
   datasource
   query
   jdbc-opts))

(defn insert-movie [db {:keys [title synopsis image-url]}]
  (execute db ["INSERT INTO movie (title, synopsis, image_url) VALUES (?, ?, ?)" title synopsis image-url]))

(defn insert-account [db {:keys [name]}]
  (execute db ["INSERT INTO account (name) VALUES (?)" name]))

(defn get-movies [db]
  (execute db ["SELECT * FROM movie"]))

(comment
  (require '[dev :refer [db]])
  (insert-movie (db) {:title "Test movie" :synopsis "Synopsis" :image-url "https://example.com/test.png"})
  (insert-account (db) {:name "Andreas Arvidsson"})

  (get-movies (db))

  (execute (db) ["CREATE DATABASE \"test-db\""])
  (execute (db) ["DROP DATABASE \"test-db\""])
  )
