(ns website-api.services
  (:require [website-api.db-config :as db]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [clojure.data.json :as json])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

(declare create-user
         delete-user
         get-user
         get-users
         test-read
         test-remove
         test-write)

;; User Services
(defn create-user [request]
  (println "create-user request: " request)
  (let [db   db/db
        coll "documents"
        id   (ObjectId.)]
    (try
      (mc/insert-and-return db coll {:_id id
                                     :type "user"
                                     :firstName (get-in request [:body :firstName])
                                     :lastName  (get-in request [:body :lastName])
                                     :email     (get-in request [:body :email])
                                     :password  (get-in request [:body :password])
                                     :isActive  true})
    (json/write-str (get-user (get-in request [:body :email])))
    (catch Exception e 
      {:body (str "Failure: User Creation: Exception: " e)
       :status 500
       :headers {"Content-Type" "text/plain"}}))))

(defn delete-user [email]
  (println "delete-user: email" email)
  (let [db   db/db
        coll "documents"
        user (mc/update db coll  {:email email} {$set {:isActive false}} {:upsert true})]
    (println user)
    (get-user email)))
              
(defn get-user [email]
  (println "EMAIL: " email)
  (let [db   db/db
        coll "documents"
        user (mc/find-one db coll {:email email})]
    (println (str user))
    user))

(defn get-users []
  (println "get-users")
  (let [db   db/db
        coll "documents"
        users (mc/find-maps db coll {:type "user"})]
    (println (str users))
    users))

;; (defn update-user [request]
;;   (println "update-user")
;;   (let [con
  

;; Test Services
;; Remove all documents from the database
(defn test-remove []
  (let [db   db/db
        coll "documents"]
    (mc/remove db coll)))


(defn test-write []
  (let [db   db/db
        coll "documents"]
    (mc/insert-and-return db coll {:_id (ObjectId.)
                                   :username "UserNameTest"
                                   :password "PasswordTest"
                                   :roles "RolesTest"})))

(defn test-read []
  (let [db   db/db
        coll "documents"
        all-docs (mc/find-maps db coll)]
    all-docs))

