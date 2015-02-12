(ns osiris.updates
  (:require [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.schema :refer [UpdateInfo]]
            [schema.core :as s]
            [osiris.couch :refer [changes-since webhooks]]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.logging :as logging]
            [cemerick.bandalore :as sqs]
            [clojure.data.json :as json]
            [osiris.config :as config]
            [osiris.logging])
  (:import (com.fasterxml.jackson.core JsonGenerationException)))

(osiris.logging/setup!)

(defn database-for-update
  [update]
  (:database update))

(def client (sqs/create-client))

(defn changes-for-update
  "Gets the seq of _changes for the given UpdateInfo"
  [update]
  (let [db (database-for-update update)
        since (last-seq db)]
    (changes-since db since)))


(defn call-hook
  "Queues a webhook call function for the given document and hook.
  "
  [client db doc hook]
  (logging/debug "Queueing hook call" doc hook)
  (let [msg-base {:doc_id (:_id doc) :doc_rev (:_rev doc) :hook_id (:id hook) :db db}
        msg (if (or (:deleted doc) (:_deleted doc)) (assoc msg-base :deleted true) msg-base)]
    (logging/info "Sending message" msg "to" config/CALL_QUEUE)
    (try
      (:id (sqs/send client config/CALL_QUEUE (json/write-str msg)))
      (catch JsonGenerationException ex {:error (.getMessage ex)}))))

(defn ensure-queue
  "Ensures the config/CALL_QUEUE queue is created"
  []
  (if-let [queue config/CALL_QUEUE]
    (if-not (some #{queue} (sqs/list-queues client))
      (sqs/create-queue client queue))
    (logging/warn "No config/CALL_QUEUE defined")))

(defn call-hooks
  "Returns a callback function for changes on the given database. Callback should be called
  once with each _changes entry"
  [db change]
  (let [doc (:doc change)]
    (logging/debug "Processing webhooks for" (:_id doc) "of type" (:type doc))
    (let [hooks (webhooks db (:type doc))]
      (logging/debug "Found" (count hooks) "webhooks for" (:_id doc))
      (try
        (let [messages (map (partial call-hook client db doc) hooks)]

          (logging/info "Processed" (count messages) "messages for db" db)
          (last-seq! db (:seq change))
          (logging/info "Updated last-seq for" db ":" (:seq change))
          messages)
        (catch JsonGenerationException ex {:error (.getMessage ex)})))))


(defn process-changes
  "Process a seq of changes, assuming doc is included"
  [db docs]
  (logging/debug "process-changes start")
  (let [result (doall (map (partial call-hooks db) docs))]
    (logging/debug "process-changes complete")
    result))

(defn process
  "Processes a single update of the form {:database db-name}"
  [update]
  (let [db (database-for-update update)]
    (when (not (= db osiris.config/COUCH_DATABASE))
      (logging/info "Processing changes for" db)
      (ensure-queue)
      (process-changes db (changes-for-update update)))))
