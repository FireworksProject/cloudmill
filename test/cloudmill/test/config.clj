(ns cloudmill.test.config
  (:use cloudmill.config
        midje.sweet
        clojure.test))

(fact "For each key-value pair in :nodes create a node."
  (create-nodes {:roles #{:db}
                 :hardware-spec {:small {:image {:os-family :debian}
                                         :network {:inbound-ports #{5984}}
                                         :hardware {:min-cores 1 :min-ram 600}}}
                 :nodes {:db :small}})
  =>
  (just [{:image {:os-family :debian}
          :network {:inbound-ports #{5984}}
          :hardware {:min-cores 1 :min-ram 600}
          :roles #{:db}}])

  (create-nodes {:roles #{:db :web-server}
                 :hardware-spec {:small {:image {:os-family :debian}
                                         :network {:inbound-ports #{5984}}
                                         :hardware {:min-cores 1 :min-ram 600}}
                                 :medium {:image {:os-family :debian}
                                          :network {:inboud-ports #{80}}
                                          :hardware {:min-cores 2 :min-ram 1024}}}
                 :nodes {:db :small, :web-server :medium}})
  =>
  (just {:image {:os-family :debian}
         :network {:inbound-ports #{5984}}
         :hardware {:min-cores 1 :min-ram 600}
         :roles #{:db}}
        {:image {:os-family :debian}
         :network {:inboud-ports #{80}}
         :hardware {:min-cores 2 :min-ram 1024}
         :roles #{:web-server}}
        :in-any-order)

  (create-nodes {:roles #{:db :web-server}
                 :hardware-spec {:small {:image {:os-family :debian}
                                         :network {:inbound-ports #{5984}}
                                         :hardware {:min-cores 1 :min-ram 600}}
                                 :medium {:image {:os-family :debian}
                                          :network {:inboud-ports #{80}}
                                          :hardware {:min-cores 2 :min-ram 1024}}}
                 :nodes {[:db :web-server] :medium}})
  =>
  (just [{:image {:os-family :debian}
          :network {:inboud-ports #{80}}
          :hardware {:min-cores 2 :min-ram 1024}
          :roles #{:web-server :db}}])

  (create-nodes {:roles #{:web-app :web-server :db :monitoring}
                 :connectivity #{[:web-app :db] 
                                 [:web-app :web-server]
                                 [:web-server :web-app]
                                 [:web-server :monitoring]
                                 [:db         :monitoring]}
                 :hardware-spec {:small {:image {:os-family :debian}
                                         :network {:inbound-ports #{5984}}
                                         :hardware {:min-cores 1 :min-ram 600}}
                                 :medium {:image {:os-family :debian}
                                          :network {:inboud-ports #{80}}
                                          :hardware {:min-cores 2 :min-ram 1024}}
                                 :large {:image {:os-family :debian}
                                         :hardware {:min-cores 4 :min-ram 4096}}}
                 :nodes {[:web-server :web-app :db] :large
                         :monitoring :small}})
  =>
  (just {:image {:os-family :debian}
          :hardware {:min-cores 4 :min-ram 4096}
          :roles #{:web-server :web-app :db}}
         {:image {:os-family :debian}
          :network {:inbound-ports #{5984}}
          :hardware {:min-cores 1 :min-ram 600}
          :roles #{:monitoring}}
         :in-any-order))

