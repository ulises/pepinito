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
(def BINSTRING       \T)
(def SHORT-BINSTRING \U)
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
(def BINPUT          \q)
(def LONG-BINPUT     \r)
(def SETITEM         \s)
(def TUPLE           \t)
(def EMPTY-TUPLE     \))
(def SETITEMS        \u)
(def BINFLOAT        \G)
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

(defn write-byte [^DataOutputStream os ^Character c]
  (.writeByte os c))

(defn write-int [^DataOutputStream os ^Integer i]
  (write-byte os (bit-and 0xff i))
  (write-byte os (bit-and 0xff (bit-shift-right i 8)))
  (write-byte os (bit-and 0xff (bit-shift-right i 16)))
  (write-byte os (bit-and 0xff (bit-shift-right i 24))))

(defn dump [o ^DataOutputStream out]
  (write-byte out PROTO)
  (write-byte out VERSION)
  (dump* o out)
  (write-byte out STOP))


(defmulti dump* "Pickles objects" (fn [o _] (class o)))

(defmethod dump* Integer
  [i ^DataOutputStream out]
  (dump* (long i) out))

(defmethod dump* Long
  [i ^DataOutputStream out]
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
