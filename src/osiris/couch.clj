(ns osiris.couch
  (:require [com.ashafa.clutch :as cl]
            [osiris.config :as config]
            [osiris.schema :refer [UpdateType]]
            [schema.core :as s]
            [clojure.tools.logging :as logging]
            ))

(defn database
  "Constructs a database URL for the given database name. Other parameters are pulled from config."
  [db-name]
  (assoc (cemerick.url/url config/COUCH_HOST db-name)
    :username config/COUCH_USER
    :password config/COUCH_PASSWORD))


(defonce ^{:private true} db (atom (database config/COUCH_DATABASE)))

(defonce ^{:private true} token (atom false))

(defn couch-ready?
  "True if the defined db has been checked/created"
  []
  @token)

(defn ensure-db
  []
  (if (not (couch-ready?))
    (do
      (logging/info "Creating database" @db)
      (let [meta (cl/get-database @db)]
        (swap! token not)
        meta))))

(defn watched-state
  [database-name]
  (ensure-db)
  (-> (cl/get-document @db database-name)
      (cl/dissoc-meta)))

(defn set-watched-state!
  [database-name last-seq]
  (ensure-db)
  (-> (if-let [doc (cl/get-document @db database-name)]
        (cl/put-document @db (assoc doc :last-seq last-seq))
        (cl/put-document @db {:_id database-name :last-seq last-seq}))
      (cl/dissoc-meta)))

(defn changes-since
  "Returns all database changes since the given sequence (a string) for the database db"
  [db-name since]
  (let [url (database db-name)]
    (if (nil? since)
      (cl/changes url :include_docs true)
      (cl/changes url :since since :include_docs true))))

(defn webhooks
  "Gets all webhooks for the given database for updated documents with the given type"
  [database                                                 ; :- s/Str
   doc-type]                                                ; :- UpdateType
  ;; TODO
  nil)
