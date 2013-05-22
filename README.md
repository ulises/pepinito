# pepinito

A writer of a subset of Python's pickle serialisation protocol in Clojure.

## Include it

Include ``[pepinito "0.0.1-SNAPSHOT"]`` in your project.clj if you're
using leiningen. Alternatively, add
```XML
<dependency>
  <groupId>pepinito</groupId>
  <artifactId>pepinito</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```
if you're using maven.


## Use it

Pickle a vector into a file:

```clojure
(ns com.example.my
  (require [pepinito.core :as pickle])
  (import [java.io DataOutputStream FileOutputStream]))

(def out (DataOutputStream. (FileOutputStream. "some-file.pickle")))

(pickle/dump out [1 2 3])
```

or into a socket:

```clojure
(ns com.example.my
  (require [pepinito.core :as pickle])
  (import [java.io DataOutputStream BufferedOutputStream]
          [java.net Socket]))

(defn send [o]
  (let [c (Socket. "localhost" 9999)
        out (DataOutputStream. (BufferedOutputStream. (.getOutputStream c)))]
    (pickle/dump out o)
    (.close out)))

(send [1 2 3])
```

The following data structures can be pickled:

  * Numbers, i.e. Integer, Float, Double, and Long
  * Strings. Both short (len < 256) and long.
  * Tuples, i.e. clojure.lang.PersistentVector
  * Order preserving collections. Only lists are picklable for now
    though, i.e. clojure.lang.PersistentList.

# Developing

Please fork and send pull-requests my way :)

## How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
