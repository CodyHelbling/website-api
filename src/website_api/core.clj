(ns website-api.core
  (:gen-class)
  (:require [website-api.routes :as routes]
            [website-api.services :as services]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def wrapped-app (-> routes/app
                     (wrap-defaults site-defaults)))

(defn -main []
  (jetty/run-jetty #'wrapped-app {:join? false :port 8080}))
