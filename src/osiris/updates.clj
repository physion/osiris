(ns osiris.updates
  (:require [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.schema :refer [UpdateInfo]]
            [schema.core :as s]
            [osiris.couch :refer [changes-since webhooks]]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.tools.logging :as logging]
            [osiris.logging :refer [setup!]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(setup!)

(defn database-for-update
  [update]
  (:database update))


(defn changes-for-update
  "Gets the seq of _changes for the given UpdateInfo"
  [update]
  (let [db (database-for-update update)
        since (last-seq db)]
    (changes-since db since)))


; Web hooks are
; {
;   "type": "webhook",
;   "trigger_type": <doc type>,
;   "db": <db>
;   "url": <url>
; }
;
(defn call-hook
  "Makes a webhook call function for the given hook document.

  Web hooks are
  {
    type: webhook,
    trigger_typ: <doc type>,
    db: <db>
    url: <url>
  }

  A POST is sent to the url with a WebCallback as body.

  Returns the result of the http POST
  "
  [doc]
  (fn [hook]
    (let [{:keys [status body error]} @(http/post (:url hook) {:body (json/write-str doc) :content-type :json :accept :json})]
      {:status status,
       :body   (json/read-str body)
       :error  error
       :token  {:hook (:_id hook)
                :doc  (:_id doc)
                :rev  (:_rev doc)}})))


(defn call-hooks
  "Returns a callback function for changes on the given database. Callback should be called
  once with each _changes entry"
  [db]
  (fn [change]
    (let [doc (:doc change)]
      (logging/info "Processing webhooks for" db ":" (:_id doc))

      (try
        (let [hooks (webhooks db (:type doc))
              results (map (call-hook doc) hooks)]

          ;; TODO check results for failures and send them to the failed queue 
          (map (fn [result] result) results))
        (finally
          (last-seq! db (:seq change))
          (logging/info "Updated last-seq for" db ":" (:seq change)))))))


(defn process-changes
  "Process a seq of changes, assuming doc is included"
  [db docs]
  (doall (map (call-hooks db) docs)))

(defn process
  "Processes a single update of the form {:database db-name}"
  [update]
  (let [db (database-for-update update)]
    (when (not (= db osiris.config/COUCH_DATABASE))
      (do
        (logging/info "Processing changes for" db)
        (process-changes db (changes-for-update update))))))
