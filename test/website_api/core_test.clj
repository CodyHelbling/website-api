(ns website-api.core-test
  (:require [clojure.test :refer :all]
            [website-api.db-config :as db]
            [website-api.core :refer :all]
            [website-api.services :as services]
            [clj-http.client :as client]
            [clojure.pprint :as pp]
            [slingshot.slingshot :refer :all]
            [cheshire.core :refer :all]
            [clojure.data.json :as json]))

(def server "localhost:8080")

(defn create-test-user []
  (client/post (str "http://" server "/api/user")
               {;:basic-auth ["user" "pass"]
                 :throw-exceptions false
                 :body (generate-string {
                                          :firstName "testFirstName",
                                          :lastName "testLastName",
                                          :email "test@test.com",
                                          :password "testPassword"
                                          })
                 :headers {"Content-Type" "application/json"}
                 :content-type :application/json
                 :socket-timeout 1000  ;; in milliseconds
                 :conn-timeout 1000    ;; in milliseconds
                 :accept :json}))

;; Test User Management MongoDb

;; Setup database connection
(db/init)

;; Wipe the database
(services/test-remove)

(def user-id "")

(deftest test-user-operations
  (do
    (testing "user creation"
      (let [new-user (services/create-user "TestFirst" "TestLast" "TestEmail" "TestPassword")
            new-user-id (get-in new-user [:body :_id])]
        (def user-id new-user-id)
        (println "user-id: " user-id)
        ))

    (testing "get user by email"
      (let [user (services/get-user-by-email "TestEmail")
            expected-first-name "TestFirst"
            expected-last-name "TestLast"
            expected-email "TestEmail"
            expected-password "TestPassword"]
        ; (pp/pprint user)
        (is (= expected-first-name (:firstName user)))
        (is (= expected-last-name (:lastName user)))
        (is (= expected-email (:email user)))
        (is (= expected-password (:password user)))))

    (testing "get user by id"
      (let [user (services/get-user-by-id user-id)
            expected-first-name "TestFirst"
            expected-last-name "TestLast"
            expected-email "TestEmail"
            expected-password "TestPassword"]
        ; (pp/pprint user)
        (is (= expected-first-name (:firstName user)))
        (is (= expected-last-name (:lastName user)))
        (is (= expected-email (:email user)))
        (is (= expected-password (:password user)))))

    (testing "update user"
      (let [updates {:firstName "UpdatedFirstName"}
            user (services/update-user user-id updates)
            expected-first-name "UpdatedFirstName"
            expected-last-name "TestLast"
            expected-email "TestEmail"
            expected-password "TestPassword"]
        ; (pp/pprint user)
        (is (= expected-first-name (:firstName user)))
        (is (= expected-last-name (:lastName user)))
        (is (= expected-email (:email user)))
        (is (= expected-password (:password user)))))

    (testing "delete user"
      (let [user (services/delete-user user-id)
            expected-active false
            ]
        (is (= expected-active (:isActive user)))))))



;; Test User API
;; (deftest user-api-post
;;   (do
;;     ; Wipe the database
;;     (client/get "http://localhost:8080/test-remove")

;;     (testing "POST api/user"
;;       (let [response (create-test-user)
;;             body (parse-string (get-in response [:body]) true)]
;;         ; (pp/pprint response)
;;         (is (= (:email     body) "test@test.com"))
;;         (is (= (:firstName body) "testFirstName"))
;;         (is (= (:lastName  body) "testLastName"))
;;         (is (= (:password  body) "testPassword"))))

;;     ;; Verify two accounts can't have the same email address
;;     (testing "POST api/user"
;;       (let [response (create-test-user)
;;             body (parse-string (:body response) true)
;;             message (:message (:body body))]
;;         ; This gets paresed out wierd for some reason
;;         ; (pp/pprint body)
;;         (is (= message "Conflict: User Creation: Email Already Exists"))
;;         (is (= (:status 409)))))))

;; (def user-api-get
;;   (do
;;     ; Wipe the database
;;     (client/get "http://localhost:8080/test-remove")

;;     (testing "GET api/user with no users"
;;       (let [response (client/get (str "http://" server "/api/user"))]
;;         (is (= (response "[]")))))

;;     (create-test-user)

;;     (testing "GET api/user"
;;       (let [response (client/get (str "http://" server "/api/user"))
;;             body (first (parse-string (get-in response [:body]) true))]
;;         ; (pp/pprint response)
;;         (is (= (:email     body) "test@test.com"))
;;         (is (= (:firstName body) "testFirstName"))
;;         (is (= (:lastName  body) "testLastName"))))

;;     (testing "GET api/user one user"
;;       (let [response (client/get (str "http://" server "/api/user")
;;                                  {;:basic-auth ["user" "pass"]
;;                                    :throw-exceptions false
;;                                    :body (generate-string {:email "test@test.com"})
;;                                    :headers {"Content-Type" "application/json"}
;;                                    :content-type :application/json
;;                                    :socket-timeout 1000  ;; in milliseconds
;;                                    :conn-timeout 1000    ;; in milliseconds
;;                                    :accept :json})
;;             body (first (parse-string (get-in response [:body]) true))]
;;         (is (= (:email     body) "test@test.com"))
;;         (is (= (:firstName body) "testFirstName"))
;;         (is (= (:lastName  body) "testLastName"))))))


;; (def user-api-put
;;   (do
;;     ; Wipe the database
;;     (client/get "http://localhost:8080/test-remove")

;;     (create-test-user)

;;     (testing "PUT api/user"
;;       (let [test-user (client/get (str "http://" server "/api/user"))
;;             _id  (first (parse-string (get-in test-user [:body]) true))
;;             response (client/put (str "http://" server "/api/user")
;;                                  {;:basic-auth ["user" "pass"]
;;                                    :throw-exceptions false
;;                                    :body (generate-string {
;;                                                             :_id _id
;;                                                             :firstName "UtestFirstName"
;;                                                             :lastName "UtestLastName"
;;                                                             :email "Utest@test.com"
;;                                                             })
;;                                    :headers {"Content-Type" "application/json"}
;;                                    :content-type :application/json
;;                                    :socket-timeout 1000  ;; in milliseconds
;;                                    :conn-timeout 1000    ;; in milliseconds
;;                                    :accept :json})
;;             body (parse-string (get-in response [:body]) true)]
;;         (pp/pprint response)
;;         (pp/pprint body)
;;         (println "adsf" _id)
;;         (println "_id: " _id)
;;         (is (= (:email     body) "Utest@test.com"))
;;         (is (= (:firstName body) "UtestFirstName"))
;;         (is (= (:lastName  body) "UtestLastName"))
;;         (is (not (= (:password  body) "UtestPassword")))))))

;; (def user-api-delete
;;   (do
;;     ; Wipe the database
;;     (client/get "http://localhost:8080/test-remove")

;;     ;; Created test user has isActive flag set to true

;;     (testing "DELETE api/user"
;;       (let [test (create-test-user)
;;             response (client/delete (str "http://" server "/api/user")
;;                                     {;:basic-auth ["user" "pass"]
;;                                       :throw-exceptions false
;;                                       :body (generate-string {:email "test@test.com"})
;;                                       :headers {"Content-Type" "application/json"}
;;                                       :content-type :application/json
;;                                       :socket-timeout 1000  ;; in milliseconds
;;                                       :conn-timeout 1000    ;; in milliseconds
;;                                       :accept :json})
;;             body (parse-string (get-in response [:body]) true)]
;;         ;(pp/pprint response)
;;         ;(pp/pprint body)
;;         (is (= (:email     body) "test@test.com"))
;;         (is (= (:firstName body) "testFirstName"))
;;         (is (= (:lastName  body) "testLastName"))
;;         (is (= (:isActive  body) false))))))

