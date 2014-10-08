(ns osiris.test.handler
  (:require [midje.sweet :refer :all]
            [osiris.handler :as handler]
            [ring.mock.request :refer [request header content-type body]]
            [clojure.data.json :as json]
            [ring.util.codec :refer [base64-encode]]))

(facts "About Osiris"
       (fact "HEAD => 200"
             (let [response (handler/app (request :head "/"))]
               (:status response) => 200))
       (fact "POST /updates => 200"
             (let [ msg (String. (base64-encode (.getBytes "{\"database\" : \"my-database\"}" "utf-8")))

                    post (-> (request :post "/updates")
                               (header "X-Aws-Sqsd-Msgid" "123")
                               (header "X-Aws-Sqsd-Queue" "queue")
                               (header "X-Aws-Sqsd-First-Received-At" "12-12-12")
                               (header "X-Aws-Sqsd-Receive-Count" "1")
                               (content-type "application/json")
                               (body msg))
                    ]
               (:status (handler/app post)) => 200
               (provided
                 (osiris.updates/process anything) => ())))
  )
