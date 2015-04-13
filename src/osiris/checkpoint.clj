(ns osiris.checkpoint
  (:require [osiris.couch :as c]))




(defn last-seq
  "Gets the last processed _changes seq for a database name"
  [database]
  (:osiris.couch/last-seq (c/watched-state database)))

(defn last-seq!
  "Updates the last processed _changes seq for a database name"
  [database last-seq]
  (c/set-watched-state! database last-seq))
