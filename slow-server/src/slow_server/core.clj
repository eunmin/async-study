(ns slow-server.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]))

(defn to-int [s]
  (try
    (Integer/parseInt s)
    (catch Throwable t
      0)))

(defn handler [{:keys [uri query-string]}]
  (Thread/sleep (* 1000 (to-int query-string)))
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body uri})

(defn -main []
  (run-jetty handler {:port 50007}))
