(defproject clj-wamp-example "0.1.0"
  :description "An example http-kit websocket project with clj-wamp"
  :url "https://github.com/cgmartin/clj-wamp-example"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [http-kit "2.1.3"]]
  :profiles {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :main clj-wamp-example.core)
