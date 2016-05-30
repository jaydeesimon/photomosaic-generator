Trying to see if I can create a photo mosaic generator. From Wikipedia: "a photographic mosaic, also known under the term Photomosaic, a portmanteau of photo and mosaic, is a picture (usually a photograph) that has been divided into (usually equal sized) tiled sections, each of which is replaced with another photograph that matches the target photo." A well-known example of this is the Truman show poster.

![](https://bespectacled.files.wordpress.com/2008/12/truman_show_ver11.jpg)

Below is what I got so far. This is only using one photo superimposed on a picture of flowers. I matched the picture of the tiny photo by grayscaling it and then turning it into the shade of the color that I needed.

![](http://jaydeesimon.github.io/mosaics/first_mosaic.png)

**Update May 29:** Whoa, it worked! I was having trouble finding enough photos to use as tiles but then I realized I could modify my movgrab project to extract the frames from a movie. I happened to have The Good Dinosaur on my laptop so I used that as a first test. I used [this movie poster](http://image.tmdb.org/t/p/original/c6sS6IJBDDIK1dh7HmyCWIpa5ei.jpg) as the bottom.

Here's a preview of the finished product.

![](http://jaydeesimon.github.io/mosaics/good_dinosaur_small.png)

The original can be downloaded [here](http://jaydeesimon.github.io/mosaics/good_dinosaur.png) (it's 14 MB).

I cheated a little bit by laying the tiles on top of the bottom of image with an opacity of 0.5. I didn't know what to expect but it looks pretty cool. Here's the example usage that produces a BufferedImage of the photomosaic. The process took about 5 minutes on my machine but I'm sure there are plenty of opportunities to optimize it. The most obvious is that I'm resizing the tiles every time I use it.

```
(mosaic (img/read-image (io/resource "good_dinosaur_bottom.jpg"))
        (io/file "/directory/with/tile/images")
        30 40 0.5)
```

For comparison, here's the same process done with an opacity of 0.8.

![](http://jaydeesimon.github.io/mosaics/good_dinosaur_point8_small.png)

[Original](http://jaydeesimon.github.io/mosaics/good_dinosaur_point8.png)

If you don't have a large sample size of images to use as tiles, it can become difficult to match based on just the average color of the tile.