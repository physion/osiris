(ns osiris.checkpoint
  (:require [osiris.couch :as c]))




(defn last-seq
  "Gets the last processed _changes seq for a database name"
  [database]
  (:last-seq (c/watched-database-state database)))

(defn last-seq!
  "Updates the last processed _changes seq for a database name"
  [database last-seq]
  (c/database-last-seq! database last-seq))
