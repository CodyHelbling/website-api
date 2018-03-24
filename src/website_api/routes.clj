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
  (get-in request [:body :password]))1

(def please-authenticate
  {:body (json/write-str {:message "Please Authenticate!"})
   :status 401
   :headers {"Content-Type" "application/json"}})

(compojure/defroutes app
  (compojure/GET "/api/login" request
                 (println "Route: /api/login")
                 (pprint/pprint request)
                 (if (authenticated? request)
                   {:body (str "Hello " (:identity request) "!")
                    :status 200
                    :headers {"Content-Type" "text/plain"}}
                   {:body "Hello Anonymous!"
                    :status 200
                    :headers {"Content-Type" "text/plain"}}))
  ;; User API

  ;; ------ This Endpoint Needs to Be removed in Prod ---------------
  (compojure/OPTIONS "/api/user" request
                 (println "Route: OPTIONS /api/user")
                 (json/write-str {"ContentType" "application/json"
                                  "Access-Control-Allow-Origin" "*"
                                  "Access-Control-Allow-Headers" "*"
                                  "Access-Control-Max-Age" "*"}))

  (compojure/OPTIONS "/api/login" request
                 (println "Route: OPTIONS /api/user")
                 (json/write-str {"ContentType" "application/json"
                                  "Access-Control-Allow-Origin" "*"
                                  "Access-Control-Allow-Headers" "*"
                                  "Access-Control-Max-Age" "*"}))
  
(compojure/OPTIONS "/api/user/:id" request
                 (println "Route: OPTIONS /api/user/:id")
                 (json/write-str {"ContentType" "application/json"
                                  "Access-Control-Allow-Origin" "*"
                                  "Access-Control-Allow-Headers" "*"
                                  "Access-Control-Max-Age" "*"}))
  
  ;; --------------------------------------------------------------
  
  (compojure/POST "/api/user" request
                  (println "Route: POST /api/user")
                 ;; (if (authenticated? request)
                   (services/create-user request)
                   ;; please-authenticate)
                   )

  (compojure/GET "/api/user" request
                 (println "Route: GET /api/user")
                 ;;(if (authenticated? request)
                     (services/get-users)
                   ;;  please-authenticate))
                     ;; {:body "Hello Anonymous!"
                     ;;  :status 200
                     ;;  :headers {"Content-Type" "text/plain"}}))
  )
  (compojure/GET "/api/user/:id" [_id]
                 (println "Route: GET /api/user/:id")
                 (services/get-user-by-id _id))

  (compojure/PUT "/api/user/:id" [id :as request]
                 (println "Route: PUT /api/user/:id")
                 (pprint/pprint request)
                 (if (authenticated? request)
                   (services/update-user id (get-in request [:body]))
                   please-authenticate))

  (compojure/DELETE "/api/user" request
                    (println "Route: DELETE /api/user")
                    (if (authenticated? request)
                      (services/delete-user (get-in request [:body :id]))
                      please-authenticate))

  ;; Test Endpoints
  (compojure/POST "/api/login" [] login)
  (compojure/GET "/test" [] "test")
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
