(ns cloudmill.crate.couchdb
  (:use
   [cloudmill.crate.defconfig :only [defconfig]]
   [pallet.action
    [exec-script :only [exec-checked-script]]
    [file :only [sed]]]
   [pallet.session :only [target-ip os-family]])
  (:require [pallet.crate git]
            [cloudmill.crate web-server]))

(defconfig :couchdb
  :port 5984
  :bind-address target-ip
  :init-script-orig "etc/init.d/couchdb"
  :local-config-file "etc/couchdb/local.ini"
  :couchdb-directory "/opt/couchdb/")

(defmacro debug
  [form]
  `(let [result# ~form]
     (println '~form "=>" result#)
     result#))

(defn init-script-path
  [session]
  (case (os-family session)
    :debian "/etc/init.d/couchdb"))

(defn update-rc
  [session]
  (case (os-family session)
    :debian '(update-rc.d couchdb defaults)))

(defn install
  [session]
  (-> session
      cloudmill.crate.web-server/clone
      cloudmill.crate.web-server/compile-couchdb))

(defn configure
  [session]
  (-> session
      (exec-checked-script
       "Setup CouchDB's init.d script."

       (cp ~(str (couchdb-directory session) (init-script-orig session))
           ~(init-script-path session))
       ~(update-rc session))

      (sed (str (couchdb-directory session) (local-config-file session))
           {";port = 5984" (str "port = " (port session))
            ";bind_address = 127.0.0.1" (str "bind_address = " (bind-address session))})))

(defn start
  [session]
  (-> session
      (exec-checked-script
       "Start CouchDB"

       (~(init-script-path session) start))))