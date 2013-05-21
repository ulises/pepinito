(ns pepinito.core
  (:import java.io.DataOutputStream)
  (:require [clojure.tools.logging :as log]))

(defn- asc [c]
  (let [bytes (.getBytes (str c))]
    (first bytes)))

(def MARK            (asc \())
(def STOP            0x2e)
(def POP             \0)
(def POP-MARK        \1)
(def DUP             \2)
(def FLOAT           \F)
(def INT             \I)
(def BININT          (asc \J))
(def BININT1         (asc \K))
(def LONG            \L)
(def BININT2         \M)
(def NONE            \N)
(def PERSID          \P)
(def BINPERSID       \Q)
(def REDUCE          \R)
(def STRING          \S)
(def BINSTRING       (asc \T))
(def SHORT-BINSTRING (asc \U))
(def UNICODE         \V)
(def BINUNICODE      \X)
(def APPEND          \a)
(def BUILD           \b)
(def GLOBAL          \c)
(def DICT            \d)
(def EMPTY-DICT      \})
(def APPENDS         \e)
(def GET             \g)
(def BINGET          \h)
(def INST            \i)
(def LONG-BINGET     \j)
(def LIST            \l)
(def EMPTY-LIST      \])
(def OBJ             \o)
(def PUT             \p)
(def BINPUT          (asc \q))
(def LONG-BINPUT     (asc \r))
(def SETITEM         \s)
(def TUPLE           (asc \t))
(def EMPTY-TUPLE     \))
(def SETITEMS        \u)
(def BINFLOAT        (asc \G))
(def  VERSION        0x02)
(def  PROTO          0x80)
(def  NEWOBJ         0x81)
(def  EXT1           0x82)
(def  EXT2           0x83)
(def  EXT4           0x84)
(def  TUPLE1         0x85)
(def  TUPLE2         0x86)
(def  TUPLE3         0x87)
(def  NEWTRUE        0x88)
(def  NEWFALSE       0x89)
(def  LONG1          0x8a)
(def  LONG4          0x8b)

(declare dump*)

(defn- write-byte [^DataOutputStream os ^Character c]
  (.writeByte os c))

(defn- write-int [^DataOutputStream os ^Integer i]
  (write-byte os (bit-and 0xff i))
  (write-byte os (bit-and 0xff (bit-shift-right i 8)))
  (write-byte os (bit-and 0xff (bit-shift-right i 16)))
  (write-byte os (bit-and 0xff (bit-shift-right i 24))))

(defn- write-double [^DataOutputStream out ^Double d]
  (.writeDouble out d))

(defn- write-idx [^DataOutputStream out ^Integer idx]
  (if (< idx 256)
    (do (write-byte out BINPUT)
        (write-byte out idx))
    (do (write-byte out LONG-BINPUT)
        (write-int out idx)))
  (inc idx))

(defn- maybe-write-idx [^DataOutputStream out ^Integer idx putvar]
  (if putvar (write-idx out idx) idx))

(defn dump [o ^DataOutputStream out]
  (write-byte out PROTO)
  (write-byte out VERSION)
  (dump* o out 0 :putvar)
  (write-byte out STOP))

(defmulti dump* "Pickles objects" (fn [obj out idx putvar] (class obj)))

(defmethod dump* Long
  [^Long l ^DataOutputStream out ^Integer _idx _putvar]
  (dump* (double l) out _idx _putvar))

(defmethod dump* Float
  [^Float l ^DataOutputStream out ^Integer _idx _putvar]
  (dump* (double l) out _idx _putvar))

(defmethod dump* Double
  [^Double d ^DataOutputStream out ^Integer idx _putvar]
  (write-byte out BINFLOAT)
  (write-double out d)
  idx)

(defmethod dump* Integer
  [^Integer i ^DataOutputStream out ^Integer idx _putvar]
  (if (<= 0 i)
    (condp >= i
      0xff (do (write-byte out BININT1)
               (write-byte out i))
      0xffff (do (write-byte out BININT2)
                 (write-byte out (bit-shift-right i 8)))
      0xffffffff (do (write-byte out BININT)
                     (write-int out i))
      (throw (IllegalArgumentException. (str "can't pickle " i))))
    (condp = (bit-shift-right i 31)
      0 (do (write-byte out BININT)
            (write-int out i))
      -1 (do (write-byte out BININT)
             (write-int out i))
      (throw (IllegalArgumentException. (str "can't pickle " i)))))
  idx)

(defmethod dump* Boolean
  [^Boolean b ^DataOutputStream out ^Integer idx _putvar]
  (if b
    (write-byte out NEWTRUE)
    (write-byte out NEWFALSE))
  idx)

(defn- encode-short-string [^String s ^DataOutputStream out ^Integer idx putvar]
  (write-byte out SHORT-BINSTRING)
  (write-byte out (count s))
  (doseq [b (map asc s)]
    (write-byte out b))
  (maybe-write-idx out idx putvar))

(defn- encode-long-string [^String s ^DataOutputStream out ^Integer idx putvar]
  (write-byte out BINSTRING)
  (write-int out (count s))
  (doseq [b (map asc s)]
    (write-byte out b))
  (maybe-write-idx out idx putvar))

(defmethod dump* String
  [^String s ^DataOutputStream out ^Integer idx putvar]
  (if (< (count s) 256)
    (encode-short-string s out idx putvar)
    (encode-long-string s out idx putvar)))

(defn- encode-tuple-1 [^clojure.lang.PersistentVector [item] ^DataOutputStream out ^Integer idx putvar]
  (let [next-idx (dump* item out idx putvar)]
    (write-byte out TUPLE1)
    (maybe-write-idx out next-idx putvar)))

(defn- encode-tuple-2 [^clojure.lang.PersistentVector [item1 item2] ^DataOutputStream out ^Integer idx putvar]
  (let [next-idx1 (dump* item1 out idx putvar)
        next-idx2 (dump* item2 out next-idx1 putvar)]
    (write-byte out TUPLE2)
    (maybe-write-idx out next-idx2 putvar)))

(defn- encode-tuple-3 [^clojure.lang.PersistentVector [item1 item2 item3]
                       ^DataOutputStream out ^Integer idx putvar]
  (let [next-idx1 (dump* item1 out idx putvar)
        next-idx2 (dump* item2 out next-idx1 putvar)
        next-idx3 (dump* item3 out next-idx2 putvar)]
    (write-byte out TUPLE3)
    (maybe-write-idx out next-idx3 putvar)))

(defn- encode-tuple-n [^clojure.lang.PersistentVector tuple
                       ^DataOutputStream out ^Integer idx putvar]
  (write-byte out MARK)
  (let [new-idx (reduce (fn [old-idx item]
                          (dump* item out old-idx putvar)) idx
                        tuple)]
    (write-byte out TUPLE)
  (maybe-write-idx out new-idx putvar)))

(defmethod dump* clojure.lang.PersistentVector
  [^clojure.lang.PersistentVector tuple ^DataOutputStream out ^Integer idx putvar]
  (condp #(= %1 (count %2)) tuple
    1 (encode-tuple-1 tuple out idx putvar)
    2 (encode-tuple-2 tuple out idx putvar)
    3 (encode-tuple-3 tuple out idx putvar)
    (encode-tuple-n tuple out idx putvar)))
