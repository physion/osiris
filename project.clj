(defproject osiris "0.1.0-SNAPSHOT"
  :description "Ovation update handler"
  :url "http://ovation.io"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.incubator "0.1.3"]

                 [metosin/compojure-api "0.18.0"]

                 [ring/ring-servlet "1.3.1"]
                 [javax.servlet/servlet-api "2.5"]


                 [com.ashafa/clutch "0.4.0"]
                 [com.cemerick/url "0.1.1"]                 ;; For clutch

                 [com.newrelic.agent.java/newrelic-agent "3.11.0"] ;; NB Update javaagent string
                 [com.newrelic.agent.java/newrelic-api "3.11.0"] ;; NB Update javaagent string
                 [com.climate/clj-newrelic "0.1.1"]

                 [ring/ring-codec "1.0.0"]
                 [org.clojure/data.json "0.2.5"]

                 [org.clojure/tools.logging "0.3.1"]
                 [clj-logging-config "1.9.12"]
                 [org.slf4j/slf4j-api "1.7.7"]
                 [org.slf4j/slf4j-log4j12 "1.7.7"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]

                 [com.cemerick/bandalore "0.0.6"]]

  ;:java-agents [[com.newrelic.agent.java/newrelic-agent "3.9.0"]]

  :plugins [[lein-ring "0.8.12"]
            [lein-midje "3.1.3"]
            [lein-elastic-beanstalk "0.2.8-SNAPSHOT"]
            [lein-awsuberwar "0.1.0"]
            [lein-kibit "0.0.8"]]

  :ring {:handler osiris.handler/app
         :init osiris.handler/init}


  ;; For New Relic, we need to bundle newrelic.yml and newrelic.jar
  :war-resources-path "war-resources"

  :aws {:beanstalk {:stack-name   "64bit Amazon Linux running Tomcat 7"
                    :environments [{:name  "osiris-development"
                                    :alias "development"}

                                   {:name  "osiris-production"
                                    :alias "production"}]}}

  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring-mock "0.1.5"]
                                      [midje "1.6.3"]
                                      [ring-serve "0.1.2"]]}
             :jenkins {:aws {:access-key ~(System/getenv "AWS_ACCESS_KEY")
                             :secret-key ~(System/getenv "AWS_SECRET_KEY")}}})
