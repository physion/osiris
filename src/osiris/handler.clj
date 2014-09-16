(ns osiris.handler
  (:require [clojure.string :refer [join]]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]))

(defapi app
        {:formats [:application/json]}

        (swaggered "osiris"
                   ))
