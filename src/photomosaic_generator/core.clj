(ns photomosaic-generator.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [photomosaic-generator.image :as img])
  (:import (javax.imageio ImageIO)
           (java.awt Color)))

(defn pixel->rgb [i]
  (let [color (Color. i)]
    [(.getRed color)
     (.getGreen color)
     (.getBlue color)]))

(defn rgb-seq [image]
  (let [[width height] (img/dims image)
        coords (for [w (range width)
                     h (range height)]
                 [w h])]
    (map (fn [[w h]]
           (pixel->rgb (.getRGB image w h)))
         coords)))

(defn avg-rgb [rgb-seq]
  (->> (reduce (fn [sum [r g b]]
                 (mapv + sum [r g b]))
               [0 0 0]
               rgb-seq)
       (mapv #(int (/ % (count rgb-seq))))))

;; TODO: If the image does not divide evenly, there will
;; be leftover pixels not accounted for on the right and
;; the bottom. My current thinking is to extend the last
;; column or bottom row to absorb the remaining pixels.
(defn block-seq [rows cols max-width max-height]
  (let [width (int (/ max-width rows))
        height (int (/ max-height cols))]
    (for [x (take rows (range 0 max-width width))
          y (take cols (range 0 max-height height))]
      {:x x
       :y y
       :width width
       :height height})))

;; bottom row is every cols element in the sequence
;; rightmost column are the last 'rows' elements in the sequence
(defn block-seq [rows cols max-width max-height]
  (let [width (/ max-width rows)
        height (/ max-height cols)]
    (for [x (range 0 max-width width)
          y (range 0 max-height height)]
      {:x x
       :y y
       :width width
       :height height})))

(defn- ratios->ints [m]
  (into {} (map (fn [[k v]]
                  (if (ratio? v) [k (int v)] [k v]))
                m)))

(defn fit-blocks [blocks rows cols max-width max-height]
  (let [width (/ max-width rows)
        height (/ max-height cols)
        rightmost-col (- (* width rows) width)
        bottom-row (- (* height cols) height)]
    (->> blocks
         (map (fn [{:keys [x width] :as block}]
                (if (= x rightmost-col)
                  (assoc block :width (+ width (mod max-width rows)))
                  block)))
         (map (fn [{:keys [y height] :as block}]
                (if (= y bottom-row)
                  (assoc block :height (+ height (mod max-height cols)))
                  block)))
         #_(map ratios->ints))))

(comment

  "Next Steps are to actually start choosing pictures:"
  "1) Choose a photo to use as the mosaic"
  "2) Need to start downloading photos that I can resize and take their"
  "   RGB values of"
  "3) Alternatively, do a test by combining a solid color on a photo using an opacity"

  (def img1 (ImageIO/read (io/as-file (io/resource "cells.jpg"))))

  (def img2 (ImageIO/read (io/as-file (io/resource "dog.jpg"))))

  (def cat-img (ImageIO/read (io/as-file (io/resource "cat.jpg"))))

  (def monkey-img (ImageIO/read (io/as-file (io/resource "monkey.jpg"))))

  (def obama (ImageIO/read (io/as-file (io/resource "obama.jpg"))))

  (def flowers (ImageIO/read (io/as-file (io/resource "flowers.jpg"))))

  )
