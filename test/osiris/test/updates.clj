(ns osiris.test.updates
  (:require [midje.sweet :refer :all]
            [osiris.updates :refer :all]
            [osiris.changes :refer [since-seq]]
            [osiris.checkpoint :refer [last-seq last-seq!]]))

(facts "About update processing"
       (fact "pulls _changes feed from last known sequence"
             (let [dbname "db-123"]
               (process {:database dbname}) => ...changes...
               (provided
                 (since-seq dbname ...seq...) => ...changes...
                 (last-seq dbname) => ...seq...
                 (last-seq! dbname anything) => anything)))

       (fact "sets last known sequence in DynamoDB"
             (let [dbname "db-123"]
               (process {:database dbname}) => ...changes...
               (provided
                 (since-seq dbname ...seq...) => ...changes...
                 (last-seq dbname) => ...seq...
                 (last-seq! dbname ...last...) => anything
                 (last ...changes...) => {:sequence ...last...})))

       (fact "calls webhooks for update"
             true => false))


