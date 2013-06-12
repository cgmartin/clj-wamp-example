(ns clj-wamp-example.websocket
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as http-kit]
            [clj-wamp.server :as wamp]))

(defn ws-on-open [sess-id]
  (log/debug "New websocket client connected [" sess-id "]"))

(defn ws-on-close [sess-id status]
  (log/debug "Websocket client disconnected [" sess-id "] " status))

;; RPC functions

(defn rpc-add [sess-id & params]
  (log/debug "Websocket RPC [" sess-id "] Add: " params)
  {:result (apply + params)})

;; PubSub Handlers

(defn ws-on-subscribe
  [sess-id topic]
  (log/debug "Websocket subscribe [" sess-id "] " topic)
  true) ; return false to deny subscription to this topic

(defn ws-on-publish
  [sess-id topic event exclude eligible]
  (log/debug "Websocket publish [" sess-id "] " topic event)
  [sess-id topic event exclude eligible]) ; return same params, can be rewritten

;; Main http-kit/WAMP WebSocket handler

(def ws-base-url "http://clj-wamp-example")
(def rpc-base-url (str ws-base-url "/api#"))
(def evt-base-url (str ws-base-url "/event#"))

(defn wamp-websocket-handler
  "returns a http-kit websocket handler with wamp subprotocol"
  [req]
  (http-kit/with-channel req channel
    (if (:websocket? req)
      (wamp/http-kit-handler channel
        {:on-open  ws-on-open
         :on-close ws-on-close
         :on-call      {(str rpc-base-url "add")  rpc-add}
         :on-subscribe {(str evt-base-url "chat") ws-on-subscribe}
         :on-publish   {(str evt-base-url "chat") ws-on-publish}})
      (http-kit/close channel))))
