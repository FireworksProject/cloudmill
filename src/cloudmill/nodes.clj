(ns cloudmill.nodes
  (:require [pallet.compute :as compute]
            [pallet.node :as n])
  (:use [pallet.configure :only [compute-service]])
  (:import java.util.UUID))

(def nodes (atom {}))

(defn canonicalize-node
  [node]
  (let [key-map {:group-name      n/group-name
                 :hostname        n/hostname
                 :64-bit?         n/is-64bit?
                 :os-family       n/os-family
                 :os-version      n/os-version
                 :primary-ip      n/primary-ip
                 :private-ip      n/private-ip
                 :running?        n/running?
                 :ssh-port        n/ssh-port
                 :id              n/id}]
    (zipmap (keys key-map) ((apply juxt (vals key-map)) node))))

(defn update-nodes
  [compute-name]
  (swap! nodes assoc compute-name (reduce #(assoc %1 (UUID/fromString (n/id %2)) %2)
                                          {}
                                          (compute/nodes (compute-service compute-name)))))

(defn is-pallet-node?
  "Check if the given node has a \"group-name\" tag. If it does then we know that
pallet knows how to handle it. "
  [node]
  (not (empty? (n/group-name node))))

(defn get-nodes
  [compute-name]
  {:compute-provider compute-name
   :nodes (into #{} (mapv canonicalize-node (filter is-pallet-node?
                                                    (-> (update-nodes compute-name)
                                                        (get compute-name)
                                                        vals))))})

(defn- lookup-nodes
  [compute-name node-ids]
  (map #(get-in @nodes [compute-name %]) node-ids))

(defn start-node
  [compute-name & node-ids]
  (->> (lookup-nodes compute-name node-ids)
       (compute/boot-if-down (compute-service compute-name))))

(defn- shutdown-node
  [compute-name node]
  (compute/shutdown-node (compute-service compute-name) node (compute/admin-group node)))

;; (defn stop-node
;;   [compute-name node-ids]
;;   (->> (lookup-nodes compute-name node-ids)
;;        (compute/shutdown :virtualbox)))

;; (defn restart-node
;;   [compute-name node-id])


(defn destroy-node
  [compute-name & node-ids]
  (->> (doto (lookup-nodes compute-name node-ids) println)
       (map (partial compute/destroy-node (compute-service compute-name)))))