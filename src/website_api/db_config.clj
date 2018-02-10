

(ns website-api.db-config
  (:gen-class)
  (:require [monger.core :as mg])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def conn nil)

(def db nil)

(defn init []
  (let [ conn (mg/connect {:host "mongo1" :port 27017})]
    (println "\nInitializing Database Connection: " conn)
    (def conn conn)
    (def db db)))

(def server "http://0.0.0.0:8080")
