(defproject clj-wamp-example "0.1.0"
  :description "An example http-kit websocket project with clj-wamp"
  :url "https://github.com/cgmartin/clj-wamp-example"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
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
                 [http-kit "2.1.3"]
                 [clj-wamp "0.4.2"]]
  :profiles {:dev {:resource-paths ["dev-resources"]
                   :jvm-opts ["-Xmx1g" "-server"
                              "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :production {:resource-paths ["prod-resources"]}}
  :main clj-wamp-example.server)
