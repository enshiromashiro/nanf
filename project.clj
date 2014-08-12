(defproject nanf "0.1"
  :description "front-end for nan"
  :url "http://bitbucket.org/subaru45/nanf"
  :license {:name "NYSL"
            :url "http://www.kmonos.net/nysl/"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main ^:skip-aot nanf.core
  :resouce-paths ["res"]
  :target-path "target/%s"
  :manifest {"SplashScreen-Image" "splash.png"}
  :profiles {:uberjar {:aot :all}})
