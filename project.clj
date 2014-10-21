(defproject osiris "0.1.0-SNAPSHOT"
  :description "Ovation update handler"
  :url "http://ovation.io"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.incubator "0.1.3"]

                 [compojure "1.1.8"]
                 [metosin/compojure-api "0.16.2"]
                 [prismatic/schema "0.2.4"]
                 [prismatic/plumbing "0.3.3"]
                 [metosin/ring-swagger "0.13.0"]
                 [metosin/ring-swagger-ui "2.0.17"]
                 [metosin/ring-http-response "0.5.0"]

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
                 [org.slf4j/slf4j-api "1.7.7"]
                 [org.slf4j/slf4j-log4j12 "1.7.7"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 ]

  ;:java-agents [[com.newrelic.agent.java/newrelic-agent "3.9.0"]]

  :plugins [[lein-ring "0.8.12"]
            [lein-midje "3.1.3"]
            [lein-elastic-beanstalk "0.2.8-SNAPSHOT"]]

  :ring {:handler osiris.handler/app}

  ;; For New Relic, we need to bundle newrelic.yml and newrelic.jar
  :war-resources-path "war_resources"

  :aws {:beanstalk {:stack-name   "64bit Amazon Linux running Tomcat 7"
                    :environments [{:name  "osiris-development"
                                    :alias "development"
                                    :env   {"OVATION_IO_HOST_URI"   "https://dev.ovation.io"}}

                                   {:name    "osiris-production"
                                    :alias   "production"
                                    :env     {"OVATION_IO_HOST_URI" "https://ovation.io"}}]}}

  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring-mock "0.1.5"]
                                      [midje "1.6.3"]]}
             :jenkins {:aws {:access-key ~(System/getenv "AWS_ACCESS_KEY")
                             :secret-key ~(System/getenv "AWS_SECRET_KEY")}}})
