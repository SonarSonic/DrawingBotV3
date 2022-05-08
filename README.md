# Drawing Bot V3

![Screenshot](https://github.com/SonarSonic/DrawingBotV3/blob/master/images/screenshotV5.JPG?raw=true)
[![Platforms](https://img.shields.io/badge/platform-Windows%2C%20Mac%2C%20Linux-green?style=flat-square)](https://github.com/SonarSonic/DrawingBotV3#installation)
![GitHub top language](https://img.shields.io/github/languages/top/SonarSonic/DrawingBotV3?style=flat-square)
[![GitHub License](https://img.shields.io/github/license/SonarSonic/DrawingBotV3?style=flat-square)](https://github.com/SonarSonic/DrawingBotV3/blob/master/LICENSE)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/SonarSonic/DrawingBotV3?include_prereleases&style=flat-square)](https://github.com/SonarSonic/DrawingBotV3/releases/latest)
[![Documentation Status](https://readthedocs.org/projects/drawingbotv3/badge/?version=latest)](https://drawingbotv3.readthedocs.io/en/latest/?badge=latest)
[![Discord Shield](https://discordapp.com/api/guilds/929089222118359100/widget.png?style=shield)](https://discord.com/invite/pwNdjYxrM9)


### About

DrawingBotV3 is a software for converting images to line drawings for Plotters / Drawing Machines / 3D printers. It also serves as an application for visual artists to create stylised line drawings from images / video. You can find the full documentation [here](https://drawingbotv3.readthedocs.io/en/latest/). 

There are two versions, **Free** and **Premium**. 
- The **Free** version is Open-Source and can be downloaded [here](https://github.com/SonarSonic/DrawingBotV3/releases/latest). 
- The **Premium** version is Closed-Source and can be purchased [here](http://drawingbotv3.ollielansdell.co.uk/downloads/)

Both versions are available for Windows, Mac and Linux.

Feel free to join us on the [Discord Server](https://discord.com/invite/pwNdjYxrM9) to chat all things DrawingBotV3!

### Features - *Free Version*

-  [3 Path Finding Algorithms](https://drawingbotv3.readthedocs.io/en/latest/pfms.html): *all highly configurable to create unique drawing styles.*
-  Automatic Path Optimisation for Faster Plots: *Line Simplifying, Merging, Filtering, Sorting*   
-  [Pen Settings](https://drawingbotv3.readthedocs.io/en/latest/pensettings.html): *configurable colour / stroke width / distribution weight / blend modes - perfect for multi-layered plots.*
-  [60+ Image Filters](https://drawingbotv3.readthedocs.io/en/latest/preprocessing.html): *for pre processing the imported image*
-  [Advanced User Interface](https://drawingbotv3.readthedocs.io/en/latest/userinterface.html): *with live drawing preview* 
-  User configurable Drawing Area, *with Padding / Scaling Modes*
-  Version Control: *Save your favourite versions as you go and reload them.*
-  Project Saving & Loading: *save your work and continue where you left off!* 
-  [Special pens](https://drawingbotv3.readthedocs.io/en/latest/pensettings.html#special-drawing-pens): *for Original Colour/Grayscale Sampling*
-  [Presets](https://drawingbotv3.readthedocs.io/en/latest/presets.html): *can be saved/imported/exported for sharing different styles with other users*
-  Multiple Export Options: *can be exported per/pen or per/drawing in multiple file types*
-  [GCode Export](https://drawingbotv3.readthedocs.io/en/latest/exportsettings.html#gcode-settings): *configurable Drawing Area, XYZ Offsets / Auto Homing.*
-  [vpype](https://github.com/abey79/vpype) Integration: *automatically send plots to vpype for further optimization and processing*

### Features - *Premium Version*
-  **All the features included in the Free Version**
-  [29 Path Finding Algorithms](https://drawingbotv3.readthedocs.io/en/latest/pfms.html): *includes 26 more Path Finding Modules!*
-  Automated [CMYK separation](https://drawingbotv3.readthedocs.io/en/latest/cmyk.html)
-  [Batch Processing](https://drawingbotv3.readthedocs.io/en/latest/batchprocessing.html): *Convert entire folders of images automatically.*
-  [Export Animations](https://drawingbotv3.readthedocs.io/en/latest/exportsettings.html#image-sequence-settings): *You can export animations of your creations as Image Sequences or MP4/MOV files!*
-  Plotter / Serial Port Connection: *Connect to HPGL based plotters and control them over the serial port.*
-  Video Processing: *Convert every frame from imported video files automatically*
-  HPGL Export: *Export files for plotters, with configurable Hard-Clip limits, X-Axis Mirror, Y-Axis Mirror, X-Axis Alignment, Y-Axis Alignment, Rotation, Curve Flatness, Pen Velocity and the initial Pen Number.*
-  Hardware Accelerated Renderer: Fast OpenGL Based Renderer to preview drawings in higher clarity and speed!

### Path Finding Modules

#### Free
- Sketch Lines PFM
- Sketch Squares PFM
- Spiral PFM
  
#### Premium
- Sketch Curves PFM 
- Sketch Quad Beziers PFM
- Sketch Cubic Beziers PFM
- Sketch Catmull-Roms PFM
- Sketch Shapes PFM
- Sketch Sobel Edges PFM
- Sketch Waves PFM
- Adaptive Circular Scribbles
- Adaptive Shapes
- Adaptive Triangulation
- Adaptive Tree
- Adaptive Stippling
- Adaptive Dashes
- Adaptive Diagram
- Adaptive TSP
- Voronoi Circles
- Voronoi Triangulation
- Voronoi Tree
- Voronoi Stippling
- Voronoi Dashes
- Voronoi Diagram
- Voronoi TSP
- Mosaic Rectangles
- Mosaic Voronoi
- Mosaic Custom
- Layers PFM

More info [here](https://drawingbotv3.readthedocs.io/en/latest/pfms.html)

#### Supported File Types

```text
Import Formats: 
    Images: [.tif, .tga, .png, .jpg, .gif, .bmp, .jpeg] 
    Videos: [.mp4, .mov, .avi]
       
Export Formats: 
    Vectors: [.svg, .pdf, .hpgl (Premium)],
    Images/Image Sequences: [.png, .jpg, .jpeg, .tif, .tga]
    Videos: [.mp4 (Premium), .mov (Premium)]
    GCode: [.gcode, .txt],    
```

# Installation

Downloads: [Premium Versions](http://drawingbotv3.ollielansdell.co.uk/downloads/) or [Free Versions](https://github.com/SonarSonic/DrawingBotV3/releases/latest)

You can choose from the following options.

1) **Windows - Installer** _(.exe)_
        
   Includes all required libraries and Java Runtime. No further setup required.
   
2) **Windows - Portable** _(.zip)_
   
   Includes all required libraries and Java Runtime. No further setup required.
   
3) **Mac - Installer** _(.pkg)_

   Includes all required libraries and Java Runtime. No further setup required.

4) **Mac (x86)/Linux/Win - Executable** _(.jar)_ 

   Includes all required libraries but you must manually install [JAVA 11+](https://www.oracle.com/java/technologies/javase-downloads.html)
   
5) **Mac M1 (arm64) - Executable** _(.jar)_ 

   The bundled OpenJFX does not work on arm64 processors and using a x86 java build (through Rosetta 2 emulation) has graphical glitches & reduced performance. 
   The best option is to install a JDK build with JFX built in, such as the one provided by [Bellsoft](https://github.com/bell-sw/homebrew-liberica) just make sure to install the full package or JFX won't be bundled.

6) **Raspberry PI (ARM32) - Executable** _(.jar)_ 

   As JavaFX is no longer part of the JDK (since JAVA 11), running a JavaFX program on Raspberry Pi will not work.<br>
   BellSoft provides the [Liberica JDK](https://bell-sw.com/pages/downloads/#/java-11-lts). The version dedicated for the Raspberry Pi includes JavaFX. And setting the version by default using the update-alternatives command.<br>
   Thanks to [Frank Delporte](https://github.com/FDelporte), more info at [Java Magazine](https://blogs.oracle.com/javamagazine/getting-started-with-javafx-on-raspberry-pi)
```text
    $ cd /home/pi 
    $ wget https://download.bell-sw.com/java/13/bellsoft-jdk13-linux-arm32-vfp-hflt.deb 
    $ sudo apt-get install ./bellsoft-jdk13-linux-arm32-vfp-hflt.deb 
    $ sudo update-alternatives --config javac 
    $ sudo update-alternatives --config java
```

### Running the (.jar)

Opening the .jar may open it as an archive file, instead you should launch the jar from the terminal with the following command. Swapping in the correct file name.
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
- [JOML](https://github.com/jcodec/jcodec) - for Video Import and Export
- [JCodec](https://github.com/jcodec/jcodec) - for Video Import and Export
- [jSerialComm](https://github.com/Fazecast/jSerialComm) - for Serial Port Communication

---


DrawingBotV3 started as an expansion of [Drawbot Image to GCode V2](https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2) originally written by **Scott Cooper**. Thanks to Scott for allowing me to publish this version!
