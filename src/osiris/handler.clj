(ns osiris.handler
  (:import (com.sun.xml.internal.bind.v2.model.core ID))
  (:require [clojure.string :refer [join]]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]
            [osiris.updates :refer [process]]))

;; --- Schema --- ;;
(s/defschema Success {:success s/Bool})
(s/defschema NewUpdate {:database s/Str})

;; --- Routes --- ;;
(defapi app
        (swaggered "osiris"
                   (HEAD* "/" []
                          (ok ""))
                   (POST* "/updates" []
                          :body [update NewUpdate]
                          :summary "Processes an update from Aker"
                          :header-params [x-aws-sqsd-msgid :- s/Str
                                          x-aws-sqsd-queue :- s/Str
                                          x-aws-sqsd-first-received-at :- s/Str
                                          x-aws-sqsd-receive-count :- s/Str]

                          (let [update-info (-> update
                                                (assoc :sqs-msgid x-aws-sqsd-msgid)
                                                (assoc :sqs-queue x-aws-sqsd-queue)
                                                (assoc :sqs-first-received-at x-aws-sqsd-first-received-at)
                                                (assoc :sqs-receive-count (Integer/parseInt x-aws-sqsd-receive-count)))]

                            (ok (process update-info))))))
