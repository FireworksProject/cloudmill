(ns cloudmill.aws
  (:require [cloudmill.dispatch :as dsp]
            [cloudmill.configuration :as cfg]

            [pallet.core :refer [node-spec server-spec converge group-spec]]
            [pallet.configure :refer [compute-service]]
            [pallet.api :refer [plan-fn]]
            [pallet.node :refer [primary-ip ssh-port]]
            [pallet.crate
             [automated-admin-user :refer [automated-admin-user]]]))

(def default-node-spec
  (node-spec :image {:os-family :ubuntu}
             :hardware {:hardware-id "t1.micro"}
             :network {:inbound-ports [22]}))

(def base-server
  (server-spec :phases {:bootstrap (plan-fn (automated-admin-user))}))

(defn error
  [message & args]
  (.println *err* (apply format message args)))

(defn credentials
  "Store AWS credientials in configuration."
  [identity credentials]
  (cfg/swap-config! cfg/config assoc ::credentials {:identity identity :secret credentials}))

(defmacro with-credentials
  [bindings & body]
  `(let [~bindings (::credentials @cfg/config)]
     (if (some empty? (list ~@(keys bindings)))
       (error "Could not load your AWS credentials.")
       (do ~@body))))

(defn start
  [name]
  (with-credentials {id :identity secret :secret}
    (->> @(converge {(group-spec name :extends [base-server] :node-spec default-node-spec) 1}
                   :compute (compute-service "aws-ec2" :identity id :credential secret))
        :new-nodes
        (map :node)
        (map #(println (format "Successfully started a VM. You can ssh into the VM at %s:%d"
                               (primary-ip %) (ssh-port %))))
        dorun)))

(defn stop
  [name]
  (with-credentials {id :identity secret :secret}
    (->> @(converge {(group-spec name) 0}
                    :compute (compute-service "aws-ec2" :identity id :credential secret))
         :old-nodes
         (map :node)
         (map #(println (format "Successfully stopped a VM: %s" (primary-ip %))))
         dorun)))

(defn aws
  "Interact with Amazon AWS.

Subcommands:
  credentials <identity> <credential>
  start <cloud-name>
  stop <cloud-name>"
  [command & args]
  (case command
    "credentials" (apply credentials args)))

(dsp/register-command "aws" #'aws)