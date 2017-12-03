(defproject website-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]
                 [com.novemberain/monger "3.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [cheshire "5.8.0"]
                 [buddy/buddy-auth "2.1.0"]]
  :main ^:skip-aot website-api.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
