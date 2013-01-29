(ns cloudmill.aws
  (:require [cloudmill.dispatch :as dsp]
            [cloudmill.configuration :as cfg]))

(defn credentials
  "Store AWS credientials in configuration."
  [identity credentials]
  (cfg/swap-config! cfg/config assoc ::credentials {:identity identity :secret credentials}))

(defn aws
  "Interact with Amazon AWS.

Subcommands:
  credentials <identity> <credential>"
  [command & args]
  (case command
    "credentials" (apply credentials args)))

(dsp/register-command "aws" #'aws)