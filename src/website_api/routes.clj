(ns website-api.routes
  (:require [compojure.core :as compojure]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.data.json :as json]
            [monger.json]
            [website-api.services :as services]))

(compojure/defroutes app
    (compojure/GET "/" [] {:body "Hello World!"
                           :status 200
                           :headers {"Content-Type" "text/plain"}})
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
