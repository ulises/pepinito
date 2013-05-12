(ns pepinito.t-core
  (:use midje.sweet)
  (:use [pepinito.core])
  (:import [java.io DataOutputStream ByteArrayOutputStream
            OutputStream]
           [java.lang String]))

(defn- os->str [^ByteArrayOutputStream os]
  (String. (.toByteArray os)))

(defn- string-from-pickle [o]
  (let [baos (ByteArrayOutputStream.)
        os (DataOutputStream. baos)]
    (dump o os)
    (os->str baos)))

(facts "about pickling ints"
  (fact "pickle 1"
    (string-from-pickle (int 1)) => (slurp "test/resources/1.py.pickle"))
  (fact "pickle 5"
    (string-from-pickle (int 5)) => (slurp "test/resources/5.py.pickle"))
  (fact "pickle 0"
    (string-from-pickle (int 0)) => (slurp "test/resources/0.py.pickle"))
  (fact "pickle -1"
    (string-from-pickle (int -1)) => (slurp "test/resources/neg.1.py.pickle"))
  (fact "pickle -5"
    (string-from-pickle (int -5)) => (slurp "test/resources/neg.5.py.pickle"))
  (fact "pickle Integer/MAX_VALUE"
    (string-from-pickle Integer/MAX_VALUE) => (slurp "test/resources/max_int.py.pickle"))
  (fact "pickle Integer/MIN_VALUE"
    (string-from-pickle Integer/MIN_VALUE) => (slurp "test/resources/min_int.py.pickle")))

(facts "about pickling doubles"
  (fact "pickle (double 1)"
   (string-from-pickle (double 1)) => (slurp "test/resources/1d.py.pickle"))
  (fact "pickle (double 5)"(string-from-pickle (double 5)) => (slurp "test/resources/5d.py.pickle"))
  (fact "pickle (double 0)"
    (string-from-pickle (double 0)) => (slurp "test/resources/0d.py.pickle"))
  (fact "pickle (double -1)"
    (string-from-pickle (double -1)) => (slurp "test/resources/neg.1d.py.pickle"))
  (fact "pickle (double -5)"
    (string-from-pickle (double -5)) => (slurp "test/resources/neg.5d.py.pickle"))
  (fact "pickle Double/MAX_VALUE"
    (string-from-pickle Double/MAX_VALUE) => (slurp "test/resources/max_double.py.pickle"))
  (fact "pickle Double/MIN_VALUE"
    (string-from-pickle Double/MIN_VALUE) => (slurp "test/resources/min_double.py.pickle")))

(facts "about pickling floats"
  (fact "pickle (float 1)"
   (string-from-pickle (float 1)) => (slurp "test/resources/1d.py.pickle"))
  (fact "pickle (float 5)"(string-from-pickle (float 5)) => (slurp "test/resources/5d.py.pickle"))
  (fact "pickle (float 0)"
    (string-from-pickle (float 0)) => (slurp "test/resources/0d.py.pickle"))
  (fact "pickle (float -1)"
    (string-from-pickle (float -1)) => (slurp "test/resources/neg.1d.py.pickle"))
  (fact "pickle (float -5)"
    (string-from-pickle (float -5)) => (slurp "test/resources/neg.5d.py.pickle"))
  ;;; FIXME: pickle Float extreme values
  ;; (fact "pickle Float/MAX_VALUE"
  ;;   (string-from-pickle Float/MAX_VALUE) => (slurp "test/resources/max_float.py.pickle"))
  ;; (fact "pickle Float/MIN_VALUE"
  ;;   (string-from-pickle Float/MIN_VALUE) => (slurp "test/resources/min_float.py.pickle"))
  )

(facts "about pickling longs"
  (fact "pickle (long 1)"
   (string-from-pickle (long 1)) => (slurp "test/resources/1d.py.pickle"))
  (fact "pickle (long 5)"(string-from-pickle (long 5)) => (slurp "test/resources/5d.py.pickle"))
  (fact "pickle (long 0)"
    (string-from-pickle (long 0)) => (slurp "test/resources/0d.py.pickle"))
  (fact "pickle (long -1)"
    (string-from-pickle (long -1)) => (slurp "test/resources/neg.1d.py.pickle"))
  (fact "pickle (long -5)"
    (string-from-pickle (long -5)) => (slurp "test/resources/neg.5d.py.pickle"))
  (fact "pickle Long/MAX_VALUE"
    (string-from-pickle Long/MAX_VALUE) => (slurp "test/resources/max_long.py.pickle"))
  (fact "pickle Long/MIN_VALUE"
    (string-from-pickle Long/MIN_VALUE) => (slurp "test/resources/min_long.py.pickle")))

(facts "about pickling booleans"
  (fact "pickle true"
    (string-from-pickle true) => (slurp "test/resources/true.py.pickle"))
  (fact "pickle false"
    (string-from-pickle false) => (slurp "test/resources/false.py.pickle")))

(facts "about pickling strings"
  (fact "pickle short string (< 256)"
    (string-from-pickle "hello world") => (slurp "test/resources/hello.py.pickle"))

  (fact "pickle long string (> 256)"
    (let [long-string (reduce str (repeat 257 \a))]
      (string-from-pickle long-string) => (slurp "test/resources/257a.py.pickle"))))
