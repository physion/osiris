(ns osiris.test.checkpoint
  (:require [midje.sweet :refer :all]
            [osiris.checkpoint :refer :all]))

(facts "About checkpointing"
       (fact "gets last processed sequence for a database with provided creds"
             (last-seq ...database... :credentials ...creds...) => ...last...
             (provided
               (#'osiris.checkpoint/checkpoint-table ...creds...) => ...table...
               (#'osiris.checkpoint/checkpoint ...table... ...database...) => ...last...))

       (fact "sets last processed sequence for a database with provided creds"
             (last-seq! ...database... ...last... :credentials ...creds...) => ...last...
             (provided
               (#'osiris.checkpoint/checkpoint-table ...creds...) => ...table...
               (#'osiris.checkpoint/checkpoint! ...table... ...database... ...last...) => ...last...)))
