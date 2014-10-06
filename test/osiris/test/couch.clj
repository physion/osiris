(ns osiris.test.couch
  (:require [midje.sweet :refer :all]
            [osiris.couch :refer :all]
            [com.ashafa.clutch :as cl]))

(facts "About watched database state"
  (fact "gets state document for named database"
    (watched-database-state ...database...) => {:last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-document anything ...database...) => {:_id "id",
                                                    :_rev "rev",
                                                    :last-seq ...last...}))
  (fact "sets state document for named database with existing state document"
    (database-last-seq! ...database... ...last...) => {:last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-document anything ...database...) => {:_id "id",
                                                    :_rev "rev",
                                                    :last-seq ...other...}
      (cl/put-document anything (contains {:last-seq ...last...})) => {:_id "new-id"
                                               :_rev "rev-2"
                                               :last-seq ...last...}
      ))

  (fact "sets state document for named database without existing state document"
    (database-last-seq! ...database... ...last...) => {:last-seq ...last...}
    (provided
      (couch-ready?) => true
      (cl/get-document anything ...database...) => nil
      (cl/put-document anything (contains {:last-seq ...last...})) => {:_id "new-id"
                                                                       :_rev "rev-2"
                                                                       :last-seq ...last...}
      )))

(facts "About _changes feed"
  (fact "gets _changes since sequence number"
    (changes-since ...seq...) => ...changes...
    (provided
      (couch-ready?) => true
      (cl/changes :since ...seq... :include_docs true) => ...changes...)))

(facts "About database creation"
  (fact "creates database when not checked"
    (ensure-db) => ...meta...
    (provided
      (couch-ready?) => false
      (cl/get-database anything) => ...meta...)))
