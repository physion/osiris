(ns osiris.logging
  (:require [clj-logging-config.log4j :as log-config]
            [osiris.config :refer [LOGGING_HOST]]))

(defn setup! []
  (if-let [host LOGGING_HOST]
    (log-config/set-logger!
      :level :debug
      :out (doto (org.apache.log4j.net.SyslogAppender.)
             (.setSyslogHost host)
             (.setFacility "LOCAL7")
             (.setFacilityPrinting false)
             (.setName "osiris"))) ;             (org.apache.log4j.PatternLayout. "%p: (%F:%L) %x %m %n")

    (log-config/set-logger!
      :level :debug
      :out (org.apache.log4j.ConsoleAppender. (org.apache.log4j.SimpleLayout.)))))
