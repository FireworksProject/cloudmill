(ns cloudmill.dispatch)

(declare help)

(def commands {"help" #'help})

(defn register-command
  "Register the function pointed to by var to be the handler for command-name."
  [command-name var]
  (alter-var-root #'commands assoc command-name var))

(defn unregister-command
  [command-name]
  (alter-var-root #'commands dissoc command-name))

(defn print-commands
  [commands]
  (doseq [k (keys commands)]
    (println "\t" k)))

(defn dispatch
  [command args]
  (if-let [handler (get commands command)]
    (apply handler args)
    (do
      (println "Unknown command:" command)
      (println "Perhaps you meant one of:")
      (print-commands commands))))

(defn help
  "Print documenation describing how to use a given command.

Example: cloudmill help virtualbox"
  [& [command & args]]
  (if-let [var (get commands command)]
    (println (:doc (meta var)))
    (do
      (println "Unknown command:" command)
      (println "Perhaps you meant one of:")
      (print-commands commands))))