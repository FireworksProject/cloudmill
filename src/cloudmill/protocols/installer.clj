(ns cloudmill.protocols.installer)

(defprotocol Installer
  "Abstraction of the array of tools that can be used to install
software on a node."
  (make-install-fn [this]
    "Return a function that can be used by the a pallet phase-fn to
    install a service."))