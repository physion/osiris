(ns osiris.test.updates
  (:require [midje.sweet :refer :all]
            [osiris.updates :refer :all]
            [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.couch :refer [changes-since couch-ready?]]))

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

(facts "About webhook callbacks"
  (fact ))


