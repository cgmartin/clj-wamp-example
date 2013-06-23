(ns clj-wamp-example.config
  (:use [clojure.tools.logging :only [info]])
  (:require [clojure.java.io :as io]))

(def ^:private config (atom nil))

(defn read-conf
  "returns a parsed application config from resources directory"
  []
  (let [path (io/resource "config.clj")]
    (info "Reading configuration from" path)
    (read-string (slurp path))))

(defn reset-conf!
  "clears the in-memory config map"
  []
  (reset! config (read-conf)))

(defn conf
  "returns a config map, read once from resources directory"
  []
  (if (nil? @config)
    (reset! config (read-conf))
    @config))