(ns osiris.test.checkpoint
  (:require [midje.sweet :refer :all]
            [osiris.checkpoint :refer :all]))

(facts "About checkpointing"
       (fact "gets last processed sequence for a database"
             (last-seq ...database...) => ...last...
             (provided
               (osiris.couch/watched-state ...database...) => {:osiris.couch/last-seq ...last...}))

       (fact "sets last processed sequence for a database"
             (last-seq! ...database... ...last...) => ...last...
             (provided
               (osiris.couch/set-watched-state! ...database... ...last...) => ...last...)))
