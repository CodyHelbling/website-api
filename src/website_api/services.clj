(ns website-api.services
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.data.json :as json])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

(defn get-user [email]
  (println "EMAIL: " email)
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"
        user (mc/find-one db coll {:email email})]
    (println (str user))
    user))

(defn get-users []
  (println "get-users")
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"
        users (mc/find-maps db coll {:type "user"})]
    (println (str users))
    users))  

(defn create-user [request]
  (println "create-user request: " request)
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"
        id   (ObjectId.)]
    (try
      (mc/insert-and-return db coll {:_id id
                                     :type "user"
                                     :firstName (get-in request [:body :firstName])
                                     :lastName  (get-in request [:body :lastName])
                                     :email     (get-in request [:body :email])
                                     :password  (get-in request [:body :password])})
    (json/write-str (get-user (get-in request [:body :email])))
    (catch Exception e 
      {:body (str "Failure: User Creation: Exception: " e)
       :status 500
       :headers {"Content-Type" "text/plain"}}))))

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
