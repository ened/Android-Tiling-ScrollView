Introduction
============

This android widget aims to provide a scalable way to show large images (like Metro maps, paintings) while keeping the memory consumption as low as possible.

The source image will be provided in TILES, so that all tiles combined create the full scale image.

Current features:

* Specify a image pattern to find the tiles
* Freely navigate through the image using a two dimensional scroll view
* Load required tiles on demand and non-blocking

Planned features:

* Zoom levels using multiple levels of tiles
* Panning, Pinching etc (multitouch gestures)
* A way to overlay icons or other widgets at fix points (similar to Google Maps), which should be useful for annotations

Example
=======
``` xml
<asia.ivity.android.tiledscrollview.TiledScrollView
    android:id="@+id/map"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    app:file_pattern="clifford_layout_highres/CROP_%col%_%row%.jpg"
    app:tile_height="120"
    app:tile_width="120"
    app:image_width="3008"
    app:image_height="2136"
    />
```
Attributes:

* file_pattern - specifies the pattern to find the files. Placeholders %col% and %row% are mandatory
* tile_height & tile_width - specify the tile dimensions. The widget should be able to handle non-fitting (i.e. if the last tile is smaller then others tiles well)
* image_height & image_width - image dimensions to support abovementioned functions.

The attributes are very likely to be reduced and cut. I prefer the widget to be more simple in the long term.

Multiple Zoom Levels
====================

The View supports different zoom levels. You can add them using Java.

``` java
TiledScrollView view = (TiledScrollView) findViewById(R.id.map);

view.addConfigurationSet(TiledScrollView.ZoomLevel.LEVEL_1, new ConfigurationSet(
    "clifford_layout_level1/crop_%col%_%row%.png", 120, 120, 2100, 1491));

view.addConfigurationSet(TiledScrollView.ZoomLevel.LEVEL_2, new ConfigurationSet(
    "clifford_layout_level2/CROP_%col%_%row%.jpg", 120, 120, 3008, 2136));
```

Credits
=======

* Apples iOS CATiledLayer for inspiring this work
* http://GORGES.us for developing and publishing a two dimensional ScrollView
* http://www.animalspedia.com/wallpaper/The-Siege---Siberian-Tiger/ for providing a nice sample picture

Apps using this library
=======================

* https://play.google.com/store/apps/details?id=asia.ivity.qifu.android.map

License
=======

This library is released under a BSD license. See the LICENSE file included with the distribution for details.