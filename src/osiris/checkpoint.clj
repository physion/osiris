(ns osiris.checkpoint
  (:require [rotary.client :as ddb]
    ))

(defonce default-client-creds (atom {:access-key (System/getenv "AWS_ACCESS_KEY")
                                     :secret-key (System/getenv "AWS_SECRET_KEY")}))

(defonce ddb-checkpoint-table-name (atom (System/getenv "UNDERWORLD_TABLE")))

(defonce ddb-checkpoint-table (ddb/ensure-table credentials {:name ddb-checkpoint-table-name
                                                             :hash-key {:name "worker" :type :s}
                                                             :throughput {:read 5 :write 5}}))

(defn set-default-client-creds!
  "Sets the default AWS client credentials"
  [creds]
  (reset! default-client-creds creds))

(defn- checkpoint
  [table database]
  nil)

(defn- checkpoint!
  [table database last-seq]
  nil)

(defn- checkpoint-table
  [credentials]
  {:table       ddb-checkpoint-table-name
   :credentials credentials})

(defn last-seq
  "Gets the last processed _changes seq for a database"
  [database & {:keys [credentials] :or {:credentials @default-client-creds}}]
  (let [table (checkpoint-table credentials)]
    (checkpoint table database)))

(defn last-seq!
  "Updates the last processed _changes seq for a database"
  [database last-seq & {:keys [credentials] :or {:credentials @default-client-creds}}]
  (let [table (checkpoint-table credentials)]
    (checkpoint! table database last-seq)))
