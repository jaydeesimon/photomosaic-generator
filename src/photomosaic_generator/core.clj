(ns photomosaic-generator.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:import (javax.imageio ImageIO)
           (java.awt Color AlphaComposite Image)
           (java.awt.image BufferedImage)))

(defn- pixel->rgb [i]
  (let [color (Color. i)]
    [(.getRed color)
     (.getGreen color)
     (.getBlue color)]))

(defn- rgb-seq [image]
  (let [coords (for [w (range (.getWidth image))
                     h (range (.getHeight image))]
                 [w h])]
    (map (fn [[w h]]
           (pixel->rgb (.getRGB image w h)))
         coords)))

(defn- avg-rgb [rgb-seq]
  (->> (reduce (fn [sum [r g b]]
                 (mapv + sum [r g b]))
               [0 0 0]
               rgb-seq)
       (mapv #(int (/ % (count rgb-seq))))))

(defn place-image-on-image [image-bottom image-top x y]
  (do (doto (.createGraphics image-bottom)
            (.setComposite (AlphaComposite/getInstance AlphaComposite/SRC_OVER 1.0))
            (.drawImage image-top x y nil)
            (.dispose))
      image-bottom))

(defn resize-image [image w h]
  (let [scaled-instance (.getScaledInstance image w h Image/SCALE_SMOOTH)
        resized-image (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics resized-image)]
    (do (.drawImage g scaled-instance 0 0 nil)
        (.dispose g)
        resized-image)))

(defn subimage [image x y w h]
  (.getSubimage image x y w h))

(defn write-image [image filename]
  (ImageIO/write image "png" (io/as-file filename)))

(defn block-seq [num-blocks max-width max-height]
  (let [width (int (/ max-width num-blocks))
        height (int (/ max-height num-blocks))]
    (for [x (take num-blocks (range 0 max-width width))
          y (take num-blocks (range 0 max-height height))]
      {:x      x
       :y      y
       :width  width
       :height height})))

(defn- block-rgb-seq [image num-blocks]
  (let [max-width (.getWidth image)
        max-height (.getHeight image)]
    (map (fn [{:keys [x y width height] :as block-info}]
           (assoc block-info :rgb (avg-rgb (rgb-seq (subimage image x y width height)))))
         (block-seq num-blocks max-width max-height))))


(comment

  (def img1 (ImageIO/read (io/as-file (io/resource "cells.jpg"))))
  (def img2 (ImageIO/read (io/as-file (io/resource "dog.jpg"))))

  )
