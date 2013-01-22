(ns cloudmill.bootstrap
  (:require [pallet
             core
             compute
             [node :as n]]
            [conch.core :as sh]
            [clojure.java.io :as io]
            [pallet.compute :as compute]
            [pallet.configure :refer [compute-service]]
            [pallet.compute.vmfest :refer [add-image]]

            cloudmill.repl))

(def bootstrap-sh (io/resource "bootstrap.sh"))
(def pallet-config (io/resource "pallet-config.clj"))

(def debian-vdi-url "https://s3.amazonaws.com/vmfest-images/debian-6.0.2.1-64bit-v0.3.vdi.gz")

(defn bootstrap
  [logfile]
  ;; Setup logging to work with pallet.
  (cloudmill.repl/force-slf4j)
  (let [script (slurp bootstrap-sh)
        proc (sh/proc "sh")
        pallet-config-path (io/as-file (str (System/getenv "HOME") "/.pallet/config.clj"))
        add-image-job (future (add-image (compute-service :virtualbox) debian-vid-url))]

    (when-not  (.exists pallet-config-path)
      (io/make-parents pallet-config-path)
      (spit pallet-config-path (slurp pallet-config)))
    
    
    (future (sh/feed-from-string proc script))
    (future (sh/stream-to proc :out (io/as-file (str logfile ".log"))))
    (future (sh/stream-to proc :err (io/as-file (str logfile ".err.log"))))

    (println "Bootstrapping...")
    @add-image-job
    (fn []
      (doto proc sh/destroy sh/exit-code)
      (sh/exit-code (sh/proc "killall" "vboxwebsrv")))))
