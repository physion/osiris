(ns osiris.handler
  (:import (com.sun.xml.internal.bind.v2.model.core ID))
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]
            [schema.coerce :as coerce]
            [osiris.updates :refer [process]]
            [osiris.schema :refer [NewUpdate]]
            [clojure.data.json :as json]
            [ring.util.codec :refer [base64-decode]]))

;; --- Routes --- ;;
(defapi app
        (swaggered "osiris"
          (HEAD* "/" []
            (ok ""))
          (GET* "/" []
            (ok "Osiris!"))

          (POST* "/updates" []
            :body [msg-body s/Str] ;; Base64-encoded
            :summary "Processes an update from Aker"
            :header-params [x-aws-sqsd-msgid :- s/Str
                            x-aws-sqsd-queue :- s/Str
                            x-aws-sqsd-first-received-at :- s/Str
                            x-aws-sqsd-receive-count :- s/Str]

            (let [ __ (clojure.pprint/pprint msg-body)
                   parsed (json/read-str (String. (base64-decode (.getBytes msg-body "utf-8"))) :key-fn #(keyword %)) ; Parse JSON from B64 encoded message body

                   __ (clojure.pprint/pprint parsed)
                   update ((coerce/coercer NewUpdate coerce/json-coercion-matcher) parsed) ; Coerce to a NewUpdate

                   update-info (-> update
                                (assoc :sqs-msgid x-aws-sqsd-msgid)
                                (assoc :sqs-queue x-aws-sqsd-queue)
                                (assoc :sqs-first-received-at x-aws-sqsd-first-received-at)
                                (assoc :sqs-receive-count (Integer/parseInt x-aws-sqsd-receive-count)))]

              (ok (process update-info))))))
