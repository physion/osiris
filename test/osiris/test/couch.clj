(ns osiris.test.couch
  (:require [midje.sweet :refer :all]
            [osiris.couch :refer :all]
            [com.ashafa.clutch :as cl]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]))

(defn slingshot-exception
  [exception-map]
  (slingshot.support/get-throwable
    (slingshot.support/make-context exception-map (str "throw+: " map) (slingshot.support/stack-trace) {})))

(facts "About watched database state"
  (fact "gets state document for named database"
    (watched-state ...database...) => {:osiris.couch/last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-view anything "state" :last-seq {:startkey   [...database... {}]
                                               :limit      1
                                               :descending true}) => {:rows [{:value ...last...}]}))

  ;;BEGIN TODO
  (fact "`set-watched-state!` sets state document for named database with existing state document"
    (set-watched-state! ...database... ...last...) => {:osiris.couch/last-seq ...last...}
    (provided
      (couch-ready?) => true
      (time/now) => ...now...
      (tc/to-long ...now...) => ...ts...
      (state-document-id ...database...) => ...id...
      (cl/put-document anything ...id... (contains {:last-seq  ...last...
                                                    :database  ...database...
                                                    :type      database-state-type
                                                    :timestamp ...ts...})) => {:_id      "new-id"
                                                                               :_rev     "rev-2"
                                                                               :last-seq ...last...}
      ))

  )


(facts "About _changes feed"
  (fact "gets _changes since sequence number"
    (changes-since ...dbname... "seq") => ...changes...
    (provided
      (database ...dbname...) => ...db...
      (cl/changes ...db... :since "seq" :include_docs true) => ...changes...))
  (fact "gets _changes from sequence start"
    (changes-since ...dbname... nil) => ...all...
    (provided
      (database ...dbname...) => ...db...
      (cl/changes ...db... :include_docs true) => ...all...)))

(facts "About database creation"
  (fact "creates database and view when not checked"
    (ensure-db) => ...meta...
    (provided
      (couch-ready?) => false
      (cl/get-database anything) => ...meta...
      (update-view!) => ...view...)))

(facts "About webhooks"
  (fact "Retrieves webhooks by [database,type]"
    (webhooks ...db... ...type...) => ...result...
    (provided
      (couch-ready?) => true
      (cl/get-view anything osiris-design-doc :db-webhooks {:include_docs true :key [...db... ...type...]}) => ...dbhooks...
      (cl/get-view anything osiris-design-doc :universal-webhooks {:include_docs true :key ...type...}) => ...universalhooks...
      (concat ...universalhooks... ...dbhooks...) => ...result...))

  (fact "Retrieves universal webhooks by type"
    (webhooks nil ...type...) => ...result...
    (provided
      (couch-ready?) => true
      (cl/get-view anything osiris-design-doc :universal-webhooks {:include_docs true :key ...type...}) => ...result...))
  )
