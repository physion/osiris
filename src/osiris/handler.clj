(ns osiris.handler
  (:import (com.sun.xml.internal.bind.v2.model.core ID))
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]
            [schema.coerce :as coerce]
            [osiris.updates :refer [process]]
            [osiris.schema :refer [NewUpdate]]
            [clj-logging-config.log4j :as log-config]
            [clojure.tools.logging :as logging]))


(if-let [papertrail-host (System/getProperty LOGGING_HOST)]
  (log-config/set-logger!
    :level :debug
    :out (org.apache.log4j.net.SyslogAppender.
           (org.apache.log4j.PatternLayout. "%p: (%F:%L) %x %m %n")
           LOGGING_HOST
           org.apache.log4j.net.SyslogAppender/LOG_LOCAL7)))


;; --- Routes --- ;;
(defapi app
  (swaggered "osiris"
    (HEAD* "/" []
      (ok ""))
    (GET* "/" []
      (ok "Osiris!"))

    (POST* "/updates" []
      :body [update NewUpdate]
      :summary "Processes an update from Aker"
      :header-params [x-aws-sqsd-msgid :- s/Str
                      x-aws-sqsd-queue :- s/Str
                      x-aws-sqsd-first-received-at :- s/Str
                      x-aws-sqsd-receive-count :- s/Str]

      (logging/debug update)
      (let [update-info (-> update
                          (assoc :sqs-msgid x-aws-sqsd-msgid)
                          (assoc :sqs-queue x-aws-sqsd-queue)
                          (assoc :sqs-first-received-at x-aws-sqsd-first-received-at)
                          (assoc :sqs-receive-count (Integer/parseInt x-aws-sqsd-receive-count)))]

        (logging/info "System properties:" (System/getProperties))
        (logging/info "Env:" (System/getenv))

        (ok (process update-info))))))
