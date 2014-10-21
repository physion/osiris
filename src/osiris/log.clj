(ns osiris.log)


(def logger (org.apache.log4j.Logger/getLogger "A1"))
(def log-levels (vec ( org.apache.log4j.Level/getAllPossiblePriorities)))

(defn start-logging []
  (org.apache.log4j.BasicConfigurator/configure))
