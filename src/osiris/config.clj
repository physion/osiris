(ns osiris.config)

(def COUCH_HOST (if-let [host (System/getenv "COUCH_HOST")]
                  host
                  "http://localhost:5995"))

(def COUCH_DATABASE (if-let [database (System/getenv "COUCH_DATABASE")]
                      database
                      "underworld_dev"))

(def COUCH_USER (if-let [user (System/getenv "COUCH_USER")]
                  user
                  "ovation-io-dev"))

(def COUCH_PASSWORD (if-let [password (System/getenv "COUCH_PASSWORD")]
                      password
                      "boom!"))
