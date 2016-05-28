(ns photomosaic-generator.core
  (:gen-class)
  (:require [photomosaic-generator.image :as img]))


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


