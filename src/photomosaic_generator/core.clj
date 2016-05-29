(ns photomosaic-generator.core
  (:gen-class)
  (:require [photomosaic-generator.image :as img]
            [clojure.string :as str]))


(defn tile-info [rows cols [width height]]
  (let [w (int (/ width rows))
        h (int (/ height cols))]
    {:tiles (for [x (take rows (range 0 width w))
                  y (take cols (range 0 height h))]
              {:coords [x y]
               :dims [w h]})
     :dims [(* w rows) (* h cols)]}))

(defn pixelate
  ([image rows cols] (pixelate image rows cols 1.0))
  ([image rows cols opacity]
   (let [{:keys [dims tiles]} (tile-info rows cols (img/dims image))
         bottom (img/resize image dims)]
     (reduce (fn [bottom-acc {:keys [coords dims]}]
               (let [avg-rgb (img/average-rgb (img/tile bottom-acc coords dims))
                     color-block (img/color-block dims avg-rgb)]
                 (img/stack bottom-acc color-block coords opacity)))
             bottom
             tiles))))

(defn- image? [f]
  (let [suffixes ["png" "jpg" "jpeg" "gif"]
        preds (map #(fn [f] (.endsWith (str/lower-case (.getName f)) %)) suffixes)
        pred (apply some-fn preds)]
    (pred f)))

(defn index-image-directory [dir]
  (let [image-files (filter image? (file-seq dir))]
    (map (fn [image-file]
           (let [resized (img/resize (img/read-image image-file) [100 100])]
             {:image image-file :rgb (img/average-rgb resized)}))
         image-files)))

(defn rgb-distance [rgb1 rgb2]
  (let [diff-squared (fn [x1 x2] (* (- x2 x1) (- x2 x1)))]
    (reduce + (mapv diff-squared rgb2 rgb1))))

(defn close? [rgb1 rgb2]
  (< (rgb-distance rgb1 rgb2) 9000))

(defn image-closet-to-color [image-index [r g b]]
  (:image (first (shuffle (filter (fn [{:keys [rgb]}] (close? rgb [r g b])) image-index)))))

(defn mosaic [image image-dir rows cols opacity]
  (let [{:keys [dims tiles]} (tile-info rows cols (img/dims image))
        bottom (img/resize image dims)
        image-index (index-image-directory image-dir)]
    (reduce (fn [bottom-acc {:keys [coords dims]}]
              (let [avg-rgb (img/average-rgb (img/tile bottom-acc coords dims))
                    image-tile (image-closet-to-color image-index avg-rgb)]
                (if image-tile
                  (img/stack bottom-acc (img/resize (img/read-image image-tile) dims) coords opacity)
                  bottom-acc)))
            bottom
            tiles)))



