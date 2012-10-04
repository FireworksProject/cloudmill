(ns cloudmill.groups.couchdb
  "Node defintions for cloudmill"
  (:use
   [pallet.core :only [group-spec server-spec node-spec]]
   [pallet.crate.automated-admin-user :only [automated-admin-user]]
   [pallet.phase :only [phase-fn]]
   [pallet.action
    [exec-script :only [exec-checked-script]]
    [file :only [sed]]]
   [pallet.session :only [target-ip]])
  (:require
   [cloudmill.crate couchdb]))

(def default-node-spec
  (node-spec
   :image {:os-family :debian}
   :network {:inbound-ports [5984]}
   :hardware {:min-cores 1 :min-ram 600}))

(def
  ^{:doc "Defines the type of node cloudmill will run on"}
  base-server
  (server-spec
   :phases
   {:bootstrap (phase-fn (automated-admin-user))}))

(def
  ^{:doc "Define a server spec for cloudmill"}
  couchdb-server
  (server-spec
   :phases
   {:configure (phase-fn
                cloudmill.crate.couchdb/install
                cloudmill.crate.couchdb/configure
                cloudmill.crate.couchdb/start)}))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  couchdb
  (group-spec
   "cloudmill"
   :extends [base-server couchdb-server]
   :node-spec default-node-spec))
