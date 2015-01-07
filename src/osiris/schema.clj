(ns osiris.schema
  (:require [schema.core :as s]))

;; --- Schema --- ;;

(s/defschema NewUpdate {:database s/Str})
(s/defschema UpdateInfo (-> NewUpdate
                            (assoc :sqs-msgid s/Str)
                            (assoc :sqs-queue s/Str)
                            (assoc :sqs-first-received-at s/Str)
                            (assoc :sqs-receive-count s/Str)))


;;NB Shared with Iris. We should factor this into a common library
(s/defschema Webhook {:_id                     s/Str
                      :_rev                    s/Str
                      :type                    "webhook"
                      :user                    s/Uuid       ;; User Id
                      :trigger_type            s/Str        ;; Entity type
                      (s/optional-key  :db)     s/Str
                      :url                     s/Str
                      :api_key                 s/Str        ;; URL API Key
                      (s/optional-key :filter) [[s/Keyword s/Str]] ;; filter field key and regex
                      })
