(ns website-api.db-config
  (:gen-class)
  (:require [monger.core :as mg]))

(def conn nil)

(def db nil)

(defn init []
  (let [conn (mg/connect)
        db (mg/get-db conn "monger-test")]
    (println "\nInitializing Database Connection:");  conn)
    (def conn conn)
    (def db db)))

(def server "http://localhost")
