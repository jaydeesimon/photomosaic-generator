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

(defn- copy-image [image]
  (let [bi (BufferedImage. (.getWidth image) (.getHeight image) BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics bi)]
    (do (.drawImage g image 0 0 nil)
        (.dispose g)
        bi)))

(defn- black-and-white [image]
  (let [bwi (BufferedImage. (.getWidth image) (.getHeight image) BufferedImage/TYPE_BYTE_GRAY)
        g (.createGraphics bwi)]
    (do (.drawImage g image 0 0 nil)
        (.dispose g)
        bwi)))

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

(defn place-image-on-image
  ([image-bottom image-top x y] (place-image-on-image image-bottom image-top x y 1.0))
  ([image-bottom image-top x y opacity]
   (do (doto (.createGraphics image-bottom)
             (.setComposite (AlphaComposite/getInstance AlphaComposite/SRC_OVER opacity))
             (.drawImage image-top x y nil)
             (.dispose))
       image-bottom)))

(defn resize-image [image width height]
  (let [scaled-instance (.getScaledInstance image width height Image/SCALE_SMOOTH)
        resized-image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (.createGraphics resized-image)]
    (do (.drawImage g scaled-instance 0 0 nil)
        (.dispose g)
        resized-image)))

(defn subimage [image x y w h]
  (.getSubimage image x y w h))

(defn write-image [image filename]
  (ImageIO/write image "png" (io/as-file filename)))

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

(defn- block-rgb-seq [image rows cols]
  (let [max-width (.getWidth image)
        max-height (.getHeight image)]
    (map (fn [{:keys [x y width height] :as block-info}]
           (assoc block-info :rgb (avg-rgb (rgb-seq (subimage image x y width height)))))
         (block-seq rows cols max-width max-height))))

;; Pulled this from
;; http://stackoverflow.com/questions/1725505/finding-similar-colors-programatically
(defn- rgb-distance [rgb1 rgb2]
  (let [diff-squared (fn [x1 x2] (* (- x2 x1) (- x2 x1)))]
    (reduce + (mapv diff-squared rgb2 rgb1))))

(defn- close? [rgb1 rgb2]
  (< (rgb-distance rgb1 rgb2) 1000))

(defn- generate-color-block [width height [r g b]]
  (let [bi (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        graphics (.createGraphics bi)]
    (do (.setColor graphics (Color. r g b))
        (.fillRect graphics 0 0 width height)
        (.dispose graphics)
        bi)))

(defn- generate-colored-image-block [image [r g b]]
  (let [width (.getWidth image)
        height (.getHeight image)
        color-block-image (generate-color-block width height [r g b])]
    (place-image-on-image image color-block-image 0 0 0.5)))

(defn- place-blocks [image rows cols]
  (let [block-rgb-seq (block-rgb-seq image rows cols)]
    (reduce (fn [image-acc {:keys [x y width height rgb]}]
              (let [block-image (generate-color-block width height rgb)]
                (place-image-on-image image-acc block-image x y)))
            image
            block-rgb-seq)))

(defn- prepare-image-blocks [images]
  (map (comp copy-image black-and-white copy-image) images))

(defn- place-blocks-image [image image-blocks rows cols]
  (let [block-rgb-seq (block-rgb-seq image rows cols)
        image-blocks (prepare-image-blocks image-blocks)]
    (reduce (fn [image-acc {:keys [x y width height rgb]}]
              (let [image-to-use (first (shuffle image-blocks))
                    block-image (generate-colored-image-block (resize-image image-to-use width height) rgb)]
                (place-image-on-image image-acc block-image x y)))
            image
            block-rgb-seq)))

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
