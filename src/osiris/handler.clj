(ns osiris.handler
  (:import (com.sun.xml.internal.bind.v2.model.core ID))
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]
            [osiris.updates :refer [process]]
            [osiris.schema :refer [NewUpdate]]
            [clojure.tools.logging :as logging]
            [osiris.logging]))

(defn init
  "Servlet init"
  []
  (osiris.logging/setup!)
  (logging/debug "Logging configured"))


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

      (let [__ (logging/info "Update received")
            update-info (-> update
                          (assoc :sqs-msgid x-aws-sqsd-msgid)
                          (assoc :sqs-queue x-aws-sqsd-queue)
                          (assoc :sqs-first-received-at x-aws-sqsd-first-received-at)
                          (assoc :sqs-receive-count (Integer/parseInt x-aws-sqsd-receive-count)))
            _ (logging/info "Update received for" (:database update-info) "(" (:sqs-msgid update-info) ")")
            result (process update-info)]
        (logging/info "Result" result (str result))
        (ok {:success true})))))                            ;;:messages (flatten result)
