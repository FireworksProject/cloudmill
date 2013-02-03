(ns cloudmill.configuration
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            

            [cloudmill.file-atom :refer [file-atom] :as fa])
  (:import [ch.qos.logback.classic LoggerContext]
           [org.slf4j LoggerFactory]
           [ch.qos.logback.classic.joran JoranConfigurator]))

(def file-separator (System/getProperty "file.separator"))
(def default-home (str/join file-separator
                            [(System/getProperty "user.home") ".cloudmill"]))

(def cloudmill-home-env "CLOUDMILL_HOME")
(def cloudmill-home (or (System/getenv cloudmill-home-env) default-home))
(def cloudmill-config-file (str/join file-separator [cloudmill-home "config.clj"]))

(def default-logdir (str/join file-separator [cloudmill-home "logs"]))

(def config (file-atom {} cloudmill-config-file))

(defn swap-config!
  [config fun & args]
  (apply fa/swap! config fun args))

(defn reset-config!
  [config value]
  (fa/reset! config value))

(defn setup-logging
  [logdir logback-xml]
  (let [logging-context (LoggerFactory/getILoggerFactory)
        configurator    (doto (JoranConfigurator.) (.setContext logging-context))]
    (doto logging-context .reset (.putProperty "log-dir" logdir))
    (.doConfigure configurator logback-xml)))