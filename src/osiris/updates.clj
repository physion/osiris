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

(setup!)

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
  (let [msg {:doc_id (:_id doc) :doc_rev (:_rev doc) :hook_id (:_id hook)}]
    (logging/info "Sending message" msg "to" config/CALL_QUEUE)
    (:id (sqs/send client config/CALL_QUEUE (json/write-str msg)))))

(defn ensure-queue
  "Ensures the config/CALL_QUEUE queue is created"
  []
  (if-not (some #{config/CALL_QUEUE} (sqs/list-queues client))
    (sqs/create-queue client config/CALL_QUEUE)))

(defn call-hooks
  "Returns a callback function for changes on the given database. Callback should be called
  once with each _changes entry"
  [db change]
  (let [doc (:doc change)]
    (logging/info "Processing webhooks for" db ":" (:_id doc))

    (try
      (let [hooks (webhooks db (:type doc))]
        (if (empty? hooks)
          '()
          (map (partial call-hook client doc) hooks)))
      (finally
        (last-seq! db (:seq change))
        (logging/info "Updated last-seq for" db ":" (:seq change))))))


(defn process-changes
  "Process a seq of changes, assuming doc is included"
  [db docs]
  (doall (map (partial call-hooks db) docs)))

(defn process
  "Processes a single update of the form {:database db-name}"
  [update]
  (let [db (database-for-update update)]
    (when (not (= db osiris.config/COUCH_DATABASE))
      (do
        (ensure-queue)
        (logging/info "Processing changes for" db)
        (process-changes db (changes-for-update update))))))
