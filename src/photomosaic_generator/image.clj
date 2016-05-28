(ns photomosaic-generator.image
  (:require [clojure.java.io :as io])
  (:import (javax.imageio ImageIO)
           (java.awt Color AlphaComposite Image)
           (java.awt.image BufferedImage)))

(def img1 (ImageIO/read (io/as-file (io/resource "cells.jpg"))))

(def img2 (ImageIO/read (io/as-file (io/resource "dog.jpg"))))

(def image-types {:rgb BufferedImage/TYPE_INT_ARGB
                  :gray-scale BufferedImage/TYPE_BYTE_GRAY})

(defn- insert [coll n x]
  (let [[low high] (split-at n coll)]
    (concat low [x] high)))

(defmacro g2d-> [image & body]
  (let [result (gensym "result")
        g (gensym "g2d")]
    `(let [~result ~image
           ~g (.createGraphics ~result)]
       (try
         (do
           ~@(map #(insert % 1 g) body)
           ~result)
         (finally
           (.dispose ~g))))))

(defn dims [image]
  (let [dims-fn (juxt #(.getWidth %) #(.getHeight %))]
    (dims-fn image)))

(defn buffered-image
  ([[width height]] (buffered-image [width height] (:rgb image-types)))
  ([[width height] type]
   (BufferedImage. width height type)))

(defn clone
  ([image] (clone image BufferedImage/TYPE_INT_ARGB))
  ([image type] (g2d-> (buffered-image (dims image) type)
                       (.drawImage image 0 0 nil))))

(defn resize [image [width height]]
  (let [scaled-instance (.getScaledInstance image width height Image/SCALE_SMOOTH)]
    (g2d-> (buffered-image [width height])
           (.drawImage scaled-instance 0 0 nil))))

(defn stack
  ([^BufferedImage bottom ^BufferedImage top opacity] (stack bottom top 0 0 opacity))
  ([^BufferedImage bottom ^BufferedImage top x y] (stack bottom top x y 1.0))
  ([^BufferedImage bottom ^BufferedImage top x y opacity]
   (g2d-> (clone bottom)
          (.setComposite (AlphaComposite/getInstance AlphaComposite/SRC_OVER opacity))
          (.drawImage top x y nil))))

(defn color-block [[width height] [r g b]]
  (g2d-> (buffered-image [width height])
         (.setColor (Color. r g b))
         (.fillRect 0 0 width height)))

(defn gray-scale [image]
  (-> image
      (clone (:gray-scale image-types))
      (clone (:rgb image-types))))

(defn subimage [image [width height] x y]
  (.getSubimage image x y width height))

(defn write-png [image filename]
  (ImageIO/write image "png" (io/as-file filename)))

(defn read-image [filename]
  (ImageIO/read (io/as-file filename)))

