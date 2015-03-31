(ns osiris.test.couch
  (:require [midje.sweet :refer :all]
            [osiris.couch :refer :all]
            [com.ashafa.clutch :as cl]))

(defn slingshot-exception
  [exception-map]
  (slingshot.support/get-throwable
    (slingshot.support/make-context exception-map (str "throw+: " map) (slingshot.support/stack-trace) {})))

(facts "About watched database state"
  (fact "gets state document for named database"
    (watched-state ...database...) => {:last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-document anything ...database...) => {:_id      "id",
                                                    :_rev     "rev",
                                                    :last-seq ...last...}))
  (fact "sets state document for named database with existing state document"
    (set-watched-state! ...database... ...last...) => {:last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-document anything ...database... :conflicts true) => {:_id      "id",
                                                                      :_rev     "rev",
                                                                      :last-seq ...other...}
      (cl/put-document anything (contains {:last-seq ...last...})) => {:_id      "new-id"
                                                                       :_rev     "rev-2"
                                                                       :last-seq ...last...}
      ))

  (fact "`set-watched-state!` retries state update on document update conflict (409)"
    (set-watched-state! ...database... ...last...) =future=> {:last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-document anything ...database... :conflicts true) => {:_id      "id",
                                                    :_rev     "rev",
                                                    :last-seq ...other...} :times 2

      (cl/put-document anything (contains {:last-seq ...last...})) =throws=> (slingshot-exception {:state 409}) :times 1
      (cl/put-document anything (contains {:last-seq ...last...})) => {:_id      "new-id"
                                                                       :_rev     "rev-2"
                                                                       :last-seq ...last...}
      ))

  (fact "sets state document for named database without existing state document"
    (set-watched-state! ...database... ...last...) => {:last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-document anything ...database... :conflicts true) => nil
      (cl/put-document anything (contains {:last-seq ...last...})) => {:_id      "new-id"
                                                                       :_rev     "rev-2"
                                                                       :last-seq ...last...}
      ))

  (fact "update! resolves document conflicts"
    (let [id "doc-id"]
      (let [doc {:_id        id
                 :_rev       ...r3...
                 :_conflicts [...c1... ...c2...]}
            expected-update (dissoc doc :_conflicts)]
        (update! ...database... doc {} :resolve_conflicts true) => ...result...
        (provided
          (cl/bulk-update ...database... (just #{{:_id id :_rev ...c1... :_deleted true}
                                                 {:_id id :_rev ...c2... :_deleted true}})) => anything
          (cl/put-document ...database... expected-update) => ...result...)))))


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
