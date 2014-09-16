(ns osiris.test.handler
  (:require [midje.sweet :refer :all]
            [osiris.handler :as handler]
            [ring.mock.request :as mock]))

(facts "about osiris"
       (fact "HEAD => 200"
             (let [response (handler/app (request :head "/"))]
               (:status response) => 200)))
