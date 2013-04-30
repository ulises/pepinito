(ns pepinito.t-core
  (:use midje.sweet)
  (:use [pepinito.core])
  (:import [java.io DataOutputStream ByteArrayOutputStream
            OutputStream]
           [java.lang String]))

(defn- os->str [^ByteArrayOutputStream os]
  (String. (.toByteArray os)))

(defn- bytes-from-pickle [o]
  (let [baos (ByteArrayOutputStream.)
        os (DataOutputStream. baos)]
    (dump o os)
    baos))

(facts "about pickling objects"
  (fact "pickling ints"
    (os->str (bytes-from-pickle 1)) => (slurp "test/resources/1.py.pickle")
    (os->str (bytes-from-pickle 5)) => (slurp "test/resources/5.py.pickle")
    (os->str (bytes-from-pickle 0)) => (slurp "test/resources/0.py.pickle")
    (os->str (bytes-from-pickle -1)) => (slurp "test/resources/neg.1.py.pickle")
    (os->str (bytes-from-pickle -5)) => (slurp "test/resources/neg.5.py.pickle")
    (os->str (bytes-from-pickle Integer/MAX_VALUE)) => (slurp "test/resources/max_int.py.pickle")
    (os->str (bytes-from-pickle Integer/MIN_VALUE)) => (slurp "test/resources/min_int.py.pickle")))
