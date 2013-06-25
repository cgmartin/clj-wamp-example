(ns clj-wamp-example.config
  (:use [clojure.tools.logging :only [info]])
  (:require [clojure.java.io :as io]))

(defonce ^:private config (atom nil))

(defn read-conf
  "returns a parsed application config.clj from a resource directory"
  []
  (let [path (io/resource "config.clj")]
    (info "Reading configuration from" path)
    (read-string (slurp path))))

(defn reset-conf!
  "re-reads the config map and stores in memory"
  []
  (reset! config (read-conf)))

(defn conf
  "returns a config map, read once from resources directory
  and stored in memory"
  []
  (if (nil? @config)
    (reset-conf!)
    @config))