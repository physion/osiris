(ns osiris.updates)

(defn process
  "Processes a single update of the form {:database db-name}"
  [update]
  (:database update))
