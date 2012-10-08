;  Copyright 2012 DynamoBI
;
;  Licensed under the Apache License, Version 2.0 (the "License");
;  you may not use this file except in compliance with the License.
;  You may obtain a copy of the License at

;    http://www.apache.org/licenses/LICENSE-2.0

;  Unless required by applicable law or agreed to in writing, software
;  distributed under the License is distributed on an "AS IS" BASIS,
;  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;  See the License for the specific language governing permissions and
;  limitations under the License.
;
;  Author: Kevin Secretan

(ns com.dendron.cuber.core
  (:gen-class)
  (:require [com.dendron.cuber.cube :as cube]
            [clojure.set])
  (:use [com.dendron.cuber storage]
        [clojure.pprint]
        [clojure.repl]
        [clojure.java.io]))

(defn -main [& args]
  (println "Don't run this directly yet, use REPL."))

(comment
  "This is for demo purposes and you want to run these if you're testing/developing in a live repl"

(do (use 'com.dendron.cuber.core) (ns com.dendron.cuber.core))

(binding [*noisy?* true] (time (construct-cube "testwith22k" "22kdata.csv" "22kdata.csv")))

(defn f [x] (str "/dev/shm/" x))
(time (apply construct-cube "testbig" (map f '(On_Time_On_Time_Performance_2001_3.csv On_Time_On_Time_Performance_2001_4.csv))))

; works with named keys

(query-cube "testwith22k" ["2010-08-10" "DL" "DCA"])

; works with positional/coordinate-keys

(query-cube "testbig" ^:numeric [0 0 0 0 0 0 0 0 0 0])

)

; Defaulted to free tier, which is 100 MB of storage and 5 writes/s, 10 reads/s
(def data-throughput {:read 10 :write 5})
(def ^:dynamic *construct-keys?* true)

(defn construct-cube [name & csvs]
  (let [tbl (dyndb-table name)
        tbl-keys (dyndb-key-table name)]
    (try (create-table tbl :dyndb {:hash-key d-dyn-fam :throughput data-throughput})
      (println "Waiting 45 secs for table creation...")
      (Thread/sleep 45000)
      (println "Table created.")
      (catch Exception e (println "Tables already exist.")))
    (if *construct-keys?* (println "Creating keys..."))
    (let [[origin N counts] (if *construct-keys?*
                              (cube/create-key-int-map csvs tbl-keys)
                              (get-origin-N tbl-keys))]
      (println "Prepped for cube with origin" origin "and size" N)
      (println "Inserting data...")
      (dorun (map #(cube/insert-row-by-row tbl tbl-keys origin %1 N) csvs))
      (println "Summing borders...")
      (cube/sum-borders tbl tbl-keys origin N counts)
      (println "Finished.")
      (if cube/collect-stats? (println @cube/statistics)))))

(defn query-cube [name namedcell]
  (let [tbl (dyndb-table name)
        tbl-keys (dyndb-key-table name)
        cell (if (:numeric (meta namedcell))
               namedcell
               (cube/names2nums tbl-keys namedcell))
        N (get-N tbl-keys)]
    (cube/query tbl tbl-keys cell N :sum)))

