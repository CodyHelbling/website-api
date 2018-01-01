(ns website-api.routes
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.pprint :as pprint]
            [clojure.data.json :as json]
            [monger.json]
            [website-api.services :as services]
            [clj-time.core :as time]
            [buddy.sign.jwt :as jwt]
            [buddy.auth :refer [authenticated? throw-unauthorized]]))

(def authdata
  {:admin "secret"
   :test "secret"})

(def secret "mysupersecret")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Semantic response helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})

;; Authenticate Handler
;; Responds to post requests in same url as login and is responsible for
;; identifying the incoming credentials and setting the appropriate authenticated
;; user into session. `authdata` will be used as source of valid users.

(defn login
  [request]
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        valid? (some-> authdata
                       (get (keyword username))
                       (= password))]
    (if valid?
      (let [claims {:user (keyword username)
                    :exp (time/plus (time/now) (time/seconds 3600))}
            token (jwt/sign claims secret {:alg :hs512})]
        (ok {:token token}))
      (bad-request {:message "wrong auth data"}))))


(defn get-first-name [request]
  (get-in request [:body :firstName]))

(defn get-last-name [request]
  (get-in request [:body :lastName]))

(defn get-email [request]
  (get-in request [:body :email]))

(defn get-password [request]
  (get-in request [:body :password]))

(compojure/defroutes app
  (compojure/GET "/" request
                 (println "Route: /")
                 (pprint/pprint request)
                 (if (authenticated? request)
                   {:body (str "Hello " (:identity request) "!")
                    :status 200
                    :headers {"Content-Type" "text/plain"}}
                   {:body "Hello Anonymous!"
                    :status 200
                    :headers {"Content-Type" "text/plain"}}))
  ;; User API
  (compojure/POST "/api/user" request
                  (println "Route: POST /api/user")
                  (services/create-user request))

  (compojure/GET "/api/user" request
                 (println "Route: GET /api/user")
                 (json/write-str (services/get-users)))
  (compojure/GET "/api/user/:id" [_id]
                 (println "Route: GET /api/user/:id")
                 (json/write-str (services/get-user-by-id _id)))
  (compojure/PUT "/api/user" request
                 (println "Route: PUT /api/user")
                 (json/write-str (services/update-user
                                   (get-in request [:body :_id]))))
  (compojure/DELETE "/api/user" request
                    (println "Route: DELETE /api/user")
                    (json/write-str (services/delete-user request)))


  ;; Product API

  ;; Test Endpoints
  (compojure/POST "/login" [] login)
  (compojure/GET "/test-write" [] {:body (json/write-str (services/test-write))
                                   :status 200
                                   :headers {"Content-Type" "text/plain"}})
  (compojure/GET "/test-read" [] {:body (json/write-str (services/test-read))
                                  :status 200
                                  :headers {"Content-Type" "text/plain"}})
  (compojure/GET "/test-remove" [] {:body (str (services/test-remove))
                                    :status 200
                                    :headers {"Content-Type" "text/plain"}})
  (route/resources "/")
  (route/not-found "Page not found"))
