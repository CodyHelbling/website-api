(ns website-api.db-config
  (:gen-class)
  (:require [monger.core :as mg]))

(def conn nil)

(defn init []
  (let [local_db (mg/connect)]
    (println "Initializing Database Connection: " conn)
    (def db local_db)))
