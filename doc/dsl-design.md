# Cluster Spec
==============

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

## Roles

Specify names for given roles that machines can play in the cluster. 

Example:

```clojure
:web-server
:web-app
:db
:monitoring
```

## Connectivity

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

## Hardware

Specifies names for the particular machines configurations to be used.

Example:

```clojure
:large
:medium
:small
```

## Nodes

Pair roles to the hardware they require.

Example:

```clojure
{[:web-server :web-app :db] :medium, :monitoring :small}
```

The above example specifies that the :web-server, :web-app, and :db
are all to run on the same machine, while the :monitoring role is to
live on its own machine. The two machines to be used are to have
:medium and :small capabilities, respectively.

These capabilities will be defined in detail by the hardware
specification.

# Service Spec
--------------

A Service is a concrete software package that provides a given Role.
By installing a Service on some Node you enable the Node to play a
given Role in the cluster. For example, if a Node can play the
:web-server role, if it has the :apache2 service installed on it.


A Service spec must define multiple pieces of information:

* name
* provides
* installer
* configurer

A Service may define:

* depends

For example:

```clojure
{:name :apache2
 :provides :web-server
 :installer #cloudmill/packager "apache" ; use apt-get or yum
 :configurer #cloudmill/remote-file "http://foo.com/bar.sh"}
 
{:name :customer-foo-web-app
 :provides :web-app
 :installer #cloudmill/github "https://github.com/foo/web-app.git"
 :configurer #cloudmill/stevedore (sed {"payment-provider-mock"
                                        "amazon-payments"}
                                        "/path/to/config/file")
 :depends #{:apache2 :mod_wsgi :couchdb}}
 
{:name :couchdb
 :provides :db
 :installer #cloudmill/chef-solo "http://foo.com/path/to/recipe"
 :configurer :installer ; configuration was provided by installer
 }
 ```

Cloudmill will provide various installers and configurers. This can be
extended by third-parties as well.

# Hardware Spec
===============

This is basically going to be a map that can be handed to pallet.core/node-spec.

# Core Abstractions
===================

## Cluster Specification Abstractions

* Role
* Connection

## Service Abstractions

* installer
* configurer
* startable


## Cluster Job Abstrations

These abstractions are involved with the process of setting up,
tearing down, or otherwise changing the configuration of a cluster.

* environment
* config definition

### A Sketch of the algorithm

1. Read the cluster spec
2. Verify Cloud Provider(s)
3. Load configurers
4. Query configurers and environment for config definition. This step
could halt the job, if a complete config definition is not found. We
will leverage core.logic for this.
5. Create the necessary pallet server and group specs.
6. Launch the job using pallet.

The config definition will be picked up during the :settings phase and
used to generate the proper installers and configurers for each pallet group.
