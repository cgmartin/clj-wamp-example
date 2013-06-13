(ns clj-wamp-example.websocket
  (:use [clojure.string :only [split]])
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as http-kit]
            [clj-wamp.server :as wamp]))

(declare usernames add-username del-username get-username get-user-list truncate-str)

;; Topic BaseUrls
(def ws-base-url "http://clj-wamp-example")
(def rpc-base-url (str ws-base-url "/api#"))
(def evt-base-url (str ws-base-url "/event#"))

(defn rpc-url [path] (str rpc-base-url path))
(defn evt-url [path] (str evt-base-url path))

;; RPC functions

(defn rpc-add [sess-id & params]
  (log/debug "Websocket RPC [" sess-id "] Add: " params)
  {:result (apply + params)})

;; PubSub Subscription Handlers

(defn ws-chat-subscribe?
  "Should the client be allowed to subscribe to the 'chat' topic?"
  [sess-id topic]
  (log/debug "Websocket subscribe? [" sess-id "] " topic)
  true) ; return false to deny subscription to this topic

(defn ws-on-subscribe
  "After successfully subscribing to any topic"
  [sess-id topic]
  (log/debug "Websocket subscribed [" sess-id "] " topic)
  (when (= topic (evt-url "chat"))
    (wamp/send-event! topic {:type     "user-joined"
                             :clientId sess-id
                             :username (get-username sess-id)})
    (wamp/send-event! sess-id topic {:type  "user-list"
                                     :users (get-user-list topic)} true)))

(defn ws-on-unsubscribe
  "After unsubscribing from any topic"
  [sess-id topic]
  (log/debug "Websocket unsubscribed [" sess-id "] " topic)
  (when (= topic (evt-url "chat"))
    (wamp/send-event! topic {:type     "user-left"
                             :clientId sess-id
                             :username (get-username sess-id)})
    (del-username sess-id)))

;; PubSub Event Message Handlers

(defn on-chat-message
  "When receiving a 'chat message', validate message"
  [sess-id topic event exclude eligible]
  ; Ignore empty messages
  (when (> (count (event "message")) 0)
    (let [message (event "message")
          event (assoc event
                  ; Truncate message
                  "message" (truncate-str message 140)
                  ; Include username
                  "username" (get-username sess-id)
                  ; Include client id
                  "clientId" sess-id)]
      ; return rewritten params for publish
      [sess-id topic event exclude eligible])))

(defn on-chat-username
  "When receivomg a username changed event, validate username"
  [sess-id topic event exclude eligible]
  ; Ignore empty names
  (when (> (count (event "newUsername")) 0)
    (let [newUsername (truncate-str (event "newUsername") 20)
          oldUsername (get-username sess-id)
          event (assoc event
                  ; Truncate message
                  "newUsername" newUsername
                  ; Include old username
                  "oldUsername" oldUsername
                  ; Include client id
                  "clientId" sess-id)]
      ; Ignore if same username
      (when (not= newUsername oldUsername)
        (add-username sess-id newUsername)
        ; return rewritten params for publish
        [sess-id topic event exclude eligible]))))

(defn ws-on-chat-publish
  "When publish events are received in the 'chat' topic"
  [sess-id topic event exclude eligible]
  (log/debug "Websocket publish [" sess-id "] " topic event)
  (when-let [event-type (event "type")]
    (case event-type
      "message"  (on-chat-message sess-id topic event exclude eligible)
      "username" (on-chat-username sess-id topic event exclude eligible)
      false))) ; deny publish of unknown types

;; Main http-kit/WAMP WebSocket handler

(defn ws-on-open [sess-id]
  (log/debug "New websocket client connected [" sess-id "]"))

(defn ws-on-close [sess-id status]
  (log/debug "Websocket client disconnected [" sess-id "] " status))

(defn wamp-websocket-handler
  "Returns a http-kit websocket handler with wamp subprotocol"
  [req]
  (http-kit/with-channel req channel
    (if (:websocket? req)
      (wamp/http-kit-handler channel
        {:on-open        ws-on-open
         :on-close       ws-on-close
         :on-call        {(rpc-url "add")  rpc-add}
         :on-subscribe   {(evt-url "chat") ws-chat-subscribe?
                          :on-after        ws-on-subscribe}
         :on-publish     {(evt-url "chat") ws-on-chat-publish}
         :on-unsubscribe ws-on-unsubscribe})
      (http-kit/close channel))))

;; Utilities

; { :sess-id-1 "username", :sess-id-2 "username" }
(def usernames (atom {}))

(defn add-username
  [sess-id username]
  (swap! usernames assoc sess-id username))

(defn del-username
  [sess-id]
  (swap! usernames dissoc sess-id))

(defn get-username [sess-id]
  (if-let [username (@usernames sess-id)]
    username
    (let [new-username (str "guest-" (last (split sess-id #"-")))]
      (add-username sess-id new-username)
      new-username)))

(defn get-user-list [topic]
  (map (fn [id] {:clientId id, :username (get-username id)})
    (wamp/topic-clients topic)))

(defn truncate-str [s, n]
  (apply str (take n s)))
