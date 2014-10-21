(ns osiris.log)

(defn start-logging []
  (org.apache.log4j.BasicConfigurator/configure))
