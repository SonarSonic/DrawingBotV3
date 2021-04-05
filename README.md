# Drawing Bot V3

![Screenshot](https://github.com/SonarSonic/Drawbot_image_to_gcode_v3/blob/master/images/ScreenshotV101.PNG?raw=true)
[![Platforms](https://img.shields.io/badge/platform-Windows%2C%20Mac%2C%20Linux-green?style=flat-square)](https://github.com/SonarSonic/DrawingBotV3#installation)
![GitHub top language](https://img.shields.io/github/languages/top/SonarSonic/DrawingBotV3?style=flat-square)
[![GitHub License](https://img.shields.io/github/license/SonarSonic/DrawingBotV3?style=flat-square)](https://github.com/SonarSonic/DrawingBotV3/blob/master/LICENSE)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/SonarSonic/DrawingBotV3?include_prereleases&style=flat-square)](https://github.com/SonarSonic/DrawingBotV3/releases/latest)
![GitHub all releases](https://img.shields.io/github/downloads/SonarSonic/DrawingBotV3/total?style=flat-square)

### About

Drawing Bot is a free, open source software for converting images to line drawings for Plotters / Drawing Machines / 3D printers. It also serves as an application for visual artists to create stylised line drawings from images / video.

It is available for Windows, Mac and Linux.

### Features

-  Advanced User Interface with live drawing preview
-  Multiple Path Finding Algorithms - configurable to create unique drawing styles
-  Automatic Path Optimisation for Faster Plots - Line Simplifying, Merging, Filtering, Sorting
-  Pen Settings: configurable colour / stroke width / distribution weight / blend modes - perfect for multi-layered plots.
-  60+ Image Filters for altering the input
-  Automated CMYK separation
-  User configurable Drawing Area, with Padding / Scaling Modes 
-  Special pens for Original Colour/Grayscale Sampling
-  Presets: can be saved/imported/exported for sharing different styles with other users
-  Exports can be exported per/pen or per/drawing in multiple file types
-  Batch Processing: Convert entire folders of images automatically.
-  GCode - configurable Drawing Area, XYZ Offsets / Auto Homing.

#### Supported File Types

```text
Import Formats: 
    Images: [.tif, .tga, .png, .jpg, .gif, .bmp, .jpeg] 
       
Export Formats: 
    Vectors: [.svg, .pdf],
    Images: [.tif, .tga, .png, .jpg, .jpeg]
    GCode: [.gcode, .txt],
```

# Installation

Downloads: [Latest Release](https://github.com/SonarSonic/DrawingBotV3/releases/latest)

You can choose from the following options.

1) **Windows - Installer** _(.exe)_
        
   Includes all required libraries and Java Runtime. No further setup required.
   
2) **Windows - Portable** _(.zip)_
   
   Includes all required libraries and Java Runtime. No further setup required.

3) **Mac/Linux/Win - Executable** _(.jar)_ 

   Includes all required libraries but you must manually install [JAVA 11+](https://www.oracle.com/java/technologies/javase-downloads.html)


### Running the (.jar) on a MAC

Sometimes opening the .jar normally won't work on MAC, instead you should launch the jar from the terminal with the following command. Swapping in the correct file name
```text
    java -jar DrawingBotV3-X.X.X-XXXX-all.jar
```



### Included Dependencies

All the dependencies are automatically included and **do not need to be installed manually**.

- [OpenJFX](https://github.com/openjdk/jfx) - for User Interface / Rendering
- [JTS Topology Suite](https://github.com/locationtech/jts) - for Vectors/Geometry
- [ImgScalr](https://github.com/rkalla/imgscalr) - for Optimised Image Scaling
- [Gson](https://github.com/google/gson) - for Configuration/Preset Files
- [Apache XML Graphics](https://github.com/apache/xmlgraphics-batik) - for SVG Rendering
- [iText](https://github.com/itext/itextpdf) - for PDF Rendering
- [FXGraphics2D](https://github.com/jfree/fxgraphics2d) - for Swing/JavaFX Compatibility
- [JHLabs](http://www.jhlabs.com/) - for Image Filters / Effects

---

### Original Version

DrawingBotV3 started as an expansion of [Drawbot Image to GCode V2](https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2) originally written by **Scott Cooper**.

Thanks to Scott for allowing me to publish this version!

---
If you want to support my work you can [donate here](https://www.paypal.com/donate?hosted_button_id=ZFNJF2R4J87DG)