(ns gol
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(import java.lang.Thread)

(defn apply-rules [state n] 
  (case state
    :alive (if (#{2 3} n) :alive :dead)
    :dead  (if (= 3 n)   :alive :dead)))

(def patterns
  (if-let [res (io/resource "patterns.edn")]
    (edn/read-string (slurp res))
    (throw (RuntimeException. "Could not find patterns.edn in classpath"))))

(defn idx [{:keys [width height]} x y]
  (let [x (mod (+ width x) width) y (mod (+ height y) height)]
    (+ x (* y width))))

(defn assoc-xy [board x y state]
  (let [i (idx board x y)]
    (update board :cells assoc i state)))

(defn get-xy [board x y]
  (let [i (idx board x y)]
    (get-in board [:cells i])))

(defn apply-pattern [board pattern x y]
  (reduce (fn [b [x0 y0]] (assoc-xy b (+ x0 x) (+ y0 y) :alive)) board pattern))

(defn format-board [board]
  (partition (:width board) (map (fn [s] ({:alive "*" :dead "."} s)) (:cells board))))

(defn print-board [board]
  (doseq [row (format-board board)] (print (string/join "" row) "\n")))
 
(defn inc-xy [counts x y]
  (assoc-xy counts x y (+ 1 (get-xy counts x y))))

(defn inc-neighbours [counts x y w h]
  (->
    {:cells counts :width w :height h}
    (inc-xy (- x 1) (- y 1))
    (inc-xy    x    (- y 1))
    (inc-xy (+ x 1) (- y 1))
    (inc-xy (- x 1)    y)
    (inc-xy (+ x 1)    y)
    (inc-xy (- x 1) (+ y 1))
    (inc-xy    x    (+ y 1))
    (inc-xy (+ x 1) (+ y 1))
    :cells))

(defn count-at 
  ([counts x y board]
    (if (= :alive (get-xy board x y)) (inc-neighbours counts x y (:width board) (:height board)) counts)) 
  ([counts i board]
    (count-at counts (mod i (:width board)) (quot i (:width board)) board)))

(defn count-neighbours [board]
  (let [total (* (:width board) (:height board))]
    (reduce (fn [cs i] (count-at cs i board)) (vec (repeat total 0)) (range total))))

(defn next-gen [board]
  (->>
    (map vector (:cells board) (count-neighbours board))
    (map (fn [[s n]] (apply-rules s n)))
    (vec)
    (assoc board :cells)))

(defn init []
  (->
    {:width 20 :height 10 :cells (vec (repeat 200 :dead))}
    (apply-pattern (:glider patterns) 0 0)))

(defn run []
  (loop [board (init)]
    (print "\u001b[2J\u001b[H")
    (print-board board)
    (flush)
    (Thread/sleep 500)
    (recur (next-gen board))))

(defn -main [& _]
  (run))
