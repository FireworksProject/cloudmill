(ns cloudmill.config)


;;; Configuration Processing
;;;
;;; 1. Create nodes
;;; 2. Bind roles to nodes
;;; 3. Setup connectivity

(defn create-node
  [hardware-spec [roles spec]]
  (if-let [hardware (get hardware-spec spec false)]
    (assoc hardware :roles (if (keyword? roles) #{roles} (set roles)))
    (throw (Exception. (format "Hardware-spec (%s) not defined for role%s: %s" spec (if (keyword? roles) "" "s") roles)))))

(defn create-nodes
  "Take a cluster-spec and return a sequence of nodes."
  [{:keys [nodes hardware-spec] :as cluster-spec}]
  (map (partial create-node hardware-spec) nodes))

