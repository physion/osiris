(ns osiris.updates
  (:require [osiris.changes :refer [since-seq]]
            [osiris.checkpoint :refer [last-seq last-seq!]]))

(defn call-hooks
  [changes]
  true)

(defn process
  "Processes a single update of the form {:database db-name}"
  [update]
  (let [db (:database update)
        since (last-seq db)
        changes (since-seq db since)]

    ;; call webhooks
    (call-hooks changes)

    ;; update last-seq for database
    (last-seq! db (:sequence (last changes)))

    ;; return changes
    changes))



