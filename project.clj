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


                 [com.ashafa/clutch "0.4.0"]
                 [com.cemerick/url "0.1.1"]                 ;; For clutch
                 ]

  ;:java-agents [[com.newrelic.agent.java/newrelic-agent "3.9.0"]]

  :plugins [[lein-ring "0.8.12"]
            [lein-midje "3.1.3"]
            [lein-elastic-beanstalk "0.2.8-SNAPSHOT"]]

  :ring {:handler osiris.handler/app}

  ;; AWS_INSTANCE_PROFILE, AWS_SSL_CERT, AWS_EB_VPC_ID, SQS_URL, NEW_RELIC_LICENSE_KEY
  :aws {:beanstalk {:stack-name   "64bit Amazon Linux running Tomcat 7"
                    :environments [{:name    "osiris-development"
                                    :alias   "development"
                                    :env     {"OVATION_IO_HOST_URI" "https://dev.ovation.io"
                                              "NEWRELIC"            (or (System/getenv "NEW_RELIC_LICENSE_KEY") "newrelic_license_key")}
                                    :options {"aws:autoscaling:asg"                 {"MinSize" "1"
                                                                                     "MaxSize" "1"}

                                              "aws:autoscaling:launchconfiguration" {"IamInstanceProfile" (System/getenv "AWS_INSTANCE_PROFILE")
                                                                                     "InstanceType"       "t2.micro"}

                                              "aws:elb:loadbalancer"                {"SSLCertificateId"      (System/getenv "AWS_SSL_CERT")
                                                                                     "LoadBalancerHTTPSPort" "443"}

                                              "aws:ec2:vpc"                         {"VPCId" (System/getenv "AWS_EB_VPC_ID")}

                                              "aws:elasticbeanstalk:sqsd"           {"WorkerQueueURL" (System/getenv "SQS_URL")
                                                                                     "HttpPath"       "/update"}

                                              }}

                                   {:name    "osiris-production"
                                    :alias   "production"
                                    :env     {"OVATION_IO_HOST_URI" "https://ovation.io"
                                              "NEWRELIC"            (or (System/getenv "NEW_RELIC_LICENSE_KEY") "newrelic_license_key")}

                                    :options {"aws:autoscaling:asg"                 {"MinSize" "2"
                                                                                     "MaxSize" "5"}

                                              "aws:autoscaling:launchconfiguration" {"IamInstanceProfile" (System/getenv "AWS_INSTANCE_PROFILE")
                                                                                     "InstanceType"       "t2.micro"}

                                              "aws:elb:loadbalancer"                {"SSLCertificateId"      (System/getenv "AWS_SSL_CERT")
                                                                                     "LoadBalancerHTTPSPort" "443"}

                                              "aws:ec2:vpc"                         {"VPCId" (System/getenv "AWS_EB_VPC_ID")}}}]}}

  :profiles {:dev     {:dependencies [[javax.servlet/servlet-api "2.5"]
                                      [ring-mock "0.1.5"]
                                      [midje "1.6.3"]]}
             :jenkins {:aws {:access-key ~(System/getenv "AWS_ACCESS_KEY")
                             :secret-key ~(System/getenv "AWS_SECRET_KEY")}}})
