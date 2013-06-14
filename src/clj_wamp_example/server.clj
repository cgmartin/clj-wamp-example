(ns clj-wamp-example.server
  (:gen-class)
  (:use compojure.core
        clj-wamp-example.websocket
        [carica.core :refer [configurer resources]]
        [clojure.tools.cli :only [cli]])
  (:require [clojure.java.io :as io]
            [noir.util.middleware :as middleware]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [ring.middleware.reload :as reload]
            [org.httpkit.server :as http-kit]
            [clabango.parser :as parser])
  (:import [java.io PushbackReader]))

;; Get app config from resources dir
(def conf (configurer (resources "config.clj")))

;; Application routes ####################################

(def template-path "clj_wamp_example/views/")

(defn render
  "Render a template with correct context for the layout"
  [req template & [params]]
  (parser/render-file (str template-path template)
    (assoc (or params {})
      :request req)))

(defroutes app-routes
  (GET "/"     [:as req] (render req "home.html"))
  (GET "/chat" [:as req] (render req "chat.html" {:title "Chat Example"
                                                  :ws-uri (:ws-uri conf)}))
  (GET "/rpc"  [:as req] (render req "rpc.html"  {:title "RPC Example"
                                                  :ws-uri (:ws-uri conf)}))
  (GET "/ws"   [:as req] (wamp-websocket-handler req))
  (route/resources "/")
  (route/not-found "Not Found"))

;; http-kit server ####################################

(defn init []
  (log/info "App starting up..."))

(defn destroy []
  (log/info "App is shutting down..."))

(def app (middleware/app-handler [app-routes]))

(def app-war-handler (middleware/war-handler app))

(defn -main
  "runs http-kit server with application routes"
  [& args]
    (let [[options args banner]
          (cli args
            ["-p" "--port" "Listen on this port"       :default 8080 :parse-fn #(Integer. %)]
            ["-i" "--ip"   "The ip address to bind to" :default "0.0.0.0"]
            ["-h" "--help" "Show help"                 :default false :flag true])
          httpkit-cfg (assoc (conf :http-kit)
                        :port (:port options)
                        :ip   (:ip options))]
      (when (:help options)
        (println banner)
        (System/exit 0))
      ; Startup hook
      (init)
      ; Shutdown hook
      (. (Runtime/getRuntime)
        (addShutdownHook (Thread. destroy)))
      ; Start server
      (http-kit/run-server
        (if (:hot-reload conf)
          (reload/wrap-reload app-war-handler)
          app-war-handler)
        httpkit-cfg)))
