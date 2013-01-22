(ns cloudmill.main
  (:require [pallet
             core
             compute
             [node :as n]]
            [conch.core :as sh]
            [clojure.java.io :as io]
            [pallet.compute :as compute]
            [pallet.configure :refer [compute-service]]

            cloudmill.repl))

(def bootstrap-sh (io/resource "bootstrap.sh"))
(def pallet-config (io/resource "pallet-config.clj"))

(defn bootstrap
  [logfile]
  ;; Setup logging to work with pallet.
  (cloudmill.repl/force-slf4j) 
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
      (doto proc sh/destroy sh/exit-code)
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

(defn create
  ([group-name]
     (create group-name (compute-service :virtualbox)))
  ([group-name compute-name]
     (if-let [create (get-create-fn group-name)]
       (map canonicalize-node (:selected-nodes (@create compute-name)))
       'fail)))

(defn destroy
  ([group-name]
     (destroy group-name (compute-service :virtualbox)))
  ([group-name compute-name]
     (if-let [destroy (get-destroy-fn group-name)]
       (map canonicalize-node (:selected-nodes (@destroy compute-name)))
       'fail)))
