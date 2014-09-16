(defproject osiris "0.1.0-SNAPSHOT"
            :description "Ovation update handler"
            :url "http://ovation.io"

            :dependencies [[org.clojure/clojure "1.6.0"]
                           [compojure "1.1.8"]
                           [metosin/compojure-api "0.16.2"]
                           [com.newrelic.agent.java/newrelic-agent "3.9.0"]]

            :plugins [[lein-ring "0.8.11"]]

            :ring {:handler osiris.handler/app}

            :aws {:beanstalk {:stack-name   "64bit Amazon Linux running Tomcat 7"
                              :environments [{:name    "osiris-dev"
                                              :env     {"OVATION_IO_HOST_URI" "https://dev.ovation.io"
                                                        "NEWRELIC"            (or (System/getenv "NEW_RELIC_LICENSE_KEY") "newrelic_license_key")}
                                              :options {"aws:autoscaling:asg"                 {"MinSize" "1"
                                                                                               "MaxSize" "1"}
                                                        "aws:autoscaling:launchconfiguration" {"IamInstanceProfile" (System/getenv "AWS_INSTANCE_PROFILE")
                                                                                               "InstanceType"       "t2.micro"}
                                                        "aws:elb:loadbalancer"                {"SSLCertificateId"      (System/getenv "AWS_SSL_CERT")
                                                                                               "LoadBalancerHTTPSPort" "443"}}}

                                             {:name    "osiris-production"
                                              :env     {"OVATION_IO_HOST_URI" "https://ovation.io"
                                                        "NEWRELIC"            (or (System/getenv "NEW_RELIC_LICENSE_KEY") "newrelic_license_key")}

                                              :options {"aws:autoscaling:asg"                 {"MinSize" "2"
                                                                                               "MaxSize" "5"}
                                                        "aws:autoscaling:launchconfiguration" {"IamInstanceProfile" (System/getenv "AWS_INSTANCE_PROFILE")
                                                                                               "InstanceType"       "t2.micro"}
                                                        "aws:elb:loadbalancer"                {"SSLCertificateId"      (System/getenv "AWS_SSL_CERT")
                                                                                               "LoadBalancerHTTPSPort" "443"}}}]}}

            :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                                [ring-mock "0.1.5"]
                                                [midje "1.6.3"]]}
                       :jenkins {:aws {:access-key ~(System/getenv "AWS_ACCESS_KEY")
                                       :secret-key ~(System/getenv "AWS_SECRET_KEY")}}})
