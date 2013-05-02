(defproject pepinito "0.0.1-SNAPSHOT"
  :description "An implementation of a subset of Python's pickle format."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.6"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]
                   :plugins [[lein-midje "3.0.0"]
                             [lein-kibit "0.0.8"]]}}
  :warn-on-reflection true)
