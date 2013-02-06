(ns cloudmill.virtualbox
  (:require [cloudmill.dispatch :as dsp]
            [cloudmill.bootstrap :refer [bootstrap]]
            [cloudmill.configuration :as cfg]

            [pallet.core :refer [group-spec node-spec converge]]
            [pallet.api :refer [plan-fn]]
            [pallet.node :refer [primary-ip ssh-port]]
            [pallet.crate.automated-admin-user :refer [automated-admin-user]]
            [pallet.actions :refer [exec-script]]
            [pallet.compute.vmfest :as vmfest]
            [pallet.configure :refer [compute-service]]))

(def groups {"debian" {:group-spec (group-spec
                                    "debian-vm"
                                    :phases {:bootstrap (plan-fn (automated-admin-user))}
                                    :node-spec (node-spec :image {:os-family :debian}))
                       :image-url "https://s3.amazonaws.com/vmfest-images/debian-6.0.2.1-64bit-v0.3.vdi.gz"}
             "ubuntu" {:group-spec (group-spec
                                    "ubuntu-vm"
                                    :phases {:bootstrap (plan-fn (automated-admin-user))}
                                    :node-spec (node-spec :image {:os-family :ubuntu}))
                       :image-url "https://s3.amazonaws.com/vmfest-images/ubuntu+12.04.vdi.gz"}})

(defn add-image
  "Wrap pallet.compute.vmfest/add-image so it has nicer semantics.

   Return true if image was successfully added (or has previously
   been added), false otherwise."
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

(defn ensure-image
  [image-spec image-url]
  (when (empty? (vmfest/find-images (compute-service :virtualbox) image-spec))
    (printf "Downdloading image: %s\n" image-url)
    (.flush *out*)
    (let [add-image-future (future (add-image image-url))
          timeout 3000, line-width 80]
      (loop [done-sentinel (deref add-image-future timeout :not-done)
             x-pos 1]
        (when (= done-sentinel :not-done)
          (if (zero? (mod x-pos line-width))
            (print "\n")
            (print "."))
          (.flush *out*)
          (recur (deref add-image-future timeout :not-done) (inc x-pos)))))
    (println "")))

(defn start-vm
  [name & args]
  (let [group (get-in groups [name :group-spec])]
    (ensure-image (:image group) (get-in groups [name :image-url]))
    (println (format "starting %s vm..." name))
    (-> @(converge {group 1} :compute (compute-service :virtualbox))
        :new-nodes
        first
        :node)))

(defn stop-vm
  [name & args]
  (println (format "stopping %s vm..." name))
  @(converge {(get-in groups [name :group-spec]) 0} :compute (compute-service :virtualbox))
  (println "done."))

(defn output-results
  [node]
  (println (format "Successfully started a VM. You can ssh into the VM at %s:%d"
                   (primary-ip node) (ssh-port node))))

(defn virtualbox
  "Start and stop one-off virtual machines.

Understood operations are:
  list
  start [os name]
  stop [os name]"
  [command & [name args]]
  (let [logdir (:logdir @cfg/config cfg/default-logdir)
        stop-vboxweb (bootstrap (str logdir "/vboxweb"))
        name (or name "ubuntu")]
    (-> (Runtime/getRuntime) (.addShutdownHook (Thread. stop-vboxweb)))
    
    (case command
      "list"  (do (println "Available OS images:") (doseq [grp (keys groups)] (println "  " grp)))
      "start" (output-results (start-vm name))
      "stop"  (stop-vm name))))

(dsp/register-command "vbox" #'virtualbox)
