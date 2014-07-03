# pepinito

A writer of a subset of Python's pickle serialisation protocol in Clojure.

[![Build Status](https://travis-ci.org/ulises/pepinito.png?branch=master)](https://travis-ci.org/ulises/pepinito)

## Include it

Include ``[pepinito "0.0.3-SNAPSHOT"]`` in your project.clj if you're
using leiningen. Alternatively, add
```XML
<dependency>
  <groupId>pepinito</groupId>
  <artifactId>pepinito</artifactId>
  <version>0.0.3-SNAPSHOT</version>
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

# License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
