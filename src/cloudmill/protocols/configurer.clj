(ns cloudmill.protocols.configurer)

(defprotocol Parameter
  (valid? [this value]))

(defprotocol Configurer
  "Abstraction of the configuration process for a particular service.

We need to be able to retrieve a set of parameters for the given
service. This set of parameters will then be used by the constraint
propagation code to generate a configuration for the deploy job.

Once the configuration is resolved, we can retrieve a function that
can be used by pallet to configure the service appropriately."
  (get-parameters [this]
    "Return a set of parameters this configurer needs to be
    specified.")
  (make-configure-fn [this parameters]
    "Returns a function that can be run in a pallet phase-fn. This
 function will apply the specified parameters to the service."))