(ns osiris.updates
  (:require [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.schema :refer [UpdateInfo]]
            [schema.core :as s]
            [osiris.couch :refer [changes-since webhooks]]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.logging :as logging]
            [osiris.logging :refer [setup!]]
            [cemerick.bandalore :as sqs]
            [clojure.data.json :as json]
            [osiris.config :as config]))



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
  "Makes a webhook call function for the given hook document.

  Web hooks are
  {
    type: webhook,
    trigger_typ: <doc type>,
    db: <db>
    url: <url>
  }

  A POST is sent to the url with a WebCallback as body.

  Returns the result of the http POST
  "
  [client doc hook]
  (logging/info "Calling hook" doc hook)
  (let [msg {:doc_id (:_id doc) :doc_rev (:_rev doc) :hook_id (:_id hook)}]
    (logging/info "Sending message" msg "to" config/CALL_QUEUE)
    (:id (sqs/send client config/CALL_QUEUE (json/write-str msg)))))

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
    (logging/info "Processing webhooks for" db ":" (:_id doc))
    (logging/info (webhooks db (:type doc)))
    (logging/info "...done getting webhooks")
    (let [hooks (webhooks db (:type doc))
          messages (map (partial call-hook client doc) hooks)]
      (logging/info "Messages:" messages)
      (last-seq! db (:seq change))
      (logging/info "Updated last-seq for" db ":" (:seq change))
      messages)))


(defn process-changes
  "Process a seq of changes, assuming doc is included"
  [db docs]
  (doall (map (partial call-hooks db) docs)))

(defn process
  "Processes a single update of the form {:database db-name}"
  [update]
  (let [db (database-for-update update)]
    (when (not (= db osiris.config/COUCH_DATABASE))
      (logging/info "Processing changes for" db)
      (let [queue (ensure-queue)
            result (process-changes db (changes-for-update update))]
        (logging/info "process:" result)
        result))))
