Cluster Spec
============

This specifies the constraints the given cluster must satisfy.

Example:

```clojure
{:roles #{:web-app :web-server :db :monitoring}
 :connectivity #{[:web-app :db] 
                 [:web-app :web-server]
                 [:web-server :web-app]
                 [:web-server :monitoring]
                 [:db         :monitoring]}
 :hardware #{:large :medium :small}
 :nodes {[:web-server :web-app :db] :meduim
         :monitoring :small}}
```

Roles
-----

Specify names for given roles that machines can play in the cluster. 

Example:

```clojure
:web-server
:web-app
:db
:monitoring
```

Connectivity
------------

Specifies how roles are connected.

Example:

    :web-app --> :db
    :web-app <--> :web-server
    :web-server --> :monitoring
    :db  --> :monitoring

The above is how I would like to write the connectivity constraints.
Here is how it would look like in pure clojure data:

```clojure
#{[:web-app :db]
  [:web-app :web-server]
  [:web-server :web-app]
  [:web-server :monitoring]
  [:db :monitoring]}
```

Hardware
--------

Specifies names for the particular machines configurations to be used.

Example:

```clojure
:large
:medium
:small
```

Nodes
-----

Pair roles to the hardware they require.

Example:

```clojure
{[:web-server :web-app :db] :medium, :monitoring :small}
```
