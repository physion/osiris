(ns osiris.test.checkpoint
  (:require [midje.sweet :refer :all]
            [osiris.checkpoint :refer :all]))

(facts "About checkpointing"
       (fact "gets last processed sequence for a database"
             (last-seq ...database...) => ...last...
             (provided
               (osiris.couch/watched-database-state ...database...) => {:last-seq ...last...}))

       (fact "sets last processed sequence for a database"
             (last-seq! ...database... ...last...) => ...last...
             (provided
               (osiris.couch/database-last-seq! ...database... ...last...) => ...last...)))
