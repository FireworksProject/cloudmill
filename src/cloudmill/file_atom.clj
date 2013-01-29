(ns cloudmill.file-atom
  (:refer-clojure :exclude [swap! reset!])
  (:require [clojure.core :as clj]
            [clojure.java.io :as io])
  (:import [java.util.concurrent.atomic AtomicReference]
           [java.io
            File FileOutputStream OutputStreamWriter
            PushbackReader]))

(defprotocol IDurableStore
  "Attempt to commit the state to a durable storage. Return false
   on failure."
  (-commit! [this value]))

(defprotocol IDurableAtom
  (-swap! [this f args]))

(defn atomic-store
  "Returns true if the update succeeds."
  [store state old new]
  (locking store
    (and (.compareAndSet state old new)
         (-commit! store new))))

(defn notify-watches
  [atom watches old-val new-val]
  (doseq [[key fun] watches]
    (try
      (fun key atom old-val new-val)
      (catch Exception e
        (throw (RuntimeException. e))))))

(defn validate
  [fun value]
  (if fun (or (fun value)
              (throw (IllegalStateException. "Invalid reference state")))))

(deftype DurableAtom
    [meta validator watches store state]

  clojure.lang.IMeta
  (meta [_] meta)

  clojure.lang.IRef
  (setValidator [_ v] (clj/reset! validator v))
  (getValidator [_] @validator)
  (getWatches [_] @watches)
  (addWatch [this key watch] (do (clj/swap! watches assoc key watch)  this))
  (removeWatch [this key] (do (clj/swap! watches dissoc key) this))

  clojure.lang.IDeref
  (deref [_] (.get state))

  IDurableAtom
  (-swap! [this fun args]
    (loop []
      (let [old (.get state), new (apply fun old args)]
        (validate @validator new)
        (if (atomic-store store state old new)
          (do (notify-watches this @watches old new) new)
          (recur))))))

(defn swap!
  "Atomically swap the value of the atom in memory and in storage."
  [atom fun & args]
  (-swap! atom fun args))

(defn reset!
  "Atomically set the value of the atom in memory and in storage."
  [atom val]
  (swap! atom (constantly val)))

(deftype FileStore
    [^File file temp-prefix temp-extension]

  IDurableStore
  (-commit! [_ value]
    (let [temp-file (File/createTempFile temp-prefix temp-extension)
          temp-fos (FileOutputStream. temp-file)
          temp-writer (OutputStreamWriter. temp-fos)]
      (try
        (print-method value temp-writer)
        (.flush temp-writer)
        (-> temp-fos .getChannel (.force true))
        (-> temp-fos .getFD .sync)
        (.close temp-fos)
        (.delete file)
        (.renameTo temp-file file)))))

(defn- file-store
  [file]
  (FileStore. file "cloudmill-tmp" ".clj"))

(defn read-form
  [^File file]
  (with-open [reader (PushbackReader. (io/reader file))]
    (read reader false nil)))

(defn file-atom
  "Creates a DurableAtom backed by file. The inital value will be
   the value of the form in the file or, if the file is empty, the
   value of init."
  [init filename & options]
  (let [file (io/file filename)
        path (.getAbsolutePath file)
        options (apply hash-map options)]
    (DurableAtom.
     (:meta options)
     (:validator options)
     (:watchest options)
     (file-store file)
     (AtomicReference. (if (.exists file) (or (read-form file) init) init)))))