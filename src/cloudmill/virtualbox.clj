(ns cloudmill.virtualbox
  (:require [cloudmill.dispatch :as dsp]
            [cloudmill.bootstrap :refer [bootstrap]]

            [pallet.core :refer [group-spec node-spec converge]]
            [pallet.api :refer [plan-fn]]
            [pallet.node :refer [primary-ip ssh-port]]
            [pallet.crate.automated-admin-user :refer [automated-admin-user]]
            [pallet.actions :refer [exec-script]]
            [pallet.configure :refer [compute-service]]))

(def default-group (group-spec "debian-vm"
                               :phases {:bootstrap (plan-fn (automated-admin-user))}
                               :node-spec (node-spec :image {:os-family :debian})))

(defn start-vm
  [& args]
  (println "starting vm...")
  (-> @(converge {default-group 1} :compute (compute-service :virtualbox))
      :new-nodes
      first
      :node))

(defn stop-vm
  [& args]
  (println "stopping vm...")
  @(converge {default-group 0} :compute (compute-service :virtualbox))
  (println "done."))

(defn output-results
  [node]
  (println (format "Successfully started a VM. You can ssh into the VM at %s:%d"
                   (primary-ip node) (ssh-port node))))

(defn virtualbox
  "Start and stop one-off virtual machines.

Understood operations are:
  start
  stop"
  [command & args]
  (let [stop-vboxweb (bootstrap "logs/vboxweb")]
    (case command
      "start" (output-results (start-vm))
      "stop"  (stop-vm))))

(dsp/register-command "vbox" #'virtualbox)
