# Drawing Bot V3
![Screenshot](https://github.com/SonarSonic/Drawbot_image_to_gcode_v3/blob/master/images/ScreenshotV101.PNG?raw=true)
![Platforms](https://img.shields.io/badge/platform-Windows%2C%20Mac%2C%20Linux-green)
![GitHub top language](https://img.shields.io/github/languages/top/SonarSonic/DrawingBotV3)
![GitHub](https://img.shields.io/github/license/SonarSonic/DrawingBotV3)
![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/SonarSonic/DrawingBotV3?include_prereleases)
### About
Drawing Bot is a free, open source software for converting images to line drawings for Plotters / Drawing Machines / 3D printers. It also serves as an application for visual artists to create stylised line drawings from images / video.

It is available for Windows, Mac or Linux.

### Features
- Advanced User Interface with live drawing preview
- GCode - configurable Drawing Area, XYZ Offsets / Auto Homing.
- Path Finding Modules - configurable to create different styles
- Presets: can be saved/imported/exported for sharing different styles with other users
- Pen Settings: configurable colours / distribution weight / blend modes
- Exports can be exported per/pen or per/drawing
- Batch Processing: Convert entire folders automatically.

#### Planned Features
- Pen Plugins (Support for multiple manufacturers)

##### Supported File Types
```text
Import Formats: 
    Images: [.tif, .tga, .png, .jpg, .gif, .bmp, .jpeg] 
       
Export Formats: 
    GCode: [.gcode, .txt],
    Vectors: [.svg, .pdf],
    Images: [.tif, .tga, .png, .jpg, .jpeg]
```

##### Installation (for Windows, Mac, Linux)
- Install [JAVA 8](https://www.java.com/en/download/) - (Newer versions are not supported)
- Download the [latest release](https://github.com/SonarSonic/DrawingBotV3/releases), you can choose one of the following options.


- **-all**: a single .jar file (for Windows, Mac, Linux) 
- **-windows**: a zip containing a windows exe (for Windows only)


##### Dependencies
- [JTS Topology Suite](https://github.com/locationtech/jts) - for Vectors/Geometry
- [ImgScalr](https://github.com/rkalla/imgscalr) - for Optimised Image Scaling
- [Gson](https://github.com/google/gson) - for Configuration/Preset Files
- [iText](https://github.com/itext/itextpdf) - for PDF Rendering
- [FXGraphics2D](https://github.com/jfree/fxgraphics2d) - for Swing/JavaFX Compatibility

### Original Version
DrawingBotV3 is an expansion of [Drawbot Image to GCode V2](https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2) originally written by **Scott Cooper**. Thanks to Scott for allowing me to publish this version!
