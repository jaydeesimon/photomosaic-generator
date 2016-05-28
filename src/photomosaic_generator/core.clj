(ns photomosaic-generator.core
  (:gen-class)
  (:require [photomosaic-generator.image :as img]))


(defn tile-info [rows cols width height]
  (let [w (int (/ width rows))
        h (int (/ height cols))]
    {:tiles (for [x (take rows (range 0 width w))
                  y (take cols (range 0 height h))]
              {:x x
               :y y
               :width w
               :height h})
     :width (* w rows)
     :height (* h cols)}))
