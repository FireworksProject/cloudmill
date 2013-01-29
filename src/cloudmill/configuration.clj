(ns cloudmill.configuration
  (:require [clojure.java.io :as io]
            [clojure.string :as str]

            [cloudmill.file-atom :refer [file-atom] :as fa]))

(def file-separator (System/getProperty "file.separator"))
(def default-home (str/join file-separator
                            [(System/getProperty "user.home") ".cloudmill"]))

(def cloudmill-home-env "CLOUDMILL_HOME")
(def cloudmill-home (or (System/getenv cloudmill-home-env) default-home))
(def cloudmill-config-file (str/join file-separator [cloudmill-home "config.clj"]))

(def config (file-atom {} cloudmill-config-file))

(defn swap-config!
  [config fun & args]
  (apply fa/swap! config fun args))

(defn reset-config!
  [config value]
  (fa/reset! config value))