(ns osiris.test.updates
  (:require [midje.sweet :refer :all]
            [osiris.updates :refer :all]))

(facts "about update processing"
       (fact "knows database name"
             (let [dbname "my database"]
               (process {:database dbname}) => dbname)))


