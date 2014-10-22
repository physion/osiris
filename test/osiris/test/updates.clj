(ns osiris.test.updates
  (:require [midje.sweet :refer :all]
            [osiris.updates :refer :all]
            [osiris.checkpoint :refer [last-seq last-seq!]]
            [osiris.couch :refer [changes-since couch-ready? webhooks]]
            [org.httpkit.client :as http]
            [org.httpkit.fake :refer [with-fake-http]]
            [clojure.data.json :as json]))

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
  (fact "call-hook POSTs doc to url and returns result"
    (let [doc {:_id "id123" :type "mytype" :_rev "rev-123"}]
      (with-fake-http [{:url ...url... :method :post :body (json/write-str doc)} {:status ...status... :error ...error... :body ...resultbody...}]
        ((call-hook doc) {:_id ...hookid... :type "webhook" :trigger_type "mytype" :db ...db... :url ...url...})) => {:status ...status...
                                                                                                                      :body   ...result...
                                                                                                                      :error  ...error...
                                                                                                                      :token  {:hook ...hookid...
                                                                                                                               :doc  (:_id doc)
                                                                                                                               :rev  (:_rev doc)}}
      (provided
        (json/read-str ...resultbody...) => ...result...)))

  (fact "call-hooks updates last seq"
    ((call-hooks ...db...) {:doc {:_id ...id... :type ...type...} :seq ...seq...}) => '(...result...)
    (provided
      (last-seq! ...db... ...seq...) => nil
      (webhooks ...db... ...type...) => '(...hook...)
      (call-hook anything) => (fn [hook] ...result...))))


