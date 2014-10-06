(ns osiris.updates
  (:require [osiris.changes :refer [since-seq]]
            [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.schema :refer [UpdateInfo]]
            [schema.core :as s]
            [osiris.couch :refer [changes-since]]
            ))


(defn call-hooks
  [db]
  (fn [change]
    (let []
      ;; update last-seq for database
      (last-seq! db (:sequence change))

      ())))

(defn- process-changes-seq
  [db changes]
  (map (call-hooks db) changes))

(defn database-for-update
  [update]
  (:database update))

(s/defn process
  "Processes a single update of the form {:database db-name}"
  [update :- UpdateInfo]
  (let [db (database-for-update update)
        changes (changes-for-update update)]

    (process-changes-seq db changes)))


(s/defn changes-for-update
  "Gets the seq of _changes for the given UpdateInfo"
  [update :- UpdateInfo]
  (let [db (database-for-update update)
        since (last-seq db)]
    (changes-since since)))



