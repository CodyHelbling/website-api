(ns website-api.core-test
  (:require [clojure.test :refer :all]
            [website-api.core :refer :all]
            [clj-http.client :as client]
            [clojure.pprint :as pp]
            [slingshot.slingshot :refer :all]
            [cheshire.core :refer :all]))

;; (deftest a-test
;;   (testing "FIXME, I fail."
;;     (is (= 0 1))))

(def server "localhost:8080")

;; Test User API
(deftest user-post
  (do
    ; Wipe the database
    (client/get "http://localhost:8080/test-remove")

    (testing "POST api/user"
      (let [response (client/post (str "http://" server "/api/user")
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
                                    :accept :json})
            body (parse-string (get-in response [:body]) true)]
        ; (pp/pprint response)
        (is (= (:email     body) "test@test.com"))
        (is (= (:firstName body) "testFirstName"))
        (is (= (:lastName  body) "testLastName"))
        (is (not (= (:password  body) "testPassword")))))

    ;; Verify two accounts can't have the same email address
    (testing "POST api/user"
      (let [response (client/post (str "http://" server "/api/user")
                                  {;:basic-auth ["user" "pass"]
                                    :throw-exceptions false
                                    :body (generate-string {
                                                             :firstName "testFirstName",
                                                             :lastName "testLastName",
                                                             :email "test@test.com",
                                                             :password "testPassword"
                                                             })
                                    :headers {}
                                    :content-type :application/json
                                    :socket-timeout 1000  ;; in milliseconds
                                    :conn-timeout 1000    ;; in milliseconds
                                    :accept :json})
            body (parse-string (get-in response [:body]) true)]
        (is (= (:message body) "Conflict: User Creation: Email Already Exists"))
        (is (= (:status 409)))))))
