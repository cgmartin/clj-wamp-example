(defproject clj-wamp-example "0.2.4"
  :description "An example http-kit websocket project with clj-wamp"
  :url "https://github.com/cgmartin/clj-wamp-example"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :uberjar-name "clj-wamp-example-standalone.jar"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [log4j "1.2.17" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.cli "0.2.2"]
                 [ring-server "0.2.8"]
                 [lib-noir "0.5.5"]
                 [compojure "1.1.5"]
                 [clabango "0.5"]
                 [http-kit "2.1.5"]
                 [clj-wamp "1.0.0-rc1"]]
  :profiles {:dev {:resource-paths ["resources-dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.3"]]
                   :jvm-opts ["-Xmx1g" "-server"
                              "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :production {:resource-paths ["resources-prod"]}}
  :main clj-wamp-example.main)
