(ns clj-wamp-example.server
  (:use compojure.core
        clj-wamp-example.websocket)
  (:require [noir.util.middleware :as middleware]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [ring.middleware.reload :as reload]
            [org.httpkit.server :as http-kit]
            [clabango.parser :as parser]))

(def config {:hot-reload true
             :http-kit {:port 8080
                        :thread 4}})

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
  (GET "/chat" [:as req] (render req "chat.html" {:title "Chat"}))
  (GET "/rpc"  [:as req] (render req "rpc.html"  {:title "Chat"}))
  (GET "/ws"   [:as req] (wamp-websocket-handler req))
  (route/resources "/")
  (route/not-found "Not Found"))

;; Ring server ####################################

(defn init []
  (log/info "App starting up..." (get-in config [:http-kit :port])))

(defn destroy []
  (log/info "App is shutting down..."))

(def app (middleware/app-handler [app-routes]))

(def app-war-handler (middleware/war-handler app))

(defn -main
  "runs http-kit server with application routes"
  [& args]
  ; Startup hook
  (init)
  ; Shutdown hook
  (. (Runtime/getRuntime)
    (addShutdownHook (Thread. destroy)))
  ; Start server
  (http-kit/run-server
    (if (config :hot-reload)
      (reload/wrap-reload app-war-handler)
      app-war-handler)
    (config :http-kit)))
