(ns cloudmill.cluster-decl)

(defn make-node-spec
  [decl hardware-name]
  (get-in decl [:hardware-spec hardware-name]))

(defn make-node-specs
  [decl]
  (vals (:hardware-spec decl)))

(comment
  (def cluster-decl
    {:roles #{:web-app :web-server :db :monitoring}
     :connectivity #{[:web-app :db] 
                     [:web-app :web-server]
                     [:web-server :web-app]
                     [:web-server :monitoring]
                     [:db         :monitoring]}
     :hardware #{:large :medium :small}
     :nodes {[:web-server :web-app :db] :meduim
             :monitoring :small}})
  (def hardware-decl {:small {:image {:os-family :debian}
                              :network {:inbound-ports [5984]}
                              :hardware {:min-cores 1 :min-ram 600}}}))