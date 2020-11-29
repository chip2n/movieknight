(ns backend.db
  (:require [next.jdbc :as jdbc]))

(def db {:dbtype "postgresql" :dbname "movieknight"})
(def ds (jdbc/get-datasource db))

(def jdbc-opts next.jdbc/snake-kebab-opts)

(defn create-movie-table []
  (jdbc/execute! ds ["
CREATE TABLE IF NOT EXISTS movie (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR,
  synopsis VARCHAR,
  image_url VARCHAR
)"]))

(defn insert-movie [{:keys [title synopsis image-url]}]
  (jdbc/execute!
   ds
   ["INSERT INTO movie (title, synopsis, image_url) VALUES (?, ?, ?)" title synopsis image-url]
   jdbc-opts))

(defn get-movies []
  (jdbc/execute!
   ds
   ["SELECT * FROM movie"]
   jdbc-opts))

(comment
  (insert-movie {:title "Test movie" :synopsis "Synopsis" :image-url "https://example.com/test.png"})
  (get-movies))
