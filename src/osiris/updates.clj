(ns osiris.updates
  (:require [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.schema :refer [UpdateInfo]]
            [schema.core :as s]
            [osiris.couch :refer [changes-since]]
            [clojure.walk :refer [keywordize-keys]]
            ))


(defn call-hooks
  [db]
  (fn [change]
    (let [doc (keywordize-keys (:doc change))
          doc-type (:type doc)
          hooks ()]

      ;; get webhooks for database, type

      ;; Types: relation, keywords, notes, timeline_events, properties, {OTHER}
      ;;TODO

      ;; update last-seq for database
      (last-seq! db (:seq change))

      nil)))

(defn- process-changes-seq
  "Process a seq of changes, assuming doc is included"
  [db docs]
  (doall (map (call-hooks db) docs)))

(defn database-for-update
  [update]
  (:database update))

(s/defn changes-for-update
  "Gets the seq of _changes for the given UpdateInfo"
  [update :- UpdateInfo]
  (let [db (database-for-update update)
        since (last-seq db)]
    (changes-since since)))

(s/defn process
  "Processes a single update of the form {:database db-name}"
  [update :- UpdateInfo]
  (let [db (database-for-update update)
        changes (changes-for-update update)]

    (process-changes-seq db changes)))






