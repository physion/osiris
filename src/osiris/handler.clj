(ns osiris.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok no-content]]
            [schema.core :as s]
            [osiris.updates :refer [process]]
            [osiris.schema :refer [NewUpdate]]
            [clojure.tools.logging :as logging]
            [osiris.logging]))

(defn init []
  (osiris.logging/setup!)
  (logging/info "Starting Osiris handler"))

;; --- Routes --- ;;
(defapi app
        (swaggered "osiris"
                   (HEAD* "/" []
                          (ok ""))
                   (GET* "/" []
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
                                                (assoc :sqs-receive-count (Integer/parseInt x-aws-sqsd-receive-count)))
                                ]

                            (logging/info "Update received for" (:database update-info))
                            (let [result (process update-info)]
                              (logging/info "Update processed" (:database update-info))
                              (logging/info "Messages" (:database update-info) result)
                              ;(ok {:messages (flatten result)})
                              (no-content)
                              )))))
