(ns cloudmill.bootstrap
  (:require [pallet
             core
             compute
             [node :as n]]
            [conch.core :as sh]
            [clojure.java.io :as io]
            [pallet.compute :as compute]
            [pallet.configure :refer [compute-service]]
            [pallet.compute.vmfest :as vmfest]

            cloudmill.repl))

(def bootstrap-sh (io/resource "bootstrap.sh"))
(def pallet-config (io/resource "pallet-config.clj"))

(def debian-vdi-url "https://s3.amazonaws.com/vmfest-images/debian-6.0.2.1-64bit-v0.3.vdi.gz")

(defn add-image
  "Wrap pallet.compute.vmfest/add-image so it has nicer semantics.

   Return true if image was successfully added (or was already added),
   false otherwise."
  [image]
  (try (nil? (vmfest/add-image (compute-service :virtualbox) image))
       (catch clojure.lang.ExceptionInfo _
         false)))

(defn attempt-to-add-image
  "Loops for up to timeout milliseconds while trying to connect to the vboxwebsrv process.
   Once connected, download and add a .vdi if needed."
  [timeout image]
  (let [start-time (System/currentTimeMillis)]
    (loop [current-time (System/currentTimeMillis), added? false]
      (when (and (not added?) (< (- current-time start-time) timeout))
        (recur (System/currentTimeMillis) (add-image image))))))

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
    
    
    (sh/feed-from-string proc script)
    (future (sh/stream-to proc :out (io/as-file (str logfile ".log"))))
    (future (sh/stream-to proc :err (io/as-file (str logfile ".err.log"))))

    (println "Starting virtualbox...")
    (attempt-to-add-image 1000 debian-vdi-url)
    (fn []
      (doto proc sh/destroy sh/exit-code)
      (sh/exit-code (sh/proc "killall" "vboxwebsrv")))))
