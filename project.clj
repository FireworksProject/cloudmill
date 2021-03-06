(defproject com.fireworksproject/cloudmill "0.1.1-SNAPSHOT"
  :description "FIXME Pallet project for cloudmill"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.cloudhoist/pallet "0.8.0-alpha.8"]
                 [org.cloudhoist/pallet-jclouds "1.5.1"]
                 [org.cloudhoist/pallet-vmfest "0.2.4"]

                 ;; To get started we include all jclouds compute providers.
                 ;; You may wish to replace this with the specific jclouds
                 ;; providers you use, to reduce dependency sizes.
                 [org.jclouds/jclouds-allblobstore "1.5.2"]
                 [org.jclouds/jclouds-allcompute "1.5.2"]
                 [org.jclouds.driver/jclouds-slf4j "1.5.2"
                  ;; the declared version is old and can overrule the
                  ;; resolved version
                  :exclusions [org.slf4j/slf4j-api]]

                 [org.jclouds.driver/jclouds-sshj "1.5.2"]
                 [ch.qos.logback/logback-classic "1.0.0"]

                 ;; Interface
                 [conch "0.2.1"]]
  
  :local-repo-classpath true
  :jvm-opts ["-Djava.awt.headless=true" "-Dlogback.configurationFile=resources/logback.xml"]
  :uberjar-name "cloudmill.jar"
  :main cloudmill.main
  :repositories
  {"sonatype-snapshots" "https://oss.sonatype.org/content/repositories/snapshots"
   "sonatype" "https://oss.sonatype.org/content/repositories/releases/"}
  ;; :repl-options {:init (do (require 'cloudmill.repl)
  ;;                          (cloudmill.repl/force-slf4j))}
  )
