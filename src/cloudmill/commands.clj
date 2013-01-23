(ns cloudmill.commands)

(declare help)

(def commands {"help" #'help})

(defn register-command
  "Register the function pointed to by var to be the handler for command-name."
  [command-name var]
  (alter-var-root #'commands assoc command-name var))

(defn dispatch
  [command & args]
  (if-let [handler (get commands command)]
    (apply handler args)
    (do
      (println "Unknown command:" command)
      (println "Perhaps you meant one of:")
      (doseq [k (keys commands)]
        (println "\t" k)))))

(defn help
  "Print documenation describing how to use a given command."
  [& [command & args]]
  (if-let [var (get commands command)]
    (println (:doc (meta var)))
    (println "Unknown command:" command)))