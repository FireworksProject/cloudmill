(ns cloudmill.main
  (:require [pallet
             core
             compute
             [node :as n]]
            [conch.core :as sh]
            [clojure.java.io :as io])
  (:use [pallet.configure :only [compute-service]]
        [cloudmill.groups.couchdb :only [couchdb]]))

(def bootstrap-sh (io/resource "bootstrap.sh"))
(def pallet-config (io/resource "pallet-config.clj"))

(defn bootstrap
  [logfile]
  (let [script (slurp bootstrap-sh)
        proc (sh/proc "sh")
        pallet-config-path (io/as-file (str (System/getenv "HOME") "/.pallet/config.clj"))]

    (when-not  (.exists pallet-config-path)
      (io/make-parents pallet-config-path)
      (spit pallet-config-path (slurp pallet-config)))
    
    
    (future (sh/feed-from-string proc script))
    (future (sh/stream-to proc :out (io/as-file (str logfile ".log"))))
    (future (sh/stream-to proc :err (io/as-file (str logfile ".err.log"))))

    (fn []
      (sh/exit-code (sh/destroy proc))
      (sh/exit-code (sh/proc "killall" "vboxwebsrv")))))

(defn get-create-fn
  [group]
  (ns-resolve group 'create))

(defn get-destroy-fn
  [group]
  (ns-resolve group 'destroy))

(defn valid-group?
  [group]
  (try
    (require group)
    (and (get-create-fn group)
         (get-destroy-fn group))
    (catch Exception _ false)))

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
                 :ssh-port        n/ssh-port}]
    (reduce #(assoc %1 (key %2) ((val %2) node))
            {}
            key-map)))

(defn create
  ([group-name]
     (create group-name (compute-service :virtualbox)))
  ([group-name compute]
     (if-let [create (get-create-fn group-name)]
       (map canonicalize-node (:selected-nodes (@create compute)))
       'fail)))

(defn destroy
  ([group-name]
     (destroy group-name (compute-service :virtualbox)))
  ([group-name compute]
     (if-let [destroy (get-destroy-fn group-name)]
       (map canonicalize-node (:selected-nodes (@destroy compute)))
       'fail)))