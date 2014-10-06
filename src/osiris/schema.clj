(ns osiris.schema
  (:require [schema.core :as s]))

;; --- Schema --- ;;

(s/defschema NewUpdate {:database s/Str})
(s/defschema UpdateInfo (-> NewUpdate
                          (assoc :sqs-msgid s/Str)
                          (assoc :sqs-queue s/Str)
                          (assoc :sqs-first-received-at s/Str)
                          (assoc :sqs-receive-count s/Str)))

(s/defschema Webhook {:user   s/Uuid                        ;; User Id
                      :entity s/Str                         ;; Entity URI
                      :type   (s/enum :update :mention :insertion) ;; message type
                      })