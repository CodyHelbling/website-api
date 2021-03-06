(ns website-api.core
  (:gen-class)
  (:require [website-api.routes :as routes]
            [website-api.services :as services]
            [website-api.db-config :as db]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]

            [cheshire.core :as json]
            [buddy.sign.jwt :as jwt]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(def secret "mysupersecret")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Semantic response helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})

;; Global var that stores valid users with their
;; respective passwords.
(def authdata
  {:admin "secret"
   :test "secret"})

;; Define function that is responsible for authenticating requests.
;; In this case it receives a map with username and password and it
;; should return a value that can be considered a "user" instance
;; and should be a logical true.

(defn my-authfn
  [req {:keys [username password]}]
  (when-let [user-password (get authdata (keyword username))]
    (when (= password user-password)
      (keyword username))))

;; Create an instance of auth backend.
(def auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entry Point
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main
  [& args]
  (db/init)
  (as-> routes/app $
      (wrap-authorization $ auth-backend)
      (wrap-authentication $ auth-backend)
      (wrap-json-response $ {:pretty false})
      (wrap-json-body $ {:keywords? true :bigdecimals? true})
      (jetty/run-jetty $ {:join? false :port 8080})))
