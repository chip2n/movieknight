(ns app.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! timeout]]))

(defn do-request []
  (go (let [response (<! (http/get "http://localhost:8000"
                                   {:with-credentials? false
                                    :accept "application/json"}))]
        (:body response))))

(defn search [query]
  (go (<! (timeout 200))
      [{:id "chobits"
        :title "Chobits"
        :synopsis "When computers start to look like humans, can love remain the same?

Hideki Motosuwa is a young country boy who is studying hard to get into college. Coming from a poor background, he can barely afford the expenses, let alone the newest fad: Persocoms, personal computers that look exactly like human beings. One evening while walking home, he finds an abandoned Persocom. After taking her home and managing to activate her, she seems to be defective, as she can only say one word, \"Chii,\" which eventually becomes her name. Unlike other Persocoms, however, Chii cannot download information onto her hard drive, so Hideki decides to teach her about the world the old-fashioned way, while studying for his college entrance exams at the same time.

Along with his friends, Hideki tries to unravel the mystery of Chii, who may be a \"Chobit,\" an urban legend about special units that have real human emotions and thoughts, and love toward their owner. But can romance flourish between a Persocom and a human?"
        :rating 7.3
        :image-url "https://cdn.myanimelist.net/images/anime/1467/92615l.webp"}]))
