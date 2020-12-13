(ns backend.db
  (:require [next.jdbc :as jdbc]
            [com.stuartsierra.component :as component]
            [backend.migrations :as migrate]))

(def jdbc-opts next.jdbc/snake-kebab-opts)

(defn create-jdbc [dbname]
  (let [db-spec {:dbtype "postgresql" :dbname dbname}
        datasource (jdbc/get-datasource db-spec)]
    {:db-spec db-spec
     :datasource datasource}))

(defn setup [{:keys [db-spec]}]
  (migrate/init db-spec)
  (migrate/migrate db-spec))

(defrecord Database [dbname]
  component/Lifecycle

  (start [this]
    (println "Starting database")
    (let [{:keys [db-spec datasource] :as db} (create-jdbc dbname)]
      (setup db)
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

(defn insert-movie [db {:keys [title synopsis rating image-url]}]
  (execute db ["INSERT INTO movie (title, synopsis, rating, image_url) VALUES (?, ?, ?, ?)" title synopsis rating image-url]))

(defn insert-account [db {:keys [name]}]
  (execute db ["INSERT INTO account (name) VALUES (?)" name]))

;; TODO can we make this more efficient?
(defn get-movies [db]
  (->> (execute db ["SELECT * FROM movie"])
       (map #(update % :movie/rating float))))

(defn get-accounts [db]
  (execute db ["SELECT * FROM account"]))

(defn get-votes [db]
  (execute db ["
SELECT
  vote.user_id,
  vote.movie_id,
  vote.answer
FROM vote
"]))

(defn insert-vote [db {:keys [user-id movie-id answer]}]
  (execute db ["INSERT INTO vote (user_id, movie_id, answer) VALUES (?, ?, ?)" user-id movie-id answer]))

(comment
  (require '[dev :refer [db]])
  (insert-movie (db) {:title "Test movie" :synopsis "Synopsis" :image-url "https://example.com/test.png"})
  (insert-account (db) {:name "Joppe Widstam"})

  (get-movies (db))

  (execute (db) ["CREATE DATABASE \"test-db\""])
  (execute (db) ["DROP DATABASE \"test-db\""])



  (let [movies [{:id 1
                 :title "Vinland Saga"
                 :synopsis "Young Thorfinn grew up listening to the stories of old sailors that had traveled the ocean and reached the place of legend, Vinland. It's said to be warm and fertile, a place where there would be no need for fighting—not at all like the frozen village in Iceland where he was born, and certainly not like his current life as a mercenary. War is his home now. Though his father once told him, \"You have no enemies, nobody does. There is nobody who it's okay to hurt,\" as he grew, Thorfinn knew that nothing was further from the truth.

The war between England and the Danes grows worse with each passing year. Death has become commonplace, and the viking mercenaries are loving every moment of it. Allying with either side will cause a massive swing in the balance of power, and the vikings are happy to make names for themselves and take any spoils they earn along the way. Among the chaos, Thorfinn must take his revenge and kill Askeladd, the man who murdered his father. The only paradise for the vikings, it seems, is the era of war and death that rages on."
                 :rating 7.3
                 :image-url "https://cdn.myanimelist.net/images/anime/1500/103005l.webp"}
                {:id 2
                 :title "Fate/Zero"
                 :synopsis "With the promise of granting any wish, the omnipotent Holy Grail triggered three wars in the past, each too cruel and fierce to leave a victor. In spite of that, the wealthy Einzbern family is confident that the Fourth Holy Grail War will be different; namely, with a vessel of the Holy Grail now in their grasp. Solely for this reason, the much hated \"Magus Killer\" Kiritsugu Emiya is hired by the Einzberns, with marriage to their only daughter Irisviel as binding contract.

Kiritsugu now stands at the center of a cutthroat game of survival, facing off against six other participants, each armed with an ancient familiar, and fueled by unique desires and ideals. Accompanied by his own familiar, Saber, the notorious mercenary soon finds his greatest opponent in Kirei Kotomine, a priest who seeks salvation from the emptiness within himself in pursuit of Kiritsugu.

Based on the light novel written by Gen Urobuchi, Fate/Zero depicts the events of the Fourth Holy Grail War—10 years prior to Fate/stay night. Witness a battle royale in which no one is guaranteed to survive."
                 :rating 7.5
                 :image-url "https://cdn.myanimelist.net/images/anime/2/73249l.webp"}
                {:id 3
                 :title "One Piece"
                 :synopsis "Gol D. Roger was known as the \"Pirate King,\" the strongest and most infamous being to have sailed the Grand Line. The capture and execution of Roger by the World Government brought a change throughout the world. His last words before his death revealed the existence of the greatest treasure in the world, One Piece. It was this revelation that brought about the Grand Age of Pirates, men who dreamed of finding One Piece—which promises an unlimited amount of riches and fame—and quite possibly the pinnacle of glory and the title of the Pirate King.

Enter Monkey D. Luffy, a 17-year-old boy who defies your standard definition of a pirate. Rather than the popular persona of a wicked, hardened, toothless pirate ransacking villages for fun, Luffy’s reason for being a pirate is one of pure wonder: the thought of an exciting adventure that leads him to intriguing people and ultimately, the promised treasure. Following in the footsteps of his childhood hero, Luffy and his crew travel across the Grand Line, experiencing crazy adventures, unveiling dark mysteries and battling strong enemies, all in order to reach the most coveted of all fortunes—One Piece."
                 :rating 7.5
                 :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}
                {:id 4
                 :title "Naruto: Shippuden"
                 :synopsis "It has been two and a half years since Naruto Uzumaki left Konohagakure, the Hidden Leaf Village, for intense training following events which fueled his desire to be stronger. Now Akatsuki, the mysterious organization of elite rogue ninja, is closing in on their grand plan which may threaten the safety of the entire shinobi world.

Although Naruto is older and sinister events loom on the horizon, he has changed little in personality—still rambunctious and childish—though he is now far more confident and possesses an even greater determination to protect his friends and home. Come whatever may, Naruto will carry on with the fight for what is important to him, even at the expense of his own body, in the continuation of the saga about the boy who wishes to become Hokage."
                 :rating 7.5
                 :image-url "https://cdn.myanimelist.net/images/anime/3/67177l.jpg"}]]
    (doseq [m movies]
      (insert-movie (db) m))))
