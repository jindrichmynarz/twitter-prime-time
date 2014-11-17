(defproject twitter-prime-time "0.1.0-SNAPSHOT"
  :description "Find out when is your prime time on Twitter."
  :url "http://github.com/jindrichmynarz/twitter-prime-time"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [twitter-api "0.7.7"]
                 [environ "0.5.0"]
                 [clj-time "0.7.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [incanter/incanter-core "1.5.5"]
                 [incanter/incanter-charts "1.5.5"]]
  :main twitter-prime-time.core
  :profiles {:uberjar {:aot :all}})
