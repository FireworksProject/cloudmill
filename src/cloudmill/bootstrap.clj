(ns cloudmill.bootstrap
  (:require [pallet
             core
             compute
             [node :as n]]
            [conch.core :as sh]
            [clojure.java.io :as io]
            [pallet.compute :as compute]
            [pallet.configure :refer [compute-service]]
            [clojure.tools.logging :as l]

            cloudmill.repl))

(def bootstrap-sh (io/resource "bootstrap.sh"))
(def pallet-config (io/resource "pallet-config.clj"))

(defn bootstrap
  [logfile]
  ;; Setup logging to work with pallet.
  (l/log-capture! 'vmfest.virtualbox.image)
  (cloudmill.repl/force-slf4j)
  
  (let [script (slurp bootstrap-sh)
        proc (sh/proc "sh")
        pallet-config-path (io/as-file (str (System/getenv "HOME") "/.pallet/config.clj"))]

    (when-not  (.exists pallet-config-path)
      (io/make-parents pallet-config-path)
      (spit pallet-config-path (slurp pallet-config)))
    
    (sh/feed-from-string proc script)
    (future (sh/stream-to proc :out (io/as-file (str logfile ".log"))))
    (future (sh/stream-to proc :err (io/as-file (str logfile ".err.log"))))

    (println "Starting virtualbox...")
    (fn []
      (doto proc sh/destroy sh/exit-code)
      (sh/exit-code (sh/proc "killall" "vboxwebsrv")))))
