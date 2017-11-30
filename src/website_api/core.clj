(ns website-api.core
  (:gen-class)
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.pprint :as pp]
                        [monger.json]
            [clojure.data.json :as json])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]))

;(let [conn (mg/connect)])


(defn test-write []
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"]
    (mc/insert-and-return db coll { :_id (ObjectId.) :username "ASDFADSFSAD" })))

(defn test-read []
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"
        stuff (mc/find-maps db coll)]
    (pp/pprint (:_id (first stuff)))
    stuff))

(defn test-remove []
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"]
    (mc/remove db coll)))

(compojure/defroutes app
    (compojure/GET "/" [] {:body "Hello World!"
                           :status 200
                           :headers {"Content-Type" "text/plain"}})
    (compojure/GET "/test-write" [] {:body (str (test-write))
                               :status 200
                                     :headers {"Content-Type" "text/plain"}})
    (compojure/GET "/test-read" [] {:body (json/write-str (test-read))
                                    :status 200
                                    :headers {"Content-Type" "text/plain"}})
    (compojure/GET "/test-remove" [] {:body (str (test-remove))
                               :status 200
                               :headers {"Content-Type" "text/plain"}})  
    (route/resources "/")
    (route/not-found "Page not found"))

(def wrapped-app (-> app
                     (wrap-defaults site-defaults)))

(defn -main []
  (jetty/run-jetty #'wrapped-app {:join? false :port 8080}))
