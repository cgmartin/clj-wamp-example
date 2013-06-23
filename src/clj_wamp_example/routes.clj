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

(def ws-uri (:ws-uri (conf)))

(defroutes server-routes*
  (GET "/"     [:as req] (render req "home.html"))
  (GET "/chat" [:as req] (render req "chat.html" {:title "Chat Example"
                                                  :ws-uri ws-uri}))
  (GET "/rpc"  [:as req] (render req "rpc.html"  {:title "RPC Example"
                                                  :ws-uri ws-uri}))
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

(defn wrap-dir-index
  "Rewrite requests of / to /index.html"
  [handler]
  (fn [req]
    (handler
      (update-in req [:uri]
        #(if (= "/" %) "/index.html" %)))))

(defn app [] (-> #'server-routes*
              wrap-session
              wrap-keyword-params
              wrap-params
              wrap-failsafe))