(ns osiris.updates
  (:require [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.schema :refer [UpdateInfo]]
            [schema.core :as s]
            [osiris.couch :refer [changes-since]]
            [clojure.walk :refer [keywordize-keys]]
            [com.climate.newrelic.trace :refer [defn-traced]]
            [clojure.tools.logging :as logging]
            [osiris.logging :refer [setup!]]
            ))

(setup!)

(defn-traced call-hooks
  "Returns a callback function for changes on the given database. Callback should be called
  once with each _changes entry"
  [db]
  (fn [change]
    (let [doc (:doc change)]
      (logging/info "Processing webhooks for" db ":" (:_id doc))

      ;; get webhooks for database, type

      ;; Types: relation, keywords, notes, timeline_events, properties, {OTHER}
      ;; TODO

      ;; update last-seq for database
      (last-seq! db (:seq change))
      (logging/info "Updated last-seq for" db ":" (:seq change))

      (:seq change))))

(defn- process-changes-seq
  "Process a seq of changes, assuming doc is included"
  [db docs]
  (doall (map (call-hooks db) docs)))

(defn database-for-update
  [update]
  (:database update))

(defn changes-for-update
  "Gets the seq of _changes for the given UpdateInfo"
  [update]
  (let [db (database-for-update update)
        since (last-seq db)]
    (changes-since db since)))

(defn-traced process
  "Processes a single update of the form {:database db-name}"
  [update]
  (let [db (database-for-update update)]
    (when (not (= db osiris.config/COUCH_DATABASE))
      (do
        (logging/info "Processing changes for" db)
        (process-changes-seq db (changes-for-update update))))))






