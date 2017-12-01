(ns website-api.services
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

(defn test-write []
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"]
    (mc/insert-and-return db coll {:_id (ObjectId.)
                                   :username "UserNameTest"
                                   :password "PasswordTest"
                                   :roles "RolesTest"})))

(defn test-read []
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"
        all-docs (mc/find-maps db coll)]
    all-docs))

;; Remove all documents from the database
(defn test-remove []
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"]
    (mc/remove db coll)))
