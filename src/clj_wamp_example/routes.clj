(ns clj-wamp-example.routes
  (:use [compojure.core :only [defroutes GET]]
        (ring.middleware [keyword-params :only [wrap-keyword-params]]
                         [params :only [wrap-params]]
                         [session :only [wrap-session]])
        [clj-wamp-example.websocket :only [wamp-handler]]
        [clj-wamp-example.config :only [conf]])
  (:require [compojure.route :as route]
            [clojure.tools.logging :as log]
            [clabango.parser :as parser]))

;; Application routes ####################################

(def template-path "clj_wamp_example/views/")

(defn render
  "Render a template with correct context for the layout"
  [req template & [params]]
  (parser/render-file (str template-path template)
    (assoc (or params {})
      :request req)))

(defn wrap-cache
  "Set cache control headers for pages that do not change frequently"
  [age body]
  {:body body :headers {"Cache-Control" (str "public, max-age=" age)}})

(def cache-age 900)

(defroutes server-routes*
  (GET "/" [:as req]
    (wrap-cache cache-age
      (render req "home.html" {:title "Clojure WebSocket subprotocol for HTTP Kit"})))
  (GET "/tutorial" [:as req]
    (wrap-cache cache-age
      (render req "tutorial.html" {:title "Tutorial / Quickstart Guide"})))
  (GET "/chat" [:as req]
    (wrap-cache cache-age
      (render req "chat.html" (merge (conf) {:title "Chat Demo Example"}))))
  (GET "/rpc"  [:as req]
    (wrap-cache cache-age
      (render req "rpc.html"  (merge (conf) {:title "RPC Demo Example"}))))
  (GET "/ws"   [:as req] (wamp-handler req))
  ;; static files under ./resources/public folder
  (route/resources "/")
  ;; 404, modify for a better 404 page
  (route/not-found "<p>Page not found.</p>"))

;; Ring middleware ####################################

(defn wrap-failsafe [handler]
  (fn [req]
    (try (handler req)
      (catch Exception e
        (log/error e "error handling request" req)
        ;; FIXME provide a better page for 500 here
        {:status 500 :body "Sorry, an error occured."}))))

(defn app [] (-> #'server-routes*
              wrap-session
              wrap-keyword-params
              wrap-params
              wrap-failsafe))