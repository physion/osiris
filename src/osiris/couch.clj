(ns osiris.couch
  (:require [com.ashafa.clutch :as cl]
            [osiris.config :as config]
            [schema.core :as s]))

(defonce ^{:private true} db (atom (assoc (cemerick.url/url config/COUCH_HOST config/COUCH_DATABASE)
                                     :username config/COUCH_USER
                                     :password config/COUCH_PASSWORD)))

(defonce ^{:private true} token (atom false))

(defn database-checked
  "True if the defined db has been checked/created"
  []
  @token)

(defn ensure-db
  []
  (if (not (database-checked))
    (do
      (let [meta (cl/get-database @db)]
        (swap! token not)
        meta))))

(defn watched-database-state
  [database-name]
  (ensure-db)
  (-> (cl/get-document @db database-name)
    (cl/dissoc-meta)))

(defn database-last-seq!
  [database-name last-seq]
  (ensure-db)
  (-> (if-let [doc (cl/get-document @db database-name)]
        (cl/put-document @db (assoc doc :last-seq last-seq))
        (cl/put-document @db {:last-seq last-seq}))
    (cl/dissoc-meta)))

(s/defn changes-since
  "Returns all database changes since the given sequence (a string)"
  [since :- s/Str]
  (ensure-db)
  (cl/changes :since since))