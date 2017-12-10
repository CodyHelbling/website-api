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
  (compojure/POST "/api/user" request
                  (println "Route: /api/user")
                  (pprint/pprint request)
                  (services/create-user request))
  (compojure/GET "/api/user" request
                 (json/write-str (services/get-users)))
  (compojure/GET "/api/user/:email" [email]
                 (json/write-str (services/get-user email)))
  ;; /api/user/  PUT
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
