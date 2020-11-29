(defproject backend "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src" "dev"]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.match "1.0.0"]
                 [compojure "1.6.2"]
                 [com.taoensso/sente "1.16.0"]
                 [com.taoensso/timbre "5.1.0"]
                 [seancorfield/next.jdbc "1.1.613"]
                 [org.postgresql/postgresql "42.2.18"]
                 [http-kit "2.5.0"]
                 [camel-snake-kebab "0.4.2"]
                 [com.stuartsierra/component "1.0.0"]
                 [com.stuartsierra/component.repl "0.2.0"]
                 [ring/ring-anti-forgery "1.3.0"]
                 [ring-cors "0.1.13"]]
  :main ^:skip-aot backend.core
  :target-path "target/%s"
  :repl-options {:init-ns user}
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
