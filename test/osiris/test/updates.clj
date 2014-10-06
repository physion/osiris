(ns osiris.test.updates
  (:require [midje.sweet :refer :all]
            [osiris.updates :refer :all]
            [osiris.changes :refer [since-seq]]
            [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.couch :refer [changes-since database-checked]]))

(facts "About update processing"
  (fact "gets changes for update"
    (changes-for-update ...update...) => ...changes...
    (provided
      (database-for-update ...update...) => ...db...
      (last-seq ...db...) => ...since...
      (changes-since ...since...) => ...changes...))

  (fact "processes changes for update"
    (process ...update...) => ...results...
    (provided
      (database-for-update ...update...) => ...db...
      (last-seq ...db...) => ...since...
      (changes-since ...since...) => ...changes...
      (#'osiris.updates/process-changes-seq ...db... ...changes...) => ...results...)))

;(facts "About call-hooks"
;  (fact "sets last known sequence in DynamoDB"
;    (let [dbname "db-123"]
;      ((call-hooks {:database dbname}) ...change...) => ...result...
;      (provided
;        ...change... =contains=> {:sequence ...last...}
;        (last-seq! dbname ...last...) => anything)))
;
;  (fact "calls webhooks for update"
;    true => false))


