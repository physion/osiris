(ns osiris.config)

(def COUCH_HOST (if-let [host (System/getProperty "COUCH_HOST")]
                  host
                  "http://localhost:5995"))

(def COUCH_DATABASE (if-let [database (System/getProperty "COUCH_DATABASE")]
                      database
                      "underworld_dev"))

(def COUCH_USER (if-let [user (System/getProperty "COUCH_USER")]
                  user
                  "ovation-io-dev"))

(def COUCH_PASSWORD (if-let [password (System/getProperty "COUCH_PASSWORD")]
                      password
                      "boom!"))
