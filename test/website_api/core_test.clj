(ns website-api.core-test
  (:require [clojure.test :refer :all]
            [website-api.core :refer :all]
            [clj-http.client :as client]
            [clojure.pprint :as pp]
            [slingshot.slingshot :refer :all]
            [cheshire.core :refer :all]))

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

;; Test User API
(deftest user-api-post
  (do
    ; Wipe the database
    (client/get "http://localhost:8080/test-remove")

    (testing "POST api/user"
      (let [response (create-test-user)
            body (parse-string (get-in response [:body]) true)]
        ; (pp/pprint response)
        (is (= (:email     body) "test@test.com"))
        (is (= (:firstName body) "testFirstName"))
        (is (= (:lastName  body) "testLastName"))
        (is (not (= (:password  body) "testPassword")))))

    ;; Verify two accounts can't have the same email address
    (testing "POST api/user"
      (let [response (create-test-user)
            body (parse-string (get-in response [:body]) true)]
        (is (= (:message body) "Conflict: User Creation: Email Already Exists"))
        (is (= (:status 409)))))))

(def user-api-get
  (do
    ; Wipe the database
    (client/get "http://localhost:8080/test-remove")

    (testing "GET api/user with no users"
      (let [response (client/get (str "http://" server "/api/user"))]
        (is (= (response "[]")))))

    (create-test-user)

    (testing "GET api/user"
      (let [response (client/get (str "http://" server "/api/user"))
            body (parse-string (get-in response [:body]) true)]
        (is (= (:email     body) "test@test.com"))
        (is (= (:firstName body) "testFirstName"))
        (is (= (:lastName  body) "testLastName"))
        (is (not (= (:password  body) "testPassword")))))

;;     (testing "GET api/user one user"
;;       (let [response (client/get (str "http://" server "api/user")
;;                                  {;:basic-auth ["user" "pass"]
;;                                    :throw-exceptions false
;;                                    :body (generate-string {:email "test@test.com"})
;;                                    :headers {"Content-Type" "application/json"}
;;                                    :content-type :application/json
;;                                    :socket-timeout 1000  ;; in milliseconds
;;                                    :conn-timeout 1000    ;; in milliseconds
;;                                    :accept :json})
;;             body (parse-string (get-in response [:body]) true)]
;;         (is (= (:email     body) "test@test.com"))
;;         (is (= (:firstName body) "testFirstName"))
;;         (is (= (:lastName  body) "testLastName"))))
    ))


    (def user-api-put
      (do
        ; Wipe the database
        (client/get "http://localhost:8080/test-remove")

        (testing "PUT api/user"
          (let [test (create-test-user)
                response (client/put (str "http://" server "/api/user")
                                     {;:basic-auth ["user" "pass"]
                                       :throw-exceptions false
                                       :body (generate-string {
                                                                :firstName "UtestFirstName",
                                                                :lastName "UtestLastName",
                                                                :email "Utest@test.com",
                                                                :password "UtestPassword"
                                                                })
                                       :headers {"Content-Type" "application/json"}
                                       :content-type :application/json
                                       :socket-timeout 1000  ;; in milliseconds
                                       :conn-timeout 1000    ;; in milliseconds
                                       :accept :json})
                body (parse-string (get-in response [:body]) true)]
            (pp/pprint response)
            (is (= (:email     body) "Utest@test.com"))
            (is (= (:firstName body) "UtestFirstName"))
            (is (= (:lastName  body) "UtestLastName"))
            (is (not (= (:password  body) "UtestPassword")))))))

