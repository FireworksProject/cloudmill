(ns cloudmill.crate.web-server
  (:use [pallet.action
         [exec-script :only [exec-checked-script]]
         [file :only [sed]]]

        [cloudmill.crate.defconfig :only [defconfig]])
  (:require [pallet.crate git]))

(defconfig :web-server
  :repo-url "git://github.com/FireworksProject/web_server.git"
  :repo-directory "~/web_server"
  :couchdb-directory "/opt/couchdb/")

(defn clone
  [session]
  (-> session
      (pallet.crate.git/git)
      (exec-checked-script
       "Checkout FireworksProject/web_server.git"

       (rm -rf ~(repo-directory session))
       (git clone ~(repo-url session) ~(repo-directory session)))))

(defn update
  [session]
  (-> session
      (exec-checked-script
       "Updating FireworksProject/web_server.git"

       (cd ~(repo-directory session))
       (git pull origin master))))

(defn compile-couchdb
  "Compile CouchDB using the scripts in FireworksProject/web_server.git"
  [session]
  (-> session
      (exec-checked-script
       "Compile CouchDB"
       
       (cd ~(repo-directory session))
       (bin/setup_couchdb "&> /dev/null"))
      (assoc session :couchdb-directory (couchdb-directory session))))