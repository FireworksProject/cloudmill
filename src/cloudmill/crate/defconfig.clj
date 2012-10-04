(ns cloudmill.crate.defconfig)

(defn- config-def
  [system [key value]]
  `(def ~(symbol (name key))
     ~(cond (and (symbol? value) (fn? @(resolve value))) value
            :else `(fn [session#] (get-in session# [~system ~key] ~value)))))

(defmacro defconfig
  "Declare a set of configuration parameters for the given system.

   config => parameter default

   The macro will define a function for each parameter. The function
   name will be (name parameter). The function will look up the value
   defined for the parameter in the session object. If it does not
   find a value for the parameter, it will return the default value
   declared here.
 "
  [system & configs]
  {:pre [(even? (count configs))]}
  `(do ~@(map (partial config-def system) (partition 2 configs))))