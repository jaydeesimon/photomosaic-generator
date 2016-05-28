(ns photomosaic-generator.image
  (:require [clojure.java.io :as io]
            [photomosaic-generator.core :refer [write-image]])
  (:import (javax.imageio ImageIO)
           (java.awt Color AlphaComposite Image)
           (java.awt.image BufferedImage)))

(def img1 (ImageIO/read (io/as-file (io/resource "cells.jpg"))))

(def img2 (ImageIO/read (io/as-file (io/resource "dog.jpg"))))

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

(defn- dimensions [image]
  (let [dimensions-fn (juxt #(.getWidth %) #(.getHeight %))]
    (dimensions-fn image)))

(defn- buffered-image
  ([[width height]] (buffered-image [width height] BufferedImage/TYPE_INT_ARGB))
  ([[width height] type]
   (BufferedImage. width height type)))

(defn clone
  ([image] (clone image BufferedImage/TYPE_INT_ARGB))
  ([image type] (g2d-> (buffered-image (dimensions image) type)
                       (.drawImage image 0 0 nil))))

(defn resize [image width height]
  (let [scaled-instance (.getScaledInstance image width height Image/SCALE_SMOOTH)]
    (g2d-> (buffered-image [width height])
           (.drawImage scaled-instance 0 0 nil))))

(defn place
  ([image-bottom image-top x y] (place image-bottom image-top x y 1.0))
  ([image-bottom image-top x y opacity]
   (g2d-> (clone image-bottom)
          (.setComposite (AlphaComposite/getInstance AlphaComposite/SRC_OVER opacity))
          (.drawImage image-top x y nil))))

(defn color-block [width height [r g b]]
  (g2d-> (buffered-image [width height])
         (.setColor (Color. r g b))
         (.fillRect 0 0 width height)))

(defn- gray-scale [image]
  (-> image
      (clone BufferedImage/TYPE_BYTE_GRAY)
      (clone BufferedImage/TYPE_INT_ARGB)))

(defn subimage [image width height x y]
  (.getSubimage image x y width height))