(ns website-api.core
  (:gen-class)
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(compojure/defroutes app
  (compojure/GET "/" [] {:body "Hello World!"
                         :status 200
                         :headers {"Content-Type" "text/plain"}})
  (compojure/GET "/test" [] {:body "This is a test!"
                             :status 200
                             :headers {"Content-Type" "text/plain"}})
  (route/resources "/")
  (route/not-found "Page not found"))

(def wrapped-app (-> app
                     (wrap-defaults site-defaults)))

(defn -main []
  (jetty/run-jetty #'wrapped-app {:join? false :port 8080}))
