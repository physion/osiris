(ns osiris.couch
  (:require [com.ashafa.clutch :as cl]
            [osiris.config :as config]
            [clojure.tools.logging :as logging]
            [osiris.logging]
            [slingshot.slingshot :refer [try+]]
            [again.core :as again]))


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

(def osiris-design-doc "osiris")

(def view-fns (cl/view-server-fns :javascript
                {:db-webhooks        {:map
                                      "function(doc) {
                                        if(doc.type && doc.type==='webhook' && doc.db) {
                                          emit([doc.db, doc.trigger_type], null);
                                        }
                                      }"
                                      }
                 :universal-webhooks {:map "function(doc) {
                                                     if(doc.type && doc.type==='webhook' && !doc.db) {
                                                       emit(doc.trigger_type, null);
                                                     }
                                                   }"}}))

(defn update-view! []
  (try+
    (if (nil? (cl/get-document @db (str "_design/" osiris-design-doc)))
      (cl/save-view @db osiris-design-doc view-fns))
    (catch [:status 409] _
      (logging/info "View update failed (409)"))))

(defn ensure-db
  []
  (when (not (couch-ready?))
    (do
      (logging/debug "Checking database" (dissoc @db :username :password))
      (let [meta (cl/get-database @db)
            view (update-view!)]                            ;
        (swap! token not)
        (and view meta)))))

(defn ensure-webhooks
  []
  (ensure-db))

(defn watched-state
  [database-name]
  (ensure-db)
  (-> (cl/get-document @db database-name)
    (cl/dissoc-meta)))


(def database-state-type "database-state")

(defn update!
  [dburl doc update-map & {:keys [resolve_conflicts] :or {resolve_conflicts true}}]

  (if-let [conflicts (:_conflicts doc)]
    (when resolve_conflicts
      (let [bulk (map (fn [conflict] {:_id      (:_id doc)
                                      :_rev     conflict
                                      :_deleted true}) conflicts)]
        (cl/bulk-update dburl bulk))))

  (cl/put-document dburl (merge (dissoc doc :_conflicts) update-map)))

(def exponential-backoff-strategy (again/max-delay 10000
                                      (again/randomize-strategy 0.5
                                        (again/multiplicative-strategy 100 2))))

(defn update-document!
  "Creates or updates the document with doc-id"

  [db doc-id update-map]


  (again/with-retries exponential-backoff-strategy
    ;; PUT/POST document
    (if-let [doc (cl/get-document @db doc-id :conflicts true)]
      (update! @db doc update-map)
      (cl/put-document @db (assoc update-map :_id doc-id)))))


(defn set-watched-state!
  [database-name last-seq]
  (ensure-db)
  (-> (update-document! db database-name {:last-seq last-seq :type database-state-type})
    (cl/dissoc-meta)))

(defn changes-since
  "Returns all database changes since the given sequence (a string) for the database db"
  [db-name since]
  (let [url (database db-name)]
    (try+
      (if (nil? since)
        (cl/changes url :include_docs true)
        (cl/changes url :since since :include_docs true))
      (catch IllegalStateException _
        (logging/info (str "Ignoring missing _changes feed" url))
        '()))))

(defn webhooks
  "Gets all webhooks for the given database for updated documents with the given type.
  If database is nil, gets only universal webhooks"
  [database                                                 ; :- s/Str
   type]                                                    ; :- document "type" value
  (ensure-webhooks)

  (if (nil? database)
    (cl/get-view @db osiris-design-doc :universal-webhooks {:include_docs true :key type})
    (concat
      (cl/get-view @db osiris-design-doc :universal-webhooks {:include_docs true :key type})
      (cl/get-view @db osiris-design-doc :db-webhooks {:include_docs true :key [database type]}))))
