(ns website-api.services-test
  (require [cheshire.core :as cheshire]
           [clojure.test :refer :all]
           [clojure.pprint :as pp]
           [clojure.data :as data]
           [clojure.data.json :as json]
           [clj-http.client :as client]
           [website-api.db-config :as db]
           [website-api.core :refer :all]
           [website-api.services :as services]
           [website-api.server-config :as server]))


(def test "hello world")

(deftest test-create-user-db
  (testing "DB User Creation"
    (services/test-remove)
    ;; (pp/pprint (services/get-users))
    (let [new-user-pass (services/create-user-db "First" "Last" "Email" "Password")
          user-id (get-in new-user-pass [:body :collection :items 0 :data 0 :value])
          new-user-fail (services/create-user-db "First" "Last" "Email" "Password")
          new-user-pass-expected
          {:status 200
           :headers {"ContentType" "application/vnd.collection+json"}
           :body {:collection
                  {:version 1.0
                   :href (str server/addr "/api/user")
                   :links []
                   :items [{:href (str server/addr "/api/user/" user-id)
                            :data [{:name "id" :value user-id :prompt "User Id"}
                                   {:name "firstName" :value "First" :prompt "First Name"}
                                   {:name "lastName" :value "Last" :prompt "Last Name"}
                                   {:name "email" :value  "Email" :prompt "Email Address"}]
                           :links []}]}}}
      
          new-user-fail-expected {:status 409
                                  :headers {"ContentType" "application/vnd.collection+json"}
                                  :body {:collection
                                         {:version 1.0,
                                          :href (str server/addr "/api/user")
                                          :error {:title "User Creation Failure"
                                                  :message "Email Already Exists"
                                                  :code ""}}}}]
      ;; (pp/pprint new-user-fail)
      ;; (pp/pprint new-user-fail-expected)
      (is (= new-user-fail new-user-fail-expected))
      ;; (pp/pprint (data/diff new-user-pass new-user-pass-expected))
      (is (= new-user-pass new-user-pass-expected)))))

;; This should be added to lein test.
;; Must have server running to test.
(deftest test-api-create-user
  (testing "API User Creation"
    (services/test-remove)
    (client/post (str "http://" server/addr "/api/user")
                 {;:basic-auth ["user" "pass"]
                  :throw-exceptions false
                  :body (cheshire/generate-string
                         {:template
                          { :data [{:firstName "First"
                                    :lastName "Last"
                                    :email "Email"
                                    :password "Password"}]}})
                  :headers {"Content-Type" "application/vnd.collection+json"}
                  :socket-timeout 1000  ;; in milliseconds
                  :conn-timeout 1000    ;; in milliseconds
                  :accept :json})))


(deftest test-get-user-by-id-db
  (testing "DB Get User By ID"
    (services/test-remove)
    (let [user (services/create-user-db "First" "Last" "Email" "Password")
          user-id (get-in user [:body :collection :items 0 :data 0 :value])
          retrieved-user (services/get-user-by-id-db user-id)
          retrieved-user-expected {:status 200,
                                   :headers {"ContentType" "application/vnd.collection+json"},
                                   :body
                                   {:collection
                                    {:version 1.0,
                                     :href "http://http://localhost:8080/api/user",
                                     :links [],
                                     :items
                                     [{:href
                                       (str "http://http://localhost:8080/api/user/" user-id),
                                       :data
                                       [{:name "id",
                                         :value user-id,
                                         :prompt "User Id"}
                                        {:name "firstName", :value "First", :prompt "First Name"}
                                        {:name "lastName", :value "Last", :prompt "Last Name"}
                                        {:name "email", :value "Email", :email "Email Address"}]}
                                      :links
                                      []]}}}]
      (is (= retrieved-user retrieved-user-expected))
      )))


(deftest test-get-user-by-email-db
  (testing "DB Get User By Email"
    (services/test-remove)
    (let [user (services/create-user-db "First" "Last" "Email" "Password")
          user-id (get-in user [:body :collection :items 0 :data 0 :value])
          user-email (get-in user [:body :collection :items 0 :data 3 :value])
          retrieved-user (services/get-user-by-email-db user-email)
          retrieved-user-expected {:status 200,
                                   :headers {"ContentType" "application/vnd.collection+json"},
                                   :body
                                   {:collection
                                    {:version 1.0,
                                     :href "http://http://localhost:8080/api/user",
                                     :links [],
                                     :items
                                     [{:href
                                       (str "http://http://localhost:8080/api/user/" user-id),
                                       :data
                                       [{:name "id",
                                         :value user-id,
                                         :prompt "User Id"}
                                        {:name "firstName", :value "First", :prompt "First Name"}
                                        {:name "lastName", :value "Last", :prompt "Last Name"}
                                        {:name "email", :value "Email", :email "Email Address"}]}
                                      :links
                                      []]}}}]
      ;; (pp/pprint (data/diff retrieved-user retrieved-user-expected))
      (is (= retrieved-user retrieved-user-expected))
      )))

(deftest test-get-all-users-db
  (testing "DB Get All Active Users"
    (services/test-remove)
    (let [user1 (services/create-user-db "First1" "Last1" "Email1" "Password1")
          user2 (services/create-user-db "First2" "Last2" "Email2" "Password2")]
      (pp/pprint (services/get-users)))))
          


(deftest test-build-user-items  
  (testing "Build User Items"
    (services/test-remove)
    (let [user1 (services/create-user-db "First1" "Last1" "Email1" "Password1")
          user2 (services/create-user-db "First2" "Last2" "Email2" "Password2")
          users (services/get-users)]
      (pp/pprint (services/build-user-items users)))))
          
