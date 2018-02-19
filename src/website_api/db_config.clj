

(ns website-api.db-config
  (:gen-class)
  (:require [monger.core :as mg])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def conn nil)

(def db nil)

(defn init []
  (let [ ;; conn (mg/connect {:host "mongo1" :port 27017})
        conn (mg/connect {:host "192.168.99.100" :port 27017})
         db (mg/get-db conn "drop-happy-db1")]
         
    (println "\nInitializing Database Connection: " conn)
    (def conn conn)
    (def db db)))

(def server "http://0.0.0.0:8080")
