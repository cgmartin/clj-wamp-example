(ns clj-wamp-example.main
  (:gen-class)
  (:use [clojure.tools.logging :only [info]]
        [clojure.tools.cli :only [cli]]
        [org.httpkit.server :only [run-server]]
        [ring.middleware.reload :only [wrap-reload]]
        [clj-wamp-example.config :only [reset-conf!]]
        [clj-wamp-example.routes :only [app]]))

(defn- to-int [s] (Integer/parseInt s))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    (info "stopping server...")
    (@server) ; contains the stop-server callback fn
    (reset! server nil)
    ;; other cleanup stuff here ...
    ))

(defn start-server
  [& [options]]
    ;; stop it if started, for running multi-times in repl
    (when-not (nil? @server) (stop-server))
    ;; other init stuff here, like init-db, init-redis, ...
    (let [cfg        (reset-conf!)
          server-cfg (merge (:http-kit cfg) options)]
      (reset! server
        (run-server (if (:hot-reload cfg) (wrap-reload (app)) (app)) server-cfg))
      (info "server started. listen on" (:ip server-cfg) "@" (:port server-cfg))))

(defn -main [& args]
  (let [[options _ banner]
          (cli args
            ["-i" "--ip"     "The ip address to bind to"]
            ["-p" "--port"   "Port to listen"            :parse-fn to-int]
            ["-t" "--thread" "Http worker thread count"  :parse-fn to-int]
            ["--[no-]help"   "Print this help"])]
    (when (:help options) (println banner) (System/exit 0))
    ; Shutdown hook
    (. (Runtime/getRuntime) (addShutdownHook (Thread. stop-server)))
    (start-server options)))