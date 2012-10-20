(ns cloudmill.test.cluster-decl
  (:require cloudmill.readers)
  (:use cloudmill.cluster-decl
        midje.sweet))

(def minimum-decl {:roles #{:db}
                   :hardware #{:small}
                   :nodes {:db :small}
                   :hardware-spec {:small {:image {:os-family :debian}
                                           :network {:inboud-ports #{5984}}
                                           :hardware {:min-cores 1 :min-ram 600}}}
                   :service-spec {:name :couchdb
                                  :provides :db
                                  :installer #cloudmill/pallet-crate cloudmill.crate.couchdb
                                  :configurer :installer}}) ; configuration was provided by installer

(fact "Pallet node-spec's are the values of the hardware-spec attribute."
  (make-node-spec minimum-decl :small) => (get-in minimum-decl [:hardware-spec :small])
  (make-node-specs minimum-decl)       => (vals (:hardware-spec minimum-decl)))

;; To create a server-spec, we need to create a phase-fn for the
;; installer and the configurer
;;

(fact "")