(ns pallet.task.add-image
  (:use [pallet.configure :only [compute-service]])
  (:require [pallet.compute.vmfest :as vmfest]))

(defn add-image
  ""
  [request url]
  (println "Adding image:" url)

  (vmfest/add-image (compute-service :virtualbox) url))
