(ns csp.core
  (:gen-class)
  (:refer-clojure :exclude [send])
  (:require [clojure.core.async :as async :refer [>!! go <!]])
  (:import java.net.InetSocketAddress
           java.nio.ByteBuffer
           [java.nio.channels AsynchronousChannelGroup AsynchronousSocketChannel CompletionHandler]
           java.util.concurrent.Executors))

(defn connect [group]
  (let [ch (async/chan)
        channel (AsynchronousSocketChannel/open group)
        handler (proxy [CompletionHandler] []
                  (completed [result attachment]
                    (>!! ch channel)))]
    (.connect channel (InetSocketAddress. "localhost" 50007) channel handler)
    ch))

(defn send [channel id]
  (let [ch (async/chan)
        message (str "GET /" id "?10 HTTP/1.0\r\n\r\n")
        write-buf (doto (ByteBuffer/allocate 2048)
                    (.put (.getBytes message))
                    (.flip))
        handler (proxy [CompletionHandler] []
                  (completed [result attachment]
                    (>!! ch write-buf)))]
    (println (str "send request " id))
    (.write channel write-buf write-buf handler)
    ch))

(defn recv [channel]
  (let [ch (async/chan)
        read-buf (ByteBuffer/allocate 2048)
        handler (proxy [CompletionHandler] []
                  (completed [result attachment]
                    (>!! ch read-buf)))]
    (.read channel read-buf read-buf handler)
    ch))

(defn -main []
  (println "Wait for starting...")
  (Thread/sleep 10000)
  (let [group (AsynchronousChannelGroup/withFixedThreadPool 10 (Executors/defaultThreadFactory))]
    (dotimes [id 10]
      (go
        (let [channel (<! (connect group))
              _ (<! (send channel id))
              r (<! (recv channel))]
          (println (String. (.array r))))))
    (Thread/sleep 15000)))
