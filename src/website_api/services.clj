(ns website-api.services
  (:require [website-api.db-config :as db]
            [website-api.server-config :as server]
            [clojure.pprint :as pprint]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [clojure.data.json :as json])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

(declare create-user
         create-user-db
         delete-user
         delete-user-db
         does-email-exist
         get-user-by-id
         get-user-by-id-db
         get-user-by-email
         get-user-by-email-db
         get-users
         get-users-db
         test-read
         test-remove
         test-write)


;; User Services
(defn create-user [request]
  (pprint/pprint request)
  (let [;; Get parameters data from request
        firstName (get-in request [:body :firstName])
        lastName (get-in request [:body :lastName])
        email (get-in request [:body :email])
        password (get-in request [:body :password])
        ;; Build the response
        new-user-creation (create-user-db firstName lastName email password)
        body (get-in new-user-creation [:body])
        status (get-in new-user-creation [:status])
        headers (get-in new-user-creation [:headers])]
    {:status status
     :headers headers
     :body (json/write-str body)}))

(defn create-user-db [firstName lastName email password]
  (let [db db/db
        coll "user"
        _id  (ObjectId.)]
    ;; Only create user if their email is unique
    (if-not (does-email-exist email)
      (try
        (mc/insert-and-return db coll {:_id _id
                                       :type "user"
                                       :firstName firstName
                                       :lastName  lastName
                                       :email     email
                                       :password  password
                                       :isActive  true})
        ;; User successfully created
        {:status 200
         :headers {"ContentType" "application/json"
                   "Access-Control-Allow-Origin" "*"
                   "Access-Control-Allow-Headers" "*"
                   "Access-Control-Max-Age" "*"}
         :body {:collection
                {:version 1.0
                 :href (str server/addr "/api/user")
                 :links []
                 :items [{:href (str server/addr "/api/user/" _id)
                          :data [{:name "id" :value (str _id) :prompt "User Id"}
                                 {:name "firstName" :value firstName :prompt "First Name"}
                                 {:name "lastName" :value lastName :prompt "Last Name"}
                                 {:name "email" :value email :prompt "Email Address"}]
                          :links []}]}}}
        (catch Exception e
          ;; Todo: Log exception
          {:status 500
           :headers {"ContentType" "application/json"
                     "Access-Control-Allow-Origin" "*"}
           :body {:collection          
                   {:version 1.0,
                    :href (str server/addr "/api/user")
                    :error {
                            :title "User Creation Failure"
                            :message "Please contact website owner."
                            :code ""}}}}))
      ;; If email exists, return a message
        {:status 409
         :headers {"ContentType" "application/vnd.collection+json"}
         :body {:collection          
                 {:version 1.0,
                  :href (str server/addr "/api/user")
                  :error {
                          :title "User Creation Failure"
                          :message "Email Already Exists"
                          :code ""}}}})))
(defn delete-user [_id]
  (let [deletion (delete-user-db _id)
        status (get-in deletion [:status])
        headers (get-in deletion [:headers])
        body (get-in deletion [:body])]
    {:status status
     :headers headers
     :body (json/write-str body)}))

(defn delete-user-db [_id]
  (try
    (let [db   db/db
          coll "user"
          user (mc/update-by-id db coll (ObjectId. _id) {$set {:isActive false}} {:upsert false})]
      (get-users-db))
    (catch Exception e
      ;; Todo: Log exception
      {:status 500
       :headers {"ContentType" "application/vnd.collection+json"}
       :body {:collection          
              {:version 1.0,
               :href (str server/addr "/api/user")
               :error {
                       :title "User Deletion Failure"
                       :message "Please contact website owner."
                       :code ""}}}})))

(defn get-user-by-email [email]
  (let [user (get-user-by-email-db email)
        body (get-in user [:body])
        status (get-in user [:status])
        headers (get-in user [:headers])]
    {:status status
     :headers headers
     :body (json/write-str body)}))

(defn get-user-by-email-db [email]
  (try
    (let [db   db/db
          coll "user"
          user (mc/find-one-as-map db coll {:email email})
          firstName (get-in user [:firstName])
          lastName (get-in user [:lastName])
          email (get-in user [:email])
          _id (str (get-in user [:_id]))]
      
      {:status 200
       :headers {"ContentType" "application/vnd.collection+json"}
       :body {:collection
              {:version 1.0
               :href (str "http://" server/addr "/api/user")
               :links []
               :items [{:href (str "http://" server/addr "/api/user/" _id)
                        :data [{:name "id" :value _id :prompt "User Id"}
                               {:name "firstName" :value firstName :prompt "First Name"}
                               {:name "lastName" :value lastName :prompt "Last Name"}
                               {:name "email" :value email :email "Email Address"}]}
                       :links []]}}})
  (catch Exception e
    ;; Todo: Log exception
    {:status 500
     :headers {"ContentType" "application/vnd.collection+json"}
     :body {:collection          
            {:version 1.0,
             :href (str server/addr "/api/user")
             :error {
                     :title "User Retrieval Failure"
                     :message "Please contact website owner."
                     :code ""}}}})))

(defn does-email-exist [email]
  (let [db db/db
        coll "user"
        user (mc/find-one-as-map db coll {:email email})]
    (if user
      true
      false)))

;; Should this return the user if IsActive == False?
(defn get-user-by-id [_id]
  (let [user (get-user-by-id-db _id)
        body (get-in user [:body])
        status (get-in user [:status])
        headers (get-in user [:headers])]
    {:status status
     :headers headers
     :body (json/write-str body)}))
                     
(defn get-user-by-id-db [_id]
  (try
    (let [db   db/db
          coll "user"
          user (mc/find-map-by-id db coll (ObjectId. _id))
          firstName (get-in user [:firstName])
          lastName (get-in user [:lastName])
          email (get-in user [:email])]
      
      {:status 200
       :headers {"ContentType" "application/vnd.collection+json"}
       :body {:collection
              {:version 1.0
               :href (str "http://" server/addr "/api/user")
               :links []
               :items [{:href (str "http://" server/addr "/api/user/" _id)
                        :data [{:name "id" :value _id :prompt "User Id"}
                               {:name "firstName" :value firstName :prompt "First Name"}
                               {:name "lastName" :value lastName :prompt "Last Name"}
                               {:name "email" :value email :email "Email Address"}]}
                       :links []]}}})
  (catch Exception e
    ;; Todo: Log exception
    {:status 500
     :headers {"ContentType" "application/vnd.collection+json"}
     :body {:collection          
            {:version 1.0,
             :href (str server/addr "/api/user")
             :error {
                     :title "User Retrieval Failure"
                     :message "Please contact website owner."
                     :code ""}}}})))

;; This should be moved??
(defn build-user-items
  ([users]
   (if (= (count users) 0)
     []
     (let [user (first users)
           _id (str (get-in user [:_id]))
           firstName (get-in user [:firstName])
           lastName (get-in user [:lastName])
           email (get-in user [:email])
           item {:href (str "http://" server/addr "/api/user/" _id)
                 :data [{:name "id" :value _id :prompt "User Id"}
                        {:name "firstName" :value firstName :prompt "First Name"}
                        {:name "lastName" :value lastName :prompt "Last Name"}
                        {:name "email" :value email :email "Email Address"}]
                 :links []}
           items [item]]
       
       (if (> (count users) 1)
         (build-user-items users items 1)
         items))))

  ([users items index]
   (let [user (nth users index)
         _id (str (get-in user [:_id]))
         firstName (get-in user [:firstName])
         lastName (get-in user [:lastName])
         email (get-in user [:email])
         item {:href (str "http://" server/addr "/api/user/" _id)
               :data [{:name "id" :value _id :prompt "User Id"}
                      {:name "firstName" :value firstName :prompt "First Name"}
                      {:name "lastName" :value lastName :prompt "Last Name"}
                      {:name "email" :value email :email "Email Address"}]
               :links []}
         items (conj items item)]
     
     (if (> (count users) (inc index))
       (build-user-items users items (inc index))
       items))))

(defn get-users []
  (let [users (get-users-db)
        status (get-in users [:status])
        headers (get-in users [:headers])
        body (get-in users [:body])]
    {:status status
     :headers headers
     :body (json/write-str body)}))

(defn get-users-db []
  (let [db   db/db
        coll "user"
        users (mc/find-maps db "user" {:isActive true})]
    {:status 200
     :headers {"ContentType" "application/json"
               "Access-Control-Allow-Origin" "*"
               "Access-Control-Allow-Methods" "*"
               "Access-Control-Allow-Headers" "*"
               "Access-Control-Max-Age" "*"}
     :body {:collection
            {:version 1.0
             :href (str server/addr "/api/user/")
             :links []
             :items (build-user-items users)}}}))

(defn update-user [_id updates]
  (println "Updating User! " _id)
  (pprint/pprint updates)
  (let [db db/db
        coll "user"
        email (get-in updates [:email])
        firstName (get-in updates [:firstName])
        lastName (get-in updates [:lastName])
        password (get-in updates [:password])
        updates (apply hash-map (first (filter (fn [[k v]] (not (nil? v)))
                                               {:firstName firstName
                                                :lastName lastName
                                                :email email
                                                :password password})))
        user  (mc/update-by-id db coll (ObjectId. _id) {$set updates})]
    (get-user-by-id _id)))


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
        all-docs (monger.collection/find-maps db coll)]
    all-docs))
