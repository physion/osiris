(ns osiris.test.updates
  (:require [midje.sweet :refer :all]
            [osiris.updates :refer :all]
            [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.couch :refer [changes-since couch-ready? webhooks]]
            [cemerick.bandalore :as sqs]
            [clojure.data.json :as json]
            [osiris.config :as config]))

(facts "About update processing"
  (fact "gets changes for update"
    (changes-for-update ...update...) => ...changes...
    (provided
      (database-for-update ...update...) => ...db...
      (last-seq ...db...) => ...since...
      (changes-since ...db... ...since...) => ...changes...))

  (fact "processes changes for update"
    (process ...update...) => ...results...
    (provided
      (database-for-update ...update...) => "db"
      (last-seq "db") => ...since...
      (changes-since "db" ...since...) => ...changes...
      (process-changes "db" ...changes...) => ...results...))

  (fact "skips changes to underworld database"
    (process ...update...) => nil
    (provided
      (database-for-update ...update...) => osiris.config/COUCH_DATABASE)))

(facts "About webhook callbacks"
  (fact "call-hook queues the hook and doc _id and _rev"
    (call-hook ...client... {:_id ...id... :_rev ...rev...} {:_id ...hookid...}) => ...msg...
    (provided
      (json/write-str {:doc_id ...id... :doc_rev ...rev... :hook_id ...hookid...}) => ...body...
      (sqs/send ...client... config/CALL_QUEUE ...body...) => ...msg...))

  (fact "call-hooks updates last seq"
    (call-hooks ...db... {:doc {:_id ...id... :type ...type...} :seq ...seq...}) => '(...result...)
    (provided
      (last-seq! ...db... ...seq...) => nil
      (webhooks ...db... ...type...) => '(...hook...)
      (call-hook client {:_id ...id... :type ...type...} ...hook...) => ...result...)))


