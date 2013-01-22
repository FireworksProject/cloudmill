(ns cloudmill.main
  (:require [cloudmill.bootstrap :refer [bootstrap]]))


(comment
  (def vmfest (compute-service :virtualbox))
  (use '[pallet.core :only [group-spec node-spec server-spec]]
       '[pallet.phase :only [phase-fn]]
       '[pallet.crate.automated-admin-user :only [automated-admin-user]]
       '[pallet.action.exec-script :only [exec-script]]
       '[pallet.session :only [nodes-in-group]])
  (def base-server (server-spec :phases {:bootstrap (phase-fn (automated-admin-user))}))
  (def debian-node (node-spec :image {:os-family :debian
                                      :os-64-bit? true}))
  (def debian-group (group-spec "debian-vms" :node-spec debian-node
                                :extends [base-server]))
  (pallet.core/converge {debian-group 1} :compute vmfest)
  (pallet.core/converge {debian-group 0} :compute vmfest)

  (use 'vmfest.manager 'pallet.compute.vmfest)

  (def g (group-spec "xxx"
                     :phases
                     {:bootstrap (phase-fn (automated-admin-user))
                      :configure (phase-fn 
                                  (exec-script (echo "hello")))}
                     :node-spec (node-spec :image {:os-family :debian})))
  
  )