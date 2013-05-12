(ns pepinito.core
  (:import java.io.DataOutputStream)
  (:require [clojure.tools.logging :as log]))

(defn- asc [c]
  (let [bytes (.getBytes (str c))]
    (first bytes)))

(def MARK            \()
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
(def TUPLE           \t)
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

(defn dump [o ^DataOutputStream out]
  (write-byte out PROTO)
  (write-byte out VERSION)
  (dump* o out 0)
  (write-byte out STOP))

(defmulti dump* "Pickles objects" (fn [obj out idx] (class obj)))

(defmethod dump* Long
  [^Long l ^DataOutputStream out ^Integer _idx]
  (dump* (double l) out _idx))

(defmethod dump* Float
  [^Float l ^DataOutputStream out ^Integer _idx]
  (dump* (double l) out _idx))

(defmethod dump* Double
  [^Double d ^DataOutputStream out ^Integer _idx]
  (write-byte out BINFLOAT)
  (write-double out d))

(defmethod dump* Integer
  [^Integer i ^DataOutputStream out ^Integer _idx]
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
      (throw (IllegalArgumentException. (str "can't pickle " i))))))

(defmethod dump* Boolean
  [^Boolean b ^DataOutputStream out ^Integer _idx]
  (if b
    (write-byte out NEWTRUE)
    (write-byte out NEWFALSE)))

(defn- encode-short-string [^String s ^DataOutputStream out ^Integer idx]
  (write-byte out SHORT-BINSTRING)
  (write-byte out (count s))
  (doseq [b (map asc s)]
    (write-byte out b))
  (write-idx out idx))

(defn- encode-long-string [^String s ^DataOutputStream out ^Integer idx]
  (write-byte out BINSTRING)
  (write-int out (count s))
  (doseq [b (map asc s)]
    (write-byte out b))
  (write-idx out idx))

(defmethod dump* String
  [^String s ^DataOutputStream out ^Integer idx]
  (if (< (count s) 256)
    (encode-short-string s out idx)
    (encode-long-string s out idx)))
