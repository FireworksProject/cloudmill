(ns cloudmill.main
  (:require [cloudmill.dispatch :as dsp]
            cloudmill.virtualbox
            cloudmill.aws
            [cloudmill.configuration :refer [setup-logging] :as cfg]
            [clojure.java.io :as io])
  (:gen-class))

(defn -main
  [command & args]
  (setup-logging (:logdir @gcfg/config cfg/default-logdir) (io/resource "logback.xml"))
  (dsp/dispatch command args)
  (shutdown-agents)
  (System/exit 0))

(comment
  (def vmfest (compute-service :virtualbox))
  (use '[pallet.core :only [group-spec node-spec server-spec]]
       '[pallet.phase :only [phase-fn]]
       '[pallet.crate.automated-admin-user :only [automated-admin-user]]
       '[pallet.action.exec-script :only [exec-script]]
       '[pallet.session :only [nodes-in-group]])
  (def base-server (server-spec :phases {:bootstrap (phase-fn (automated-admin-user))}))
  (def debian-node (node-spec :image {:os-family :debian
                                      :os-64-bit? true}))
  (def debian-group (group-spec "debian-vms" :node-spec debian-node
                                :extends [base-server]))
  (pallet.core/converge {debian-group 1} :compute vmfest)
  (pallet.core/converge {debian-group 0} :compute vmfest))