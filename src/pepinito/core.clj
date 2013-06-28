(ns pepinito.core
  (:import (java.io DataOutputStream ByteArrayOutputStream)
           (java.net Socket)
           (java.nio ByteBuffer)))

(defn- asc [c]
  (let [bytes (.getBytes (str c))]
    (first bytes)))

(def MARK            (asc \())
(def STOP            0x2e)
(def BININT          (asc \J))
(def BININT1         (asc \K))
(def BININT2         (asc \M))
(def BINSTRING       (asc \T))
(def SHORT-BINSTRING (asc \U))
(def BINPUT          (asc \q))
(def LONG-BINPUT     (asc \r))
(def TUPLE           (asc \t))
(def EMPTY-TUPLE     (asc \)))
(def EMPTY-LIST      (asc \]))
(def BINFLOAT        (asc \G))
(def APPENDS         (asc \e))
(def VERSION         0x02)
(def PROTO           0x80)
(def NEWOBJ          0x81)
(def EXT1            0x82)
(def EXT2            0x83)
(def EXT4            0x84)
(def TUPLE1          0x85)
(def TUPLE2          0x86)
(def TUPLE3          0x87)
(def NEWTRUE         0x88)
(def NEWFALSE        0x89)
(def LONG1           0x8a)
(def LONG4           0x8b)

(declare dump* write-byte)

(defn dump
  "Pickle object and write into the output stream."
  [^DataOutputStream out obj]
  (write-byte out PROTO)
  (write-byte out VERSION)
  (dump* out obj 0 :putvar)
  (write-byte out STOP))

(defn dumps
  "Pickle object and return the result as a byte array"
  [obj]
  (let [bo (ByteArrayOutputStream.)
        out (DataOutputStream. bo)]
    (dump out obj)
    (.flush ^ByteArrayOutputStream bo)
    (.toByteArray bo)))

(defn- write-byte
  [^DataOutputStream os ^Character c]
  (.writeByte os c))

(defn- write-int
  [^DataOutputStream os ^Integer i]
  (write-byte os (bit-and 0xff i))
  (write-byte os (bit-and 0xff (bit-shift-right i 8)))
  (write-byte os (bit-and 0xff (bit-shift-right i 16)))
  (write-byte os (bit-and 0xff (bit-shift-right i 24))))

(defn- write-double
  [^DataOutputStream out ^Double d]
  (.writeDouble out d))

(defn- write-idx
  [^DataOutputStream out ^Integer idx]
  (if (< idx 256)
    (do (write-byte out BINPUT)
        (write-byte out idx))
    (do (write-byte out LONG-BINPUT)
        (write-int out idx)))
  (inc idx))

(defn- maybe-write-idx
  [^DataOutputStream out ^Integer idx putvar]
  (if putvar (write-idx out idx) idx))

(defmulti dump* "Pickles objects" (fn [out obj idx putvar] (class obj)))

(defmethod dump* Long
  [^DataOutputStream out ^Long l ^Integer _idx _putvar]
  (dump* out (double l) _idx _putvar))

(defmethod dump* Float
  [^DataOutputStream out ^Float l ^Integer _idx _putvar]
  (dump* out (double l) _idx _putvar))

(defmethod dump* Double
  [^DataOutputStream out ^Double d ^Integer idx _putvar]
  (write-byte out BINFLOAT)
  (write-double out d)
  idx)

(defmethod dump* Integer
  [^DataOutputStream out ^Integer i ^Integer idx _putvar]
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
  [^DataOutputStream out ^Boolean b ^Integer idx _putvar]
  (if b
    (write-byte out NEWTRUE)
    (write-byte out NEWFALSE))
  idx)

(defn- encode-short-string
  [^DataOutputStream out ^String s ^Integer idx putvar]
  (write-byte out SHORT-BINSTRING)
  (write-byte out (count s))
  (doseq [b (map asc s)]
    (write-byte out b))
  (maybe-write-idx out idx putvar))

(defn- encode-long-string
  [^DataOutputStream out ^String s ^Integer idx putvar]
  (write-byte out BINSTRING)
  (write-int out (count s))
  (doseq [b (map asc s)]
    (write-byte out b))
  (maybe-write-idx out idx putvar))

(defmethod dump* String
  [^DataOutputStream out ^String s ^Integer idx putvar]
  (if (< (count s) 256)
    (encode-short-string out s idx putvar)
    (encode-long-string out s idx putvar)))

(defn- encode-tuple-1
  [^DataOutputStream out ^clojure.lang.PersistentVector [item]
   ^Integer idx putvar]
  (let [next-idx (dump* out item idx putvar)]
    (write-byte out TUPLE1)
    (maybe-write-idx out next-idx putvar)))

(defn- encode-tuple-2
  [^DataOutputStream out ^clojure.lang.PersistentVector [item1 item2]
   ^Integer idx putvar]
  (let [next-idx1 (dump* out item1 idx putvar)
        next-idx2 (dump* out item2 next-idx1 putvar)]
    (write-byte out TUPLE2)
    (maybe-write-idx out next-idx2 putvar)))

(defn- encode-tuple-3
  [^DataOutputStream out ^clojure.lang.PersistentVector [item1 item2 item3]
   ^Integer idx putvar]
  (let [next-idx1 (dump* out item1 idx putvar)
        next-idx2 (dump* out item2 next-idx1 putvar)
        next-idx3 (dump* out item3 next-idx2 putvar)]
    (write-byte out TUPLE3)
    (maybe-write-idx out next-idx3 putvar)))

(defn- encode-tuple-n
  [^DataOutputStream out ^clojure.lang.PersistentVector tuple
   ^Integer idx putvar]
  (write-byte out MARK)
  (let [new-idx (reduce (fn [old-idx item]
                          (dump* out item old-idx putvar)) idx
                        tuple)]
    (write-byte out TUPLE)
  (maybe-write-idx out new-idx putvar)))

(defmethod dump* clojure.lang.PersistentVector
  [^DataOutputStream out ^clojure.lang.PersistentVector tuple ^Integer idx putvar]
  (condp #(= %1 (count %2)) tuple
    1 (encode-tuple-1 out tuple idx putvar)
    2 (encode-tuple-2 out tuple idx putvar)
    3 (encode-tuple-3 out tuple idx putvar)
    (encode-tuple-n out tuple idx putvar)))

(defn- encode-sequential
  [^DataOutputStream out ^clojure.lang.Sequential coll ^Integer idx putvar]
  (write-byte out EMPTY-LIST)
  (let [next-idx (maybe-write-idx out idx putvar)]
    (write-byte out MARK)
    (let [new-idx (reduce (fn [old-idx item]
                            (dump* out item old-idx putvar)) next-idx
                            coll)]
      (write-byte out APPENDS))))

(defmethod dump* clojure.lang.PersistentList
  [^DataOutputStream out ^clojure.lang.PersistentList coll ^Integer idx putvar]
  (encode-sequential out coll idx putvar))


(defn send-pickled [o & {:keys [host port]}]
  (let [c (Socket. (or host "192.168.0.18") (or port 2004))
        out (DataOutputStream. (.getOutputStream c))
        pickled-str (dumps o)
        header (.array (.putInt (ByteBuffer/allocate 4) (count pickled-str)))]
    (.write out header)
    (.write out pickled-str)
    (.close out)))
