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

(def long-string (reduce str (repeat 257 \a)))

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
    (string-from-pickle long-string) => (slurp "test/resources/257a.py.pickle")))

(facts "about pickling tuples of 1 element"
  (fact "pickle tuple with int"
    (string-from-pickle [(int 1)]) => (slurp "test/resources/int.tuple1.py.pickle"))
  (fact "pickle tuple with double"
    (string-from-pickle [(double 1)]) => (slurp "test/resources/double.tuple1.py.pickle"))
  (fact "pickle tuple with float"
    (string-from-pickle [(float 1)]) => (slurp "test/resources/double.tuple1.py.pickle"))
  (fact "pickle tuple with long"
    (string-from-pickle [(long 1)]) => (slurp "test/resources/double.tuple1.py.pickle"))
  (fact "pickle tuple with boolean"
    (string-from-pickle [true]) => (slurp "test/resources/boolean-true.tuple1.py.pickle"))
  (fact "pickle tuple with short string"
    (string-from-pickle ["hello"]) => (slurp "test/resources/short-string.tuple1.py.pickle"))
  (fact "pickle tuple with long string"
    (string-from-pickle [long-string]) => (slurp "test/resources/long-string.tuple1.py.pickle"))
  (fact "pickle tuple with tuple with int"
    (string-from-pickle [[(int 1)]]) => (slurp "test/resources/int-tuple.tuple1.py.pickle")))

;;; necessarily shorter -and less comprehensive- than tuple-1 tests
;;; due to the combinatorial explosion of possibilities
(facts "about pickling tuples of 2 elements"
  (fact "pickle tuple with ints"
    (string-from-pickle [(int 1) (int 2)]) => (slurp "test/resources/int.tuple2.py.pickle"))
  (fact "pickle tuple with int-float"
    (string-from-pickle [(int 1) (float 2)]) => (slurp "test/resources/int-float.tuple2.py.pickle"))
  (fact "pickle tuple with int and short string"
    (string-from-pickle [(int 1) "hello"]) => (slurp "test/resources/int-short-string.tuple2.py.pickle"))
  (fact "pickle tuple with long string and single-int tuple"
    (string-from-pickle [long-string [(int 1)]]) => (slurp "test/resources/long-string-int-tuple.tuple2.py.pickle"))
  (fact "pickle tuple with long string and an int-int tuple"
    (string-from-pickle [long-string [(int 1) (int 2)]]) => (slurp "test/resources/long-string-int-tuple2.tuple2.py.pickle")))

(facts "about pickling tuples of 3 elements"
  (fact "pickle tuple with ints"
    (string-from-pickle [(int 1) (int 2) (int 3)]) => (slurp "test/resources/int.tuple3.py.pickle"))
  (fact "pickle tuple with int, short string and long"
    (string-from-pickle [(int 1) "hello" (long 3)]) => (slurp "test/resources/int-short-string-long.tuple3.py.pickle"))
  (fact "pickle tuple with long string, int-tuple-1, boolean-float-long-tuple3"
    (string-from-pickle [long-string [(int 1)] [true (float 2) (long 3)]]) => (slurp "test/resources/long-string-ituple1-bfltuple3.tuple3.py.pickle"))
  ;; python's pickled output doesn't contain multiple copies of the
  ;; same object. Although pepinito doesn't do this yet, python will
  ;; happily read pepinito's non-optimized data.
  (comment
    (fact "pickle tuple with long strings only"
      (string-from-pickle [long-string long-string long-string]) => (slurp "test/resources/long-string.tuple3.py.pickle"))))

(facts "about pickling tuples of N elements"
  (fact "pickle tuple with 10 ints"
    (string-from-pickle [(int 1) (int 2) (int 3) (int 4) (int 5)
                         (int 6) (int 7) (int 8) (int 9) (int 0)]) =>
                         (slurp "test/resources/int.tuple10.py.pickle"))
  (fact "pickle tuple with int, long-string, tuple-1, and float"
    (string-from-pickle [(int 1) long-string [(double 2)] (float 3)]) =>
    (slurp "test/resources/int-longstring-tuple1-float.py.pickle"))
  ;; python's pickled output doesn't contain multiple copies of the
  ;; same object. Although pepinito doesn't do this yet, python will
  ;; happily read pepinito's non-optimized data.
  (comment
    (fact "pickle tuple with int-tuple-4 short-string-tuple-2 long-string-tuple-7 and float-tuple-5"
      (string-from-pickle [[(int 1) (int 2) (int 3) (int 4)]
                           ["hello" "world"]
                           [long-string long-string long-string long-string
                            long-string long-string long-string]
                           [(float 1) (float 2) (float 3) (float 4) (float 5)]]) =>
                           (slurp "test/resources/complex.tuple4.py.pickle"))))
