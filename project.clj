(defproject com.fireworksproject/cloudmill "0.1.0-SNAPSHOT"
  :description "FIXME Pallet project for cloudmill"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.cloudhoist/pallet "0.7.2"]
                 [org.cloudhoist/pallet-jclouds "1.4.2"]
                 [org.cloudhoist/pallet-vmfest "0.2.2"]

                 ;; To get started we include all jclouds compute providers.
                 ;; You may wish to replace this with the specific jclouds
                 ;; providers you use, to reduce dependency sizes.
                 [org.jclouds/jclouds-allblobstore "1.4.2"]
                 [org.jclouds/jclouds-allcompute "1.4.2"]
                 [org.jclouds.driver/jclouds-slf4j "1.4.2"
                  ;; the declared version is old and can overrule the
                  ;; resolved version
                  :exclusions [org.slf4j/slf4j-api]]

                 [org.jclouds.driver/jclouds-sshj "1.4.2"]
                 [ch.qos.logback/logback-classic "1.0.0"]

                 ;; Interface
                 [conch "0.2.1"]

                 ;; Crates
                 [org.cloudhoist/git "0.7.0-beta.1"]]
  
  :dev-dependencies [[org.cloudhoist/pallet
                      "0.7.2" :type "test-jar"]
                     [org.cloudhoist/pallet-lein "0.5.2"]]
  :profiles {:dev
             {:dependencies
              [[org.cloudhoist/pallet "0.7.2" :classifier "tests"]]
              :plugins [[org.cloudhoist/pallet-lein "0.5.2"]]}}
  :local-repo-classpath true
  :repositories
  {"sonatype-snapshots" "https://oss.sonatype.org/content/repositories/snapshots"
   "sonatype" "https://oss.sonatype.org/content/repositories/releases/"}
  ;; :repl-options {:init (do (require 'cloudmill.repl)
  ;;                          (cloudmill.repl/force-slf4j))}
  )
