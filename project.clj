(defproject pepinito "0.0.2"
  :description "An implementation of a subset of Python's pickle format."
  :url "http://github.com/ulises/pepinito"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]
                   :plugins [[lein-midje "3.0.0"]
                             [lein-kibit "0.0.8"]]}}
  :global-vars {*warn-on-reflection* true}
  :deploy-branches ["master"]
  :repositories [["releases" {:url "https://clojars.org/repo"
                              :creds :gpg}]
                 ["snapshots" {:url "https://clojars.org/repo"
                               :creds :gpg}]])
