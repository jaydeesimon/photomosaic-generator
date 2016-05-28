(ns photomosaic-generator.useless)


;; Pulled this from
;; http://stackoverflow.com/questions/1725505/finding-similar-colors-programatically

(comment
  (defn- rgb-distance [rgb1 rgb2]
    (let [diff-squared (fn [x1 x2] (* (- x2 x1) (- x2 x1)))]
      (reduce + (mapv diff-squared rgb2 rgb1))))

  (defn- close? [rgb1 rgb2]
    (< (rgb-distance rgb1 rgb2) 1000))

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
    (map (comp clone-image black-and-white clone-image) images))

  (defn- place-blocks-image [image image-blocks rows cols]
    (let [block-rgb-seq (block-rgb-seq image rows cols)
          image-blocks (prepare-image-blocks image-blocks)]
      (reduce (fn [image-acc {:keys [x y width height rgb]}]
                (let [image-to-use (first (shuffle image-blocks))
                      block-image (generate-colored-image-block (resize-image image-to-use width height) rgb)]
                  (place-image-on-image image-acc block-image x y)))
              image
              block-rgb-seq)))



  )

