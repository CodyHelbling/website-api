(ns website-api.services
  (:require [website-api.db-config :as db]
            [clojure.pprint :as pprint]
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
        coll "user"
        id   (ObjectId.)]
    (if (get-user request)
      {:body {:message "Conflict: User Creation: Email Already Exists"
              :status 409
              :endpoint "/api/user"
              :method "POST"}
       :status 409
       :headers {"Content-Type" "text/plain"}}
      (try
        (mc/insert-and-return db coll {:_id id
                                       :type "user"
                                       :firstName (get-in request [:body :firstName])
                                       :lastName  (get-in request [:body :lastName])
                                       :email     (get-in request [:body :email])
                                       :password  (get-in request [:body :password])
                                       :isActive  true})
        (json/write-str (get-user request))
        (catch Exception e
          ;; Todo: Log exception
          {:body {:message (str "Failure: User Creation: Exception: " e)
                  :status 500
                  :endpoint "/api/user"
                  :method "POST"}
           :status 500
           :headers {"Content-Type" "text/plain"}})))))

(defn delete-user [email]
  (println "delete-user: email" email)
  (let [db   db/db
        coll "user"
        user (mc/update db coll  {:email email} {$set {:isActive false}} {:upsert true})]
    (println user)
    (get-user email)))

(defn get-user [request]
  (let [db   db/db
        coll "user"
        email (get-in request [:body :email])
        user (mc/find-one db coll {:email email})]
    (println (str user))
    (doall user)))

(defn get-users []
  (println "get-users")
  (let [db   db/db
        coll "user"
        users(mc/find-maps db "user")]
    (println (str users))
    users))

(defn update-user [request]
  (println "update-user")
  (let [db db/db
        coll "user"
        email (get-in request [:body :email])
        firstName (get-in request [:body :firstName])
        _id (get-in request [:body :_id])
        lastName (get-in request [:body :lastName])
        password (get-in request [:body :password])
        updates (apply hash-map (first (filter (fn [[k v]] (not (nil? v)))
                                               {:_id _id
                                                :firstName firstName
                                                :lastName lastName
                                                :email email
                                                :password password})))

        user  (mc/update db coll  {:_id _id} {$set updates} {:upsert true})]
    ; (pprint/pprint updates)
    (get-user email)))


;; Test Services
;; Remove all documents from the database
(defn test-remove []
  (let [db   db/db
        coll "user"]
    (mc/remove db coll)))


(defn test-write []
  (let [db   db/db
        coll "user"]
    (mc/insert-and-return db coll {:_id (ObjectId.)
                                   :username "UserNameTest"
                                   :password "PasswordTest"
                                   :roles "RolesTest"})))

(defn test-read []
  (let [db   db/db
        coll "user"
        all-docs (mc/find-maps db coll)]
    all-docs))

