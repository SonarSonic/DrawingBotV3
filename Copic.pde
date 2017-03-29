// Regex used in sublime to clean up html from:
// Source data:  https://imaginationinternationalinc.com/copic/store/color-picker/
//
// ^.*color:
// ; cursor.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*<h5>
// </h5>\n.*<p>
// </p>\n.*clearfix.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n
// ^(.*?),(.*?),(.*)
// sketch_color.put("$2"), color($1));  sketch_name.put("$2"), "$3");


///////////////////////////////////////////////////////////////////////////////////////////////////////
void copic_alpha_simulator() {
  color[] p = new color[5]; 
  p[0] = copic.get_original_color("N1");
  p[1] = copic.get_original_color("N3");
  p[2] = copic.get_original_color("N5");
  p[3] = copic.get_original_color("N7");
  p[4] = copic.get_original_color("100");
  
  int alpha = 210;
  int pen_off=200;
  int off=30;
  
  for(int pen=0; pen<5; pen++) {
    for(int x=0; x<5; x++) {
      //fill(p[pen], alpha);  rect(pen*150+10, pen*off+x*pen_off, 500, 80);
      stroke(p[pen], alpha);
      strokeWeight(50);
      fill(p[4], 50);
      line(pen*150+10, pen*off+x*pen_off, pen*150+10+500, pen*off+x*pen_off);
    }
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void test_draw_closest_copic_color() {
  noStroke();
  
  for (int i = 0; i < 255; i++) {
    colorMode(HSB, 255);
    color c1 = color(i, mouseX, mouseY);
    //color c1 = color(mouseX, i, mouseY);
    //color c1 = color(mouseX, mouseY, i);
    fill(c1);
    rect(i*5, 0, 5, 100);
        
    String p = copic.get_closest_original(c1);
    //println(p + "   " + c.get_original_name(p));
    color c2 = copic.get_original_color(p);
    fill(c2);
    rect(i*5, 105, 5, 100);
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
class Copix {
  HashMap <String, Integer> sketch_color;
  HashMap <String, String> sketch_name;
  HashMap <String, Integer> original_color;
  HashMap <String, String> original_name;
 
  Copix() {
    sketch_color = new HashMap <String, Integer> ();
    sketch_name = new HashMap <String, String> ();
    original_color = new HashMap <String, Integer> ();
    original_name = new HashMap <String, String> ();
    
    sketch_color.put("0", color(#ffffff));  sketch_name.put("0", "Colorless Blender");
    sketch_color.put("100", color(#312b2b));  sketch_name.put("100", "Black");
    sketch_color.put("110", color(#030708));  sketch_name.put("110", "Special Black");
    sketch_color.put("B0000", color(#f0f9fe));  sketch_name.put("B0000", "Pale Celestine");
    sketch_color.put("B000", color(#e6f4f5));  sketch_name.put("B000", "Pale Porcelain Blue");
    sketch_color.put("B00", color(#ddf0f4));  sketch_name.put("B00", "Frost Blue");
    sketch_color.put("B01", color(#d6eef2));  sketch_name.put("B01", "Mint Blue");
    sketch_color.put("B02", color(#b3e3f1));  sketch_name.put("B02", "Robin&#39;s Egg Blue");
    sketch_color.put("B04", color(#73cfe6));  sketch_name.put("B04", "Tahitian Blue");
    sketch_color.put("B05", color(#40c5e6));  sketch_name.put("B05", "Process Blue");
    sketch_color.put("B06", color(#00b3e6));  sketch_name.put("B06", "Peacok Blue");
    sketch_color.put("B12", color(#c8e6f0));  sketch_name.put("B12", "Ice Blue");
    sketch_color.put("B14", color(#71cfeb));  sketch_name.put("B14", "Light Blue");
    sketch_color.put("B16", color(#00bcea));  sketch_name.put("B16", "Cyanine Blue");
    sketch_color.put("B18", color(#1d8acb));  sketch_name.put("B18", "Lapis Lazuli");
    sketch_color.put("B21", color(#dbedf9));  sketch_name.put("B21", "Baby Blue");
    sketch_color.put("B23", color(#92c2e8));  sketch_name.put("B23", "Phthalo Blue");
    sketch_color.put("B24", color(#8acef3));  sketch_name.put("B24", "Sky");
    sketch_color.put("B26", color(#65b3e3));  sketch_name.put("B26", "Cobalt Blue");
    sketch_color.put("B28", color(#196db6));  sketch_name.put("B28", "Royal Blue");
    sketch_color.put("B29", color(#0177c1));  sketch_name.put("B29", "Ultramarine");
    sketch_color.put("B32", color(#e2eff7));  sketch_name.put("B32", "Pale Blue");
    sketch_color.put("B34", color(#82c3ed));  sketch_name.put("B34", "Manganese Blue");
    sketch_color.put("B37", color(#156fa4));  sketch_name.put("B37", "Antwerp Blue");
    sketch_color.put("B39", color(#2b64a9));  sketch_name.put("B39", "Prussian Blue");
    sketch_color.put("B41", color(#e2f0fb));  sketch_name.put("B41", "Powder Blue");
    sketch_color.put("B45", color(#75c0ea));  sketch_name.put("B45", "Smoky Blue");
    sketch_color.put("B52", color(#adcddc));  sketch_name.put("B52", "Soft Greenish Blue");
    sketch_color.put("B60", color(#dae1f3));  sketch_name.put("B60", "Pale Blue Gray");
    sketch_color.put("B63", color(#a7bbe0));  sketch_name.put("B63", "Light Hydrangea");
    sketch_color.put("B66", color(#6888c5));  sketch_name.put("B66", "Clematis");
    sketch_color.put("B69", color(#2165ae));  sketch_name.put("B69", "Stratospheric Blue");
    sketch_color.put("B79", color(#3b479d));  sketch_name.put("B79", "Iris");
    sketch_color.put("B91", color(#d5e2eb));  sketch_name.put("B91", "Pale Grayish Blue");
    sketch_color.put("B93", color(#95c1da));  sketch_name.put("B93", "Light Crockery Blue");
    sketch_color.put("B95", color(#74a7c6));  sketch_name.put("B95", "Light Grayish Cobalt");
    sketch_color.put("B97", color(#457a9a));  sketch_name.put("B97", "Night Blue");
    sketch_color.put("B99", color(#0f547e));  sketch_name.put("B99", "Agate");
    sketch_color.put("BG0000", color(#eff8f3));  sketch_name.put("BG0000", "Snow Green");
    sketch_color.put("BG000", color(#e5f4ed));  sketch_name.put("BG000", "Pale Aqua");
    sketch_color.put("BG01", color(#c7e6fa));  sketch_name.put("BG01", "Aqua Blue");
    sketch_color.put("BG02", color(#c6e8ea));  sketch_name.put("BG02", "New Blue");
    sketch_color.put("BG05", color(#83d2e1));  sketch_name.put("BG05", "Holiday Blue");
    sketch_color.put("BG07", color(#1db8ce));  sketch_name.put("BG07", "Petroleum Blue");
    sketch_color.put("BG09", color(#01b1c9));  sketch_name.put("BG09", "Blue Green");
    sketch_color.put("BG10", color(#dcf0ef));  sketch_name.put("BG10", "Cool Shadow");
    sketch_color.put("BG11", color(#ceebf1));  sketch_name.put("BG11", "Moon White");
    sketch_color.put("BG13", color(#c4e7e9));  sketch_name.put("BG13", "Mint Green");
    sketch_color.put("BG15", color(#a0d9d2));  sketch_name.put("BG15", "Aqua");
    sketch_color.put("BG18", color(#37c0b0));  sketch_name.put("BG18", "Teal Blue");
    sketch_color.put("BG23", color(#bde5dd));  sketch_name.put("BG23", "Coral Sea");
    sketch_color.put("BG32", color(#bce2d7));  sketch_name.put("BG32", "Aqua Mint");
    sketch_color.put("BG34", color(#a3dad7));  sketch_name.put("BG34", "Horizon Green");
    sketch_color.put("BG45", color(#afdfdf));  sketch_name.put("BG45", "Nile Blue");
    sketch_color.put("BG49", color(#00b6b9));  sketch_name.put("BG49", "Duck Blue");
    sketch_color.put("BG53", color(#accfd1));  sketch_name.put("BG53", "Ice Mint");
    sketch_color.put("BG57", color(#64bebe));  sketch_name.put("BG57", "Sketch Jasper");
    sketch_color.put("BG70", color(#daecee));  sketch_name.put("BG70", "Ocean Mist");
    sketch_color.put("BG72", color(#74b8bb));  sketch_name.put("BG72", "Ice Ocean");
    sketch_color.put("BG75", color(#59918e));  sketch_name.put("BG75", "Abyss Green");
    sketch_color.put("BG78", color(#49706b));  sketch_name.put("BG78", "Bronze");
    sketch_color.put("BG90", color(#e8ede7));  sketch_name.put("BG90", "Sketch Gray Sky");
    sketch_color.put("BG93", color(#bac1b9));  sketch_name.put("BG93", "Green Gray");
    sketch_color.put("BG96", color(#81a291));  sketch_name.put("BG96", "Bush");
    sketch_color.put("BG99", color(#6e9b87));  sketch_name.put("BG99", "Flagstone Blue");
    sketch_color.put("BV0000", color(#eae7f2));  sketch_name.put("BV0000", "Sketch Pale Thistle");
    sketch_color.put("BV000", color(#eae7f2));  sketch_name.put("BV000", "Iridescent Mauve");
    sketch_color.put("BV00", color(#e0dced));  sketch_name.put("BV00", "Mauve Shadow");
    sketch_color.put("BV01", color(#c4c9e6));  sketch_name.put("BV01", "Viola");
    sketch_color.put("BV02", color(#aab8db));  sketch_name.put("BV02", "Prune");
    sketch_color.put("BV04", color(#7c97ce));  sketch_name.put("BV04", "Blue Berry");
    sketch_color.put("BV08", color(#9d7eb9));  sketch_name.put("BV08", "Blue Violet");
    sketch_color.put("BV11", color(#d4d2e8));  sketch_name.put("BV11", "Soft Violet");
    sketch_color.put("BV13", color(#8491c8));  sketch_name.put("BV13", "Hydrangea Blue");
    sketch_color.put("BV17", color(#6e84bd));  sketch_name.put("BV17", "Deep Reddish Blue");
    sketch_color.put("BV20", color(#cfdbf1));  sketch_name.put("BV20", "Dull Lavender");
    sketch_color.put("BV23", color(#b1c0dd));  sketch_name.put("BV23", "Grayish Lavender");
    sketch_color.put("BV25", color(#8184a7));  sketch_name.put("BV25", "Grayish Violet");
    sketch_color.put("BV29", color(#384558));  sketch_name.put("BV29", "Slate");
    sketch_color.put("BV31", color(#eae7f2));  sketch_name.put("BV31", "Pale Lavender");
    sketch_color.put("BV34", color(#9fa7bc));  sketch_name.put("BV34", "Sketch Bluebell");
    sketch_color.put("C00", color(#e8f0f3));  sketch_name.put("C00", "Cool Gray");
    sketch_color.put("C0", color(#e0e7ed));  sketch_name.put("C0", "Cool Gray");
    sketch_color.put("C10", color(#202b31));  sketch_name.put("C10", "Cool Gray");
    sketch_color.put("C1", color(#dae3e8));  sketch_name.put("C1", "Cool Gray No. 1");
    sketch_color.put("C2", color(#ccd7dd));  sketch_name.put("C2", "Cool Gray");
    sketch_color.put("C3", color(#c1ccd2));  sketch_name.put("C3", "Cool Gray No. 3");
    sketch_color.put("C4", color(#a6b4bd));  sketch_name.put("C4", "Cool Gray");
    sketch_color.put("C5", color(#92a0ab));  sketch_name.put("C5", "Cool Gray No. 5");
    sketch_color.put("C6", color(#7b8c96));  sketch_name.put("C6", "Cool Gray");
    sketch_color.put("C7", color(#637079));  sketch_name.put("C7", "Cool Gray No. 7");
    sketch_color.put("C8", color(#535d66));  sketch_name.put("C8", "Cool Gray");
    sketch_color.put("C9", color(#3c474d));  sketch_name.put("C9", "Cool Gray");
    sketch_color.put("E0000", color(#fffaf4));  sketch_name.put("E0000", "Floral White");
    sketch_color.put("E000", color(#fef5ee));  sketch_name.put("E000", "Pale Fruit Pink");
    sketch_color.put("E00", color(#fdf3ea));  sketch_name.put("E00", "Cotton Pearl");
    sketch_color.put("E01", color(#ffeee4));  sketch_name.put("E01", "Pink Flamingo");
    sketch_color.put("E02", color(#feece0));  sketch_name.put("E02", "Fruit Pink");
    sketch_color.put("E04", color(#e4bcc4));  sketch_name.put("E04", "Lipstick Natural");
    sketch_color.put("E07", color(#cc816a));  sketch_name.put("E07", "Light Mahogany");
    sketch_color.put("E08", color(#ca6553));  sketch_name.put("E08", "Brown");
    sketch_color.put("E09", color(#d96a4f));  sketch_name.put("E09", "Burnt Sienna");
    sketch_color.put("E11", color(#fee9d6));  sketch_name.put("E11", "Barley Beige");
    sketch_color.put("E13", color(#e9c5af));  sketch_name.put("E13", "Light Suntan");
    sketch_color.put("E15", color(#fbbb8d));  sketch_name.put("E15", "Dark Suntan");
    sketch_color.put("E17", color(#b85f57));  sketch_name.put("E17", "Reddish Brass");
    sketch_color.put("E18", color(#88534d));  sketch_name.put("E18", "Copper");
    sketch_color.put("E19", color(#c45238));  sketch_name.put("E19", "Redwood");
    sketch_color.put("E21", color(#fde2c7));  sketch_name.put("E21", "Soft Sun");
    sketch_color.put("E23", color(#eccab1));  sketch_name.put("E23", "Hazelnut");
    sketch_color.put("E25", color(#d2a482));  sketch_name.put("E25", "Caribe Cocoa");
    sketch_color.put("E27", color(#997663));  sketch_name.put("E27", "Milk Chocolate");
    sketch_color.put("E29", color(#884636));  sketch_name.put("E29", "Burnt Umber");
    sketch_color.put("E30", color(#f7f0d6));  sketch_name.put("E30", "Bisque");
    sketch_color.put("E31", color(#f2e6ce));  sketch_name.put("E31", "Brick Beige");
    sketch_color.put("E33", color(#f3d2b1));  sketch_name.put("E33", "Sand");
    sketch_color.put("E34", color(#f0caa6));  sketch_name.put("E34", "Toast");
    sketch_color.put("E35", color(#e6c3a3));  sketch_name.put("E35", "Chamois");
    sketch_color.put("E37", color(#cc9159));  sketch_name.put("E37", "Sepia");
    sketch_color.put("E39", color(#c5743f));  sketch_name.put("E39", "Leather");
    sketch_color.put("E40", color(#f2e8dc));  sketch_name.put("E40", "Brick White");
    sketch_color.put("E41", color(#fef1e1));  sketch_name.put("E41", "Pearl White");
    sketch_color.put("E42", color(#f3ead9));  sketch_name.put("E42", "Sand White");
    sketch_color.put("E43", color(#e8dabd));  sketch_name.put("E43", "Dull Ivory");
    sketch_color.put("E44", color(#c5b9a9));  sketch_name.put("E44", "Clay");
    sketch_color.put("E47", color(#8a6e59));  sketch_name.put("E47", "Dark Brown");
    sketch_color.put("E49", color(#634c3c));  sketch_name.put("E49", "Dark Bark");
    sketch_color.put("E50", color(#f4ebf0));  sketch_name.put("E50", "Egg Shell");
    sketch_color.put("E51", color(#feecd6));  sketch_name.put("E51", "Milky White");
    sketch_color.put("E53", color(#f3e6c3));  sketch_name.put("E53", "Raw Silk");
    sketch_color.put("E55", color(#f1dfb9));  sketch_name.put("E55", "Light Camel");
    sketch_color.put("E57", color(#b18558));  sketch_name.put("E57", "Light Walnut");
    sketch_color.put("E59", color(#9a7f6c));  sketch_name.put("E59", "Walnut");
    sketch_color.put("E70", color(#efeae6));  sketch_name.put("E70", "Ash Rose");
    sketch_color.put("E71", color(#e2d7d3));  sketch_name.put("E71", "Champagne");
    sketch_color.put("E74", color(#a1847c));  sketch_name.put("E74", "Cocoa Brown");
    sketch_color.put("E77", color(#7f604e));  sketch_name.put("E77", "Maroon");
    sketch_color.put("E79", color(#4a2c22));  sketch_name.put("E79", "Cashew");
    sketch_color.put("E81", color(#f0e6c2));  sketch_name.put("E81", "Ivory");
    sketch_color.put("E84", color(#ae9f80));  sketch_name.put("E84", "- Sketch Khaki");
    sketch_color.put("E87", color(#6f604d));  sketch_name.put("E87", "Fig");
    sketch_color.put("E89", color(#5a4939));  sketch_name.put("E89", "- Sketch Pecan");
    sketch_color.put("E93", color(#fed2b9));  sketch_name.put("E93", "Tea Rose");
    sketch_color.put("E95", color(#fcbc7e));  sketch_name.put("E95", "Tea Orange");
    sketch_color.put("E97", color(#ed9c5d));  sketch_name.put("E97", "Deep Orange");
    sketch_color.put("E99", color(#b46034));  sketch_name.put("E99", "Baked Clay");
    sketch_color.put("FB2", color(#058fd0));  sketch_name.put("FB2", "Fluorescent Dull Blue");
    sketch_color.put("FBG2", color(#62cbe8));  sketch_name.put("FBG2", "Fluorescent Dull Blue Green");
    sketch_color.put("FRV1", color(#f5a3c7));  sketch_name.put("FRV1", "Fluorescent Pink");
    sketch_color.put("FV2", color(#7f74b6));  sketch_name.put("FV2", "Fluorescent Dull Violet");
    sketch_color.put("FY1", color(#fff697));  sketch_name.put("FY1", "Fluorescent Yellow Orange");
    sketch_color.put("FYG1", color(#9ecd43));  sketch_name.put("FYG1", "Fluorescent Yellow");
    sketch_color.put("FYG2", color(#9ecd43));  sketch_name.put("FYG2", "Fluorescent Dull Yellow Green");
    sketch_color.put("FYR1", color(#fecc99));  sketch_name.put("FYR1", "Fluorescent Orange");
    sketch_color.put("G0000", color(#f1f7f3));  sketch_name.put("G0000", "Crystal Opal");
    sketch_color.put("G000", color(#eaf5ed));  sketch_name.put("G000", "Pale Green");
    sketch_color.put("G00", color(#e3f2ed));  sketch_name.put("G00", "Jade Green");
    sketch_color.put("G02", color(#cfe8d3));  sketch_name.put("G02", "Spectrum Green");
    sketch_color.put("G03", color(#b6da9c));  sketch_name.put("G03", "Meadow Green");
    sketch_color.put("G05", color(#69c07b));  sketch_name.put("G05", "Emerald Green");
    sketch_color.put("G07", color(#7bc576));  sketch_name.put("G07", "Nile Green");
    sketch_color.put("G09", color(#7ac465));  sketch_name.put("G09", "Veronese Green");
    sketch_color.put("G12", color(#d2e8c4));  sketch_name.put("G12", "Sea Green");
    sketch_color.put("G14", color(#97cf90));  sketch_name.put("G14", "Apple Green");
    sketch_color.put("G16", color(#60c198));  sketch_name.put("G16", "Malachite");
    sketch_color.put("G17", color(#14b37d));  sketch_name.put("G17", "Forest Green");
    sketch_color.put("G19", color(#2db98a));  sketch_name.put("G19", "Bright Parrot Green");
    sketch_color.put("G20", color(#edf6db));  sketch_name.put("G20", "Wax White");
    sketch_color.put("G21", color(#c4e4cd));  sketch_name.put("G21", "Lime Green");
    sketch_color.put("G24", color(#c3e0b4));  sketch_name.put("G24", "Willow");
    sketch_color.put("G28", color(#119462));  sketch_name.put("G28", "Ocean Green");
    sketch_color.put("G29", color(#197c5d));  sketch_name.put("G29", "Pine Tree Green");
    sketch_color.put("G40", color(#e4f1df));  sketch_name.put("G40", "Dim Green");
    sketch_color.put("G43", color(#d7e7a8));  sketch_name.put("G43", "Various Pistachio");
    sketch_color.put("G46", color(#579e74));  sketch_name.put("G46", "Sketch Mistletoe");
    sketch_color.put("G82", color(#ccdab9));  sketch_name.put("G82", "Spring Dim Green");
    sketch_color.put("G85", color(#9dc3aa));  sketch_name.put("G85", "Verdigris");
    sketch_color.put("G94", color(#98a786));  sketch_name.put("G94", "Grayish Olive");
    sketch_color.put("G99", color(#5f7e3a));  sketch_name.put("G99", "Olive");
    sketch_color.put("N0", color(#eceeed));  sketch_name.put("N0", "Neutral Gray");
    sketch_color.put("N10", color(#312f30));  sketch_name.put("N10", "Neutral Gray");
    sketch_color.put("N1", color(#e2e3e5));  sketch_name.put("N1", "Neutral Gray");
    sketch_color.put("N2", color(#dadbdd));  sketch_name.put("N2", "Neutral Gray");
    sketch_color.put("N3", color(#d1d2d4));  sketch_name.put("N3", "Neutral Gray");
    sketch_color.put("N4", color(#bcbdc1));  sketch_name.put("N4", "Neutral Gray");
    sketch_color.put("N5", color(#a8a9ad));  sketch_name.put("N5", "Neutral Gray");
    sketch_color.put("N6", color(#949599));  sketch_name.put("N6", "Neutral Gray");
    sketch_color.put("N7", color(#77787c));  sketch_name.put("N7", "Neutral Gray");
    sketch_color.put("N8", color(#636466));  sketch_name.put("N8", "Neutral Gray");
    sketch_color.put("N9", color(#4c4d4f));  sketch_name.put("N9", "Neutral Gray");
    sketch_color.put("R0000", color(#fef3ef));  sketch_name.put("R0000", "Pink Beryl");
    sketch_color.put("R000", color(#fef0e7));  sketch_name.put("R000", "Cherry White");
    sketch_color.put("R00", color(#feeae1));  sketch_name.put("R00", "Pinkish White");
    sketch_color.put("R01", color(#fde0d8));  sketch_name.put("R01", "Pinkish Vanilla");
    sketch_color.put("R02", color(#fdd3c7));  sketch_name.put("R02", "Rose Salmon");
    sketch_color.put("R05", color(#f6927b));  sketch_name.put("R05", "Salmon Red");
    sketch_color.put("R08", color(#f26754));  sketch_name.put("R08", "Vermilion");
    sketch_color.put("R11", color(#fde1d5));  sketch_name.put("R11", "Pale Cherry Pink");
    sketch_color.put("R12", color(#fcd3c1));  sketch_name.put("R12", "Light Tea Rose");
    sketch_color.put("R14", color(#f59b92));  sketch_name.put("R14", "Light Rouge");
    sketch_color.put("R17", color(#f4846c));  sketch_name.put("R17", "Lipstick Orange");
    sketch_color.put("R20", color(#fcd7cf));  sketch_name.put("R20", "Blush");
    sketch_color.put("R21", color(#fac1b6));  sketch_name.put("R21", "Sardonyx");
    sketch_color.put("R22", color(#f8b7b1));  sketch_name.put("R22", "Light Prawn");
    sketch_color.put("R24", color(#f27579));  sketch_name.put("R24", "Prawn");
    sketch_color.put("R27", color(#f15062));  sketch_name.put("R27", "Cadmium Red");
    sketch_color.put("R29", color(#ed174b));  sketch_name.put("R29", "Lipstick Red");
    sketch_color.put("R30", color(#fce3df));  sketch_name.put("R30", "Pale Yellowish Pink");
    sketch_color.put("R32", color(#fac1ba));  sketch_name.put("R32", "Peach");
    sketch_color.put("R35", color(#f27185));  sketch_name.put("R35", "Coral");
    sketch_color.put("R37", color(#e86c74));  sketch_name.put("R37", "Carmine");
    sketch_color.put("R39", color(#cb487a));  sketch_name.put("R39", "Garnet");
    sketch_color.put("R43", color(#ee848e));  sketch_name.put("R43", "Bougainvillaea");
    sketch_color.put("R46", color(#e04d69));  sketch_name.put("R46", "Strong Red");
    sketch_color.put("R56", color(#d27c95));  sketch_name.put("R56", "Currant");
    sketch_color.put("R59", color(#b74f70));  sketch_name.put("R59", "Cardinal");
    sketch_color.put("R81", color(#f1c8d6));  sketch_name.put("R81", "Rose Pink");
    sketch_color.put("R83", color(#f19cb9));  sketch_name.put("R83", "Rose Mist");
    sketch_color.put("R85", color(#d36a93));  sketch_name.put("R85", "Rose Red");
    sketch_color.put("R89", color(#7d2b42));  sketch_name.put("R89", "Dark Red");
    sketch_color.put("RV0000", color(#f2eaf5));  sketch_name.put("RV0000", "Evening Primrose");
    sketch_color.put("RV000", color(#f4e2ee));  sketch_name.put("RV000", "Pale Purple");
    sketch_color.put("RV00", color(#f1daea));  sketch_name.put("RV00", "Water Lily");
    sketch_color.put("RV02", color(#fad5e6));  sketch_name.put("RV02", "Sugared Almond Pink");
    sketch_color.put("RV04", color(#f6a3bf));  sketch_name.put("RV04", "Shock  Pink");
    sketch_color.put("RV06", color(#f386af));  sketch_name.put("RV06", "Cerise");
    sketch_color.put("RV09", color(#e171ac));  sketch_name.put("RV09", "Fuchsia");
    sketch_color.put("RV10", color(#fdecf4));  sketch_name.put("RV10", "Pale Pink");
    sketch_color.put("RV11", color(#fbd6dd));  sketch_name.put("RV11", "Pink");
    sketch_color.put("RV13", color(#f9c9d7));  sketch_name.put("RV13", "Tender Pink");
    sketch_color.put("RV14", color(#f495b7));  sketch_name.put("RV14", "Begonia Pink");
    sketch_color.put("RV17", color(#db7eb3));  sketch_name.put("RV17", "Deep Magenta");
    sketch_color.put("RV19", color(#d268aa));  sketch_name.put("RV19", "Red Violet");
    sketch_color.put("RV21", color(#fde8e7));  sketch_name.put("RV21", "Light Pink");
    sketch_color.put("RV23", color(#f8bac9));  sketch_name.put("RV23", "Pure Pink");
    sketch_color.put("RV25", color(#f493be));  sketch_name.put("RV25", "Dog Rose Flower");
    sketch_color.put("RV29", color(#ef4880));  sketch_name.put("RV29", "Crimson");
    sketch_color.put("RV32", color(#fad3ce));  sketch_name.put("RV32", "Shadow Pink");
    sketch_color.put("RV34", color(#f9afae));  sketch_name.put("RV34", "Dark Pink");
    sketch_color.put("RV42", color(#f8bbb6));  sketch_name.put("RV42", "Salmon Pink");
    sketch_color.put("RV52", color(#f9cade));  sketch_name.put("RV52", "Various Cotton Candy");
    sketch_color.put("RV55", color(#e9a5ca));  sketch_name.put("RV55", "Hollyhock");
    sketch_color.put("RV63", color(#d09dae));  sketch_name.put("RV63", "Begonia");
    sketch_color.put("RV66", color(#b86a84));  sketch_name.put("RV66", "Raspberry");
    sketch_color.put("RV69", color(#8b576e));  sketch_name.put("RV69", "Peony");
    sketch_color.put("RV91", color(#e6d4e2));  sketch_name.put("RV91", "Garyish Cherry");
    sketch_color.put("RV93", color(#e7b6cc));  sketch_name.put("RV93", "Smokey Purple");
    sketch_color.put("RV95", color(#b684a1));  sketch_name.put("RV95", "Baby Blossoms");
    sketch_color.put("RV99", color(#5a4858));  sketch_name.put("RV99", "Argyle Purple");
    sketch_color.put("T0", color(#eceeed));  sketch_name.put("T0", "Toner Gray");
    sketch_color.put("T10", color(#322e2d));  sketch_name.put("T10", "Toner Gray");
    sketch_color.put("T1", color(#eaeae8));  sketch_name.put("T1", "Toner Gray");
    sketch_color.put("T2", color(#e0e0de));  sketch_name.put("T2", "Toner Gray");
    sketch_color.put("T3", color(#d1d2cc));  sketch_name.put("T3", "Toner Gray");
    sketch_color.put("T4", color(#bcbbb9));  sketch_name.put("T4", "Toner Gray");
    sketch_color.put("T5", color(#a8a7a3));  sketch_name.put("T5", "Toner Gray");
    sketch_color.put("T6", color(#949590));  sketch_name.put("T6", "Toner Gray");
    sketch_color.put("T7", color(#777674));  sketch_name.put("T7", "Toner Gray");
    sketch_color.put("T8", color(#63645f));  sketch_name.put("T8", "Toner Gray");
    sketch_color.put("T9", color(#4c4b49));  sketch_name.put("T9", "Toner Gray");
    sketch_color.put("V0000", color(#f0edf6));  sketch_name.put("V0000", "Rose Quartz");
    sketch_color.put("V000", color(#e9e5f3));  sketch_name.put("V000", "Pale Heath");
    sketch_color.put("V01", color(#e4c1d9));  sketch_name.put("V01", "Heath");
    sketch_color.put("V04", color(#e6aace));  sketch_name.put("V04", "Lilac");
    sketch_color.put("V05", color(#e2a6ca));  sketch_name.put("V05", "Azalea");
    sketch_color.put("V06", color(#ce95c2));  sketch_name.put("V06", "Lavender");
    sketch_color.put("V09", color(#8754a1));  sketch_name.put("V09", "Violet");
    sketch_color.put("V12", color(#eed7e9));  sketch_name.put("V12", "Pale Lilac");
    sketch_color.put("V15", color(#d3a6cd));  sketch_name.put("V15", "Mallow");
    sketch_color.put("V17", color(#a092c7));  sketch_name.put("V17", "Amethyst");
    sketch_color.put("V20", color(#e2e0ed));  sketch_name.put("V20", "Wisteria");
    sketch_color.put("V22", color(#b2b1d0));  sketch_name.put("V22", "Sketch Ash Lavender");
    sketch_color.put("V25", color(#857fad));  sketch_name.put("V25", "Pale Blackberry");
    sketch_color.put("V28", color(#6b668e));  sketch_name.put("V28", "Sketch Eggplant");
    sketch_color.put("V91", color(#e8c4d0));  sketch_name.put("V91", "Pale Grape");
    sketch_color.put("V93", color(#e5c1db));  sketch_name.put("V93", "Early Grape");
    sketch_color.put("V95", color(#b77ca8));  sketch_name.put("V95", "Light Grape");
    sketch_color.put("V99", color(#524358));  sketch_name.put("V99", "Aubergine");
    sketch_color.put("W00", color(#f3f3eb));  sketch_name.put("W00", "Warm Gray");
    sketch_color.put("W0", color(#ecece4));  sketch_name.put("W0", "Warm Gray");
    sketch_color.put("W10", color(#302f2b));  sketch_name.put("W10", "Warm Gray");
    sketch_color.put("W1", color(#e7e7df));  sketch_name.put("W1", "Warm Gray No. 1");
    sketch_color.put("W2", color(#ddddd5));  sketch_name.put("W2", "Warm Gray");
    sketch_color.put("W3", color(#d2d2ca));  sketch_name.put("W3", "Warm Gray No. 3");
    sketch_color.put("W4", color(#bcbdb7));  sketch_name.put("W4", "Warm Gray");
    sketch_color.put("W5", color(#a8a9a4));  sketch_name.put("W5", "Warm Gray No. 5");
    sketch_color.put("W6", color(#94958f));  sketch_name.put("W6", "Warm Gray");
    sketch_color.put("W7", color(#777873));  sketch_name.put("W7", "Warm Gray No. 7");
    sketch_color.put("W8", color(#63645f));  sketch_name.put("W8", "Warm Gray");
    sketch_color.put("W9", color(#4c4d48));  sketch_name.put("W9", "Warm Gray");
    sketch_color.put("Y0000", color(#fefef4));  sketch_name.put("Y0000", "Yellow Fluorite");
    sketch_color.put("Y000", color(#fffce9));  sketch_name.put("Y000", "Pale Lemon");
    sketch_color.put("Y00", color(#fefddf));  sketch_name.put("Y00", "Barium Yellow");
    sketch_color.put("Y02", color(#f6f396));  sketch_name.put("Y02", "Canary Yellow");
    sketch_color.put("Y04", color(#ede556));  sketch_name.put("Y04", "Acacia");
    sketch_color.put("Y06", color(#fef56c));  sketch_name.put("Y06", "Yellow");
    sketch_color.put("Y08", color(#fef200));  sketch_name.put("Y08", "Acid Yellow");
    sketch_color.put("Y11", color(#fffbcc));  sketch_name.put("Y11", "Pale Yellow");
    sketch_color.put("Y13", color(#fbf7ae));  sketch_name.put("Y13", "Lemon Yellow");
    sketch_color.put("Y15", color(#fee96c));  sketch_name.put("Y15", "Cadmium Yellow");
    sketch_color.put("Y17", color(#ffe455));  sketch_name.put("Y17", "Golden Yellow");
    sketch_color.put("Y18", color(#feed55));  sketch_name.put("Y18", "Lightning Yellow");
    sketch_color.put("Y19", color(#ffe93e));  sketch_name.put("Y19", "Napoli Yellow");
    sketch_color.put("Y21", color(#ffeec2));  sketch_name.put("Y21", "Buttercup Yellow");
    sketch_color.put("Y23", color(#fbe3b3));  sketch_name.put("Y23", "Yellowish Beige");
    sketch_color.put("Y26", color(#f0dd67));  sketch_name.put("Y26", "Mustard");
    sketch_color.put("Y28", color(#caa869));  sketch_name.put("Y28", "Lionet Gold");
    sketch_color.put("Y32", color(#f9dec0));  sketch_name.put("Y32", "Cashmere");
    sketch_color.put("Y35", color(#ffd879));  sketch_name.put("Y35", "Maize");
    sketch_color.put("Y38", color(#ffd374));  sketch_name.put("Y38", "Honey");
    sketch_color.put("YG0000", color(#f2f7e0));  sketch_name.put("YG0000", "Lily White");
    sketch_color.put("YG00", color(#e6e69e));  sketch_name.put("YG00", "Mimosa Yellow");
    sketch_color.put("YG01", color(#e2ebb2));  sketch_name.put("YG01", "Green Bice");
    sketch_color.put("YG03", color(#deeaaa));  sketch_name.put("YG03", "Yellow Green");
    sketch_color.put("YG05", color(#d6e592));  sketch_name.put("YG05", "Salad");
    sketch_color.put("YG06", color(#c4df92));  sketch_name.put("YG06", "Yellowish Green");
    sketch_color.put("YG07", color(#a5cf4f));  sketch_name.put("YG07", "Acid Green");
    sketch_color.put("YG09", color(#82c566));  sketch_name.put("YG09", "Lettuce Green");
    sketch_color.put("YG11", color(#e5f0d0));  sketch_name.put("YG11", "Mignonette");
    sketch_color.put("YG13", color(#d4e59f));  sketch_name.put("YG13", "Chartreuse");
    sketch_color.put("YG17", color(#72c156));  sketch_name.put("YG17", "Grass Green");
    sketch_color.put("YG21", color(#f7f6be));  sketch_name.put("YG21", "Anise");
    sketch_color.put("YG23", color(#e6eb8f));  sketch_name.put("YG23", "New Leaf");
    sketch_color.put("YG25", color(#d0e17b));  sketch_name.put("YG25", "Celadon Green");
    sketch_color.put("YG41", color(#d5ebd4));  sketch_name.put("YG41", "Pale Cobalt Green");
    sketch_color.put("YG45", color(#b4dcb7));  sketch_name.put("YG45", "Cobalt Green");
    sketch_color.put("YG61", color(#d6e9d6));  sketch_name.put("YG61", "Pale Moss");
    sketch_color.put("YG63", color(#a0caa2));  sketch_name.put("YG63", "Pea Green");
    sketch_color.put("YG67", color(#81bf8c));  sketch_name.put("YG67", "Moss");
    sketch_color.put("YG91", color(#dad7ae));  sketch_name.put("YG91", "Putty");
    sketch_color.put("YG93", color(#d2d29c));  sketch_name.put("YG93", "Grayish Yellow");
    sketch_color.put("YG95", color(#cbc65e));  sketch_name.put("YG95", "Pale Olive");
    sketch_color.put("YG97", color(#958f03));  sketch_name.put("YG97", "Spanish Olive");
    sketch_color.put("YG99", color(#4e6a15));  sketch_name.put("YG99", "Marine Green");
    sketch_color.put("YR0000", color(#fff3e5));  sketch_name.put("YR0000", "Pale Chiffon");
    sketch_color.put("YR000", color(#feecd8));  sketch_name.put("YR000", "Silk");
    sketch_color.put("YR00", color(#fed6bd));  sketch_name.put("YR00", "Powder Pink");
    sketch_color.put("YR01", color(#fedac2));  sketch_name.put("YR01", "Peach Puff");
    sketch_color.put("YR02", color(#fcdcc5));  sketch_name.put("YR02", "Light Orange");
    sketch_color.put("YR04", color(#fec369));  sketch_name.put("YR04", "Chrome Orange");
    sketch_color.put("YR07", color(#f26f39));  sketch_name.put("YR07", "Cadmium Orange");
    sketch_color.put("YR09", color(#f15524));  sketch_name.put("YR09", "Chinese Orange");
    sketch_color.put("YR12", color(#ffe2a6));  sketch_name.put("YR12", "Loquat");
    sketch_color.put("YR14", color(#fec84e));  sketch_name.put("YR14", "Caramel");
    sketch_color.put("YR15", color(#fbb884));  sketch_name.put("YR15", "Pumpkin Yellow");
    sketch_color.put("YR16", color(#feb729));  sketch_name.put("YR16", "Apricot");
    sketch_color.put("YR18", color(#f26b3c));  sketch_name.put("YR18", "Sanguine");
    sketch_color.put("YR20", color(#ffe1bf));  sketch_name.put("YR20", "Yellowish Shade");
    sketch_color.put("YR21", color(#f5ddb1));  sketch_name.put("YR21", "Cream");
    sketch_color.put("YR23", color(#eccf8b));  sketch_name.put("YR23", "Yellow Ochre");
    sketch_color.put("YR24", color(#f0cf64));  sketch_name.put("YR24", "Pale Sepia");
    sketch_color.put("YR27", color(#d56638));  sketch_name.put("YR27", "Tuscan Orange");
    sketch_color.put("YR30", color(#fef2da));  sketch_name.put("YR30", "Macadamia Nut");
    sketch_color.put("YR31", color(#ffdea8));  sketch_name.put("YR31", "Light Reddish Yellow");
    sketch_color.put("YR61", color(#fddac4));  sketch_name.put("YR61", "Spring Orange");
    sketch_color.put("YR65", color(#faae60));  sketch_name.put("YR65", "Atoll");
    sketch_color.put("YR68", color(#f37022));  sketch_name.put("YR68", "Orange");
    sketch_color.put("YR82", color(#fdc68d));  sketch_name.put("YR82", "Mellow Peach");

    original_color.put("0", color(#ffffff));  original_name.put("0", "Colorless Blender");
    original_color.put("100", color(#312b2b));  original_name.put("100", "Black");
    original_color.put("110", color(#030708));  original_name.put("110", "Special Black");
    original_color.put("B00", color(#ddf0f4));  original_name.put("B00", "Frost Blue");
    original_color.put("B01", color(#d6eef2));  original_name.put("B01", "Mint Blue");
    original_color.put("B02", color(#b3e3f1));  original_name.put("B02", "Robin&#39;s Egg Blue");
    original_color.put("B04", color(#73cfe6));  original_name.put("B04", "Tahitian Blue");
    original_color.put("B05", color(#40c5e6));  original_name.put("B05", "Process Blue");
    original_color.put("B06", color(#00b3e6));  original_name.put("B06", "Peacok Blue");
    original_color.put("B12", color(#c8e6f0));  original_name.put("B12", "Ice Blue");
    original_color.put("B14", color(#71cfeb));  original_name.put("B14", "Light Blue");
    original_color.put("B16", color(#00bcea));  original_name.put("B16", "Cyanine Blue");
    original_color.put("B18", color(#1d8acb));  original_name.put("B18", "Lapis Lazuli");
    original_color.put("B21", color(#dbedf9));  original_name.put("B21", "Baby Blue");
    original_color.put("B23", color(#92c2e8));  original_name.put("B23", "Phthalo Blue");
    original_color.put("B24", color(#8acef3));  original_name.put("B24", "Sky");
    original_color.put("B26", color(#65b3e3));  original_name.put("B26", "Cobalt Blue");
    original_color.put("B29", color(#0177c1));  original_name.put("B29", "Ultramarine");
    original_color.put("B32", color(#e2eff7));  original_name.put("B32", "Pale Blue");
    original_color.put("B34", color(#82c3ed));  original_name.put("B34", "Manganese Blue");
    original_color.put("B37", color(#156fa4));  original_name.put("B37", "Antwerp Blue");
    original_color.put("B39", color(#2b64a9));  original_name.put("B39", "Prussian Blue");
    original_color.put("B41", color(#e2f0fb));  original_name.put("B41", "Powder Blue");
    original_color.put("B45", color(#75c0ea));  original_name.put("B45", "Smoky Blue");
    original_color.put("BG02", color(#c6e8ea));  original_name.put("BG02", "New Blue");
    original_color.put("BG05", color(#83d2e1));  original_name.put("BG05", "Holiday Blue");
    original_color.put("BG09", color(#01b1c9));  original_name.put("BG09", "Blue Green");
    original_color.put("BG10", color(#dcf0ef));  original_name.put("BG10", "Cool Shadow");
    original_color.put("BG11", color(#ceebf1));  original_name.put("BG11", "Moon White");
    original_color.put("BG13", color(#c4e7e9));  original_name.put("BG13", "Mint Green");
    original_color.put("BG15", color(#a0d9d2));  original_name.put("BG15", "Aqua");
    original_color.put("BG18", color(#37c0b0));  original_name.put("BG18", "Teal Blue");
    original_color.put("BG32", color(#bce2d7));  original_name.put("BG32", "Aqua Mint");
    original_color.put("BG34", color(#a3dad7));  original_name.put("BG34", "Horizon Green");
    original_color.put("BG45", color(#afdfdf));  original_name.put("BG45", "Nile Blue");
    original_color.put("BG49", color(#00b6b9));  original_name.put("BG49", "Duck Blue");
    original_color.put("BG99", color(#6e9b87));  original_name.put("BG99", "Flagstone Blue");
    original_color.put("BV00", color(#e0dced));  original_name.put("BV00", "Mauve Shadow");
    original_color.put("BV04", color(#7c97ce));  original_name.put("BV04", "Blue Berry");
    original_color.put("BV08", color(#9d7eb9));  original_name.put("BV08", "Blue Violet");
    original_color.put("BV23", color(#b1c0dd));  original_name.put("BV23", "Grayish Lavender");
    original_color.put("BV31", color(#eae7f2));  original_name.put("BV31", "Pale Lavender");
    original_color.put("C0", color(#e0e7ed));  original_name.put("C0", "Cool Gray");
    original_color.put("C10", color(#202b31));  original_name.put("C10", "Cool Gray");
    original_color.put("C1", color(#dae3e8));  original_name.put("C1", "Cool Gray No. 1");
    original_color.put("C2", color(#ccd7dd));  original_name.put("C2", "Cool Gray");
    original_color.put("C3", color(#c1ccd2));  original_name.put("C3", "Cool Gray No. 3");
    original_color.put("C4", color(#a6b4bd));  original_name.put("C4", "Cool Gray");
    original_color.put("C5", color(#92a0ab));  original_name.put("C5", "Cool Gray No. 5");
    original_color.put("C6", color(#7b8c96));  original_name.put("C6", "Cool Gray");
    original_color.put("C7", color(#637079));  original_name.put("C7", "Cool Gray No. 7");
    original_color.put("C8", color(#535d66));  original_name.put("C8", "Cool Gray");
    original_color.put("C9", color(#3c474d));  original_name.put("C9", "Cool Gray");
    original_color.put("E00", color(#fdf3ea));  original_name.put("E00", "Cotton Pearl");
    original_color.put("E02", color(#feece0));  original_name.put("E02", "Fruit Pink");
    original_color.put("E04", color(#e4bcc4));  original_name.put("E04", "Lipstick Natural");
    original_color.put("E07", color(#cc816a));  original_name.put("E07", "Light Mahogany");
    original_color.put("E09", color(#d96a4f));  original_name.put("E09", "Burnt Sienna");
    original_color.put("E11", color(#fee9d6));  original_name.put("E11", "Barley Beige");
    original_color.put("E13", color(#e9c5af));  original_name.put("E13", "Light Suntan");
    original_color.put("E15", color(#fbbb8d));  original_name.put("E15", "Dark Suntan");
    original_color.put("E19", color(#c45238));  original_name.put("E19", "Redwood");
    original_color.put("E21", color(#fde2c7));  original_name.put("E21", "Soft Sun");
    original_color.put("E25", color(#d2a482));  original_name.put("E25", "Caribe Cocoa");
    original_color.put("E27", color(#997663));  original_name.put("E27", "Milk Chocolate");
    original_color.put("E29", color(#884636));  original_name.put("E29", "Burnt Umber");
    original_color.put("E31", color(#f2e6ce));  original_name.put("E31", "Brick Beige");
    original_color.put("E33", color(#f3d2b1));  original_name.put("E33", "Sand");
    original_color.put("E34", color(#f0caa6));  original_name.put("E34", "Toast");
    original_color.put("E35", color(#e6c3a3));  original_name.put("E35", "Chamois");
    original_color.put("E37", color(#cc9159));  original_name.put("E37", "Sepia");
    original_color.put("E39", color(#c5743f));  original_name.put("E39", "Leather");
    original_color.put("E40", color(#f2e8dc));  original_name.put("E40", "Brick White");
    original_color.put("E41", color(#fef1e1));  original_name.put("E41", "Pearl White");
    original_color.put("E43", color(#e8dabd));  original_name.put("E43", "Dull Ivory");
    original_color.put("E44", color(#c5b9a9));  original_name.put("E44", "Clay");
    original_color.put("E49", color(#634c3c));  original_name.put("E49", "Dark Bark");
    original_color.put("E51", color(#feecd6));  original_name.put("E51", "Milky White");
    original_color.put("E53", color(#f3e6c3));  original_name.put("E53", "Raw Silk");
    original_color.put("E55", color(#f1dfb9));  original_name.put("E55", "Light Camel");
    original_color.put("E57", color(#b18558));  original_name.put("E57", "Light Walnut");
    original_color.put("E59", color(#9a7f6c));  original_name.put("E59", "Walnut");
    original_color.put("E77", color(#7f604e));  original_name.put("E77", "Maroon");
    original_color.put("G00", color(#e3f2ed));  original_name.put("G00", "Jade Green");
    original_color.put("G02", color(#cfe8d3));  original_name.put("G02", "Spectrum Green");
    original_color.put("G05", color(#69c07b));  original_name.put("G05", "Emerald Green");
    original_color.put("G07", color(#7bc576));  original_name.put("G07", "Nile Green");
    original_color.put("G09", color(#7ac465));  original_name.put("G09", "Veronese Green");
    original_color.put("G12", color(#d2e8c4));  original_name.put("G12", "Sea Green");
    original_color.put("G14", color(#97cf90));  original_name.put("G14", "Apple Green");
    original_color.put("G16", color(#60c198));  original_name.put("G16", "Malachite");
    original_color.put("G17", color(#14b37d));  original_name.put("G17", "Forest Green");
    original_color.put("G19", color(#2db98a));  original_name.put("G19", "Bright Parrot Green");
    original_color.put("G20", color(#edf6db));  original_name.put("G20", "Wax White");
    original_color.put("G21", color(#c4e4cd));  original_name.put("G21", "Lime Green");
    original_color.put("G24", color(#c3e0b4));  original_name.put("G24", "Willow");
    original_color.put("G28", color(#119462));  original_name.put("G28", "Ocean Green");
    original_color.put("G29", color(#197c5d));  original_name.put("G29", "Pine Tree Green");
    original_color.put("G40", color(#e4f1df));  original_name.put("G40", "Dim Green");
    original_color.put("G82", color(#ccdab9));  original_name.put("G82", "Spring Dim Green");
    original_color.put("G85", color(#9dc3aa));  original_name.put("G85", "Verdigris");
    original_color.put("G99", color(#5f7e3a));  original_name.put("G99", "Olive");
    original_color.put("N0", color(#eceeed));  original_name.put("N0", "Neutral Gray");
    original_color.put("N10", color(#312f30));  original_name.put("N10", "Neutral Gray");
    original_color.put("N1", color(#e2e3e5));  original_name.put("N1", "Neutral Gray");
    original_color.put("N2", color(#dadbdd));  original_name.put("N2", "Neutral Gray");
    original_color.put("N3", color(#d1d2d4));  original_name.put("N3", "Neutral Gray");
    original_color.put("N4", color(#bcbdc1));  original_name.put("N4", "Neutral Gray");
    original_color.put("N5", color(#a8a9ad));  original_name.put("N5", "Neutral Gray");
    original_color.put("N6", color(#949599));  original_name.put("N6", "Neutral Gray");
    original_color.put("N7", color(#77787c));  original_name.put("N7", "Neutral Gray");
    original_color.put("N8", color(#636466));  original_name.put("N8", "Neutral Gray");
    original_color.put("N9", color(#4c4d4f));  original_name.put("N9", "Neutral Gray");
    original_color.put("R00", color(#feeae1));  original_name.put("R00", "Pinkish White");
    original_color.put("R02", color(#fdd3c7));  original_name.put("R02", "Rose Salmon");
    original_color.put("R05", color(#f6927b));  original_name.put("R05", "Salmon Red");
    original_color.put("R08", color(#f26754));  original_name.put("R08", "Vermilion");
    original_color.put("R11", color(#fde1d5));  original_name.put("R11", "Pale Cherry Pink");
    original_color.put("R17", color(#f4846c));  original_name.put("R17", "Lipstick Orange");
    original_color.put("R20", color(#fcd7cf));  original_name.put("R20", "Blush");
    original_color.put("R24", color(#f27579));  original_name.put("R24", "Prawn");
    original_color.put("R27", color(#f15062));  original_name.put("R27", "Cadmium Red");
    original_color.put("R29", color(#ed174b));  original_name.put("R29", "Lipstick Red");
    original_color.put("R32", color(#fac1ba));  original_name.put("R32", "Peach");
    original_color.put("R35", color(#f27185));  original_name.put("R35", "Coral");
    original_color.put("R37", color(#e86c74));  original_name.put("R37", "Carmine");
    original_color.put("R39", color(#cb487a));  original_name.put("R39", "Garnet");
    original_color.put("R59", color(#b74f70));  original_name.put("R59", "Cardinal");
    original_color.put("RV02", color(#fad5e6));  original_name.put("RV02", "Sugared Almond Pink");
    original_color.put("RV04", color(#f6a3bf));  original_name.put("RV04", "Shock  Pink");
    original_color.put("RV06", color(#f386af));  original_name.put("RV06", "Cerise");
    original_color.put("RV09", color(#e171ac));  original_name.put("RV09", "Fuchsia");
    original_color.put("RV10", color(#fdecf4));  original_name.put("RV10", "Pale Pink");
    original_color.put("RV11", color(#fbd6dd));  original_name.put("RV11", "Pink");
    original_color.put("RV13", color(#f9c9d7));  original_name.put("RV13", "Tender Pink");
    original_color.put("RV14", color(#f495b7));  original_name.put("RV14", "Begonia Pink");
    original_color.put("RV17", color(#db7eb3));  original_name.put("RV17", "Deep Magenta");
    original_color.put("RV19", color(#d268aa));  original_name.put("RV19", "Red Violet");
    original_color.put("RV21", color(#fde8e7));  original_name.put("RV21", "Light Pink");
    original_color.put("RV25", color(#f493be));  original_name.put("RV25", "Dog Rose Flower");
    original_color.put("RV29", color(#ef4880));  original_name.put("RV29", "Crimson");
    original_color.put("RV32", color(#fad3ce));  original_name.put("RV32", "Shadow Pink");
    original_color.put("RV34", color(#f9afae));  original_name.put("RV34", "Dark Pink");
    original_color.put("T0", color(#eceeed));  original_name.put("T0", "Toner Gray");
    original_color.put("T10", color(#322e2d));  original_name.put("T10", "Toner Gray");
    original_color.put("T1", color(#eaeae8));  original_name.put("T1", "Toner Gray");
    original_color.put("T2", color(#e0e0de));  original_name.put("T2", "Toner Gray");
    original_color.put("T3", color(#d1d2cc));  original_name.put("T3", "Toner Gray");
    original_color.put("T4", color(#bcbbb9));  original_name.put("T4", "Toner Gray");
    original_color.put("T5", color(#a8a7a3));  original_name.put("T5", "Toner Gray");
    original_color.put("T6", color(#949590));  original_name.put("T6", "Toner Gray");
    original_color.put("T7", color(#777674));  original_name.put("T7", "Toner Gray");
    original_color.put("T8", color(#63645f));  original_name.put("T8", "Toner Gray");
    original_color.put("T9", color(#4c4b49));  original_name.put("T9", "Toner Gray");
    original_color.put("V04", color(#e6aace));  original_name.put("V04", "Lilac");
    original_color.put("V06", color(#ce95c2));  original_name.put("V06", "Lavender");
    original_color.put("V09", color(#8754a1));  original_name.put("V09", "Violet");
    original_color.put("V12", color(#eed7e9));  original_name.put("V12", "Pale Lilac");
    original_color.put("V15", color(#d3a6cd));  original_name.put("V15", "Mallow");
    original_color.put("V17", color(#a092c7));  original_name.put("V17", "Amethyst");
    original_color.put("W0", color(#ecece4));  original_name.put("W0", "Warm Gray");
    original_color.put("W10", color(#302f2b));  original_name.put("W10", "Warm Gray");
    original_color.put("W1", color(#e7e7df));  original_name.put("W1", "Warm Gray No. 1");
    original_color.put("W2", color(#ddddd5));  original_name.put("W2", "Warm Gray");
    original_color.put("W3", color(#d2d2ca));  original_name.put("W3", "Warm Gray No. 3");
    original_color.put("W4", color(#bcbdb7));  original_name.put("W4", "Warm Gray");
    original_color.put("W5", color(#a8a9a4));  original_name.put("W5", "Warm Gray No. 5");
    original_color.put("W6", color(#94958f));  original_name.put("W6", "Warm Gray");
    original_color.put("W7", color(#777873));  original_name.put("W7", "Warm Gray No. 7");
    original_color.put("W8", color(#63645f));  original_name.put("W8", "Warm Gray");
    original_color.put("W9", color(#4c4d48));  original_name.put("W9", "Warm Gray");
    original_color.put("Y00", color(#fefddf));  original_name.put("Y00", "Barium Yellow");
    original_color.put("Y02", color(#f6f396));  original_name.put("Y02", "Canary Yellow");
    original_color.put("Y06", color(#fef56c));  original_name.put("Y06", "Yellow");
    original_color.put("Y08", color(#fef200));  original_name.put("Y08", "Acid Yellow");
    original_color.put("Y11", color(#fffbcc));  original_name.put("Y11", "Pale Yellow");
    original_color.put("Y13", color(#fbf7ae));  original_name.put("Y13", "Lemon Yellow");
    original_color.put("Y15", color(#fee96c));  original_name.put("Y15", "Cadmium Yellow");
    original_color.put("Y17", color(#ffe455));  original_name.put("Y17", "Golden Yellow");
    original_color.put("Y19", color(#ffe93e));  original_name.put("Y19", "Napoli Yellow");
    original_color.put("Y21", color(#ffeec2));  original_name.put("Y21", "Buttercup Yellow");
    original_color.put("Y23", color(#fbe3b3));  original_name.put("Y23", "Yellowish Beige");
    original_color.put("Y26", color(#f0dd67));  original_name.put("Y26", "Mustard");
    original_color.put("Y38", color(#ffd374));  original_name.put("Y38", "Honey");
    original_color.put("YG01", color(#e2ebb2));  original_name.put("YG01", "Green Bice");
    original_color.put("YG03", color(#deeaaa));  original_name.put("YG03", "Yellow Green");
    original_color.put("YG05", color(#d6e592));  original_name.put("YG05", "Salad");
    original_color.put("YG07", color(#a5cf4f));  original_name.put("YG07", "Acid Green");
    original_color.put("YG09", color(#82c566));  original_name.put("YG09", "Lettuce Green");
    original_color.put("YG11", color(#e5f0d0));  original_name.put("YG11", "Mignonette");
    original_color.put("YG13", color(#d4e59f));  original_name.put("YG13", "Chartreuse");
    original_color.put("YG17", color(#72c156));  original_name.put("YG17", "Grass Green");
    original_color.put("YG21", color(#f7f6be));  original_name.put("YG21", "Anise");
    original_color.put("YG23", color(#e6eb8f));  original_name.put("YG23", "New Leaf");
    original_color.put("YG25", color(#d0e17b));  original_name.put("YG25", "Celadon Green");
    original_color.put("YG41", color(#d5ebd4));  original_name.put("YG41", "Pale Cobalt Green");
    original_color.put("YG45", color(#b4dcb7));  original_name.put("YG45", "Cobalt Green");
    original_color.put("YG63", color(#a0caa2));  original_name.put("YG63", "Pea Green");
    original_color.put("YG67", color(#81bf8c));  original_name.put("YG67", "Moss");
    original_color.put("YG91", color(#dad7ae));  original_name.put("YG91", "Putty");
    original_color.put("YG95", color(#cbc65e));  original_name.put("YG95", "Pale Olive");
    original_color.put("YG97", color(#958f03));  original_name.put("YG97", "Spanish Olive");
    original_color.put("YG99", color(#4e6a15));  original_name.put("YG99", "Marine Green");
    original_color.put("YR00", color(#fed6bd));  original_name.put("YR00", "Powder Pink");
    original_color.put("YR02", color(#fcdcc5));  original_name.put("YR02", "Light Orange");
    original_color.put("YR04", color(#fec369));  original_name.put("YR04", "Chrome Orange");
    original_color.put("YR07", color(#f26f39));  original_name.put("YR07", "Cadmium Orange");
    original_color.put("YR09", color(#f15524));  original_name.put("YR09", "Chinese Orange");
    original_color.put("YR14", color(#fec84e));  original_name.put("YR14", "Caramel");
    original_color.put("YR16", color(#feb729));  original_name.put("YR16", "Apricot");
    original_color.put("YR18", color(#f26b3c));  original_name.put("YR18", "Sanguine");
    original_color.put("YR21", color(#f5ddb1));  original_name.put("YR21", "Cream");
    original_color.put("YR23", color(#eccf8b));  original_name.put("YR23", "Yellow Ochre");
    original_color.put("YR24", color(#f0cf64));  original_name.put("YR24", "Pale Sepia");
  }
  
  color get_sketch_color(String pen) {
    return sketch_color.get(pen);
  }

  color get_original_color(String pen) {
    return original_color.get(pen);
  }

  String get_sketch_name(String pen) {
    return sketch_name.get(pen);
  }

  String get_original_name(String pen) {
    return original_name.get(pen);
  }

  String get_closest_original(color c1) {
    //http://stackoverflow.com/questions/1847092/given-an-rgb-value-what-would-be-the-best-way-to-find-the-closest-match-in-the-d
    //https://en.wikipedia.org/wiki/Color_difference
    
    float r1 = red(c1);
    float g1 = green(c1);
    float b1 = blue(c1);
    
    float closest_value = 99999999999999999999999999.0;
    String closest_pen = "";
    
    for (Map.Entry me : original_color.entrySet()) {
      //println(me.getKey() + " is " + me.getValue());
    
      color c2 = (color)me.getValue();
      float r2 = red(c2);
      float g2 = green(c2);
      float b2 = blue(c2);
    
      float d = sq((r2-r1)*0.30) + sq((g2-g1)*0.59) + sq((b2-b1)*0.11);
      if (d<closest_value) {
        closest_value = d;
        closest_pen = (String)me.getKey();
      }
    }
    return closest_pen;
  }
  
  String get_closest_sketch(color c1) {
    //http://stackoverflow.com/questions/1847092/given-an-rgb-value-what-would-be-the-best-way-to-find-the-closest-match-in-the-d
    //https://en.wikipedia.org/wiki/Color_difference
    
    float r1 = red(c1);
    float g1 = green(c1);
    float b1 = blue(c1);
    
    float closest_value = 99999999999999999999999999.0;
    String closest_pen = "";
    
    for (Map.Entry me : sketch_color.entrySet()) {
      //println(me.getKey() + " is " + me.getValue());
    
      color c2 = (color)me.getValue();
      float r2 = red(c2);
      float g2 = green(c2);
      float b2 = blue(c2);
    
      float d = sq(abs((r2-r1))*0.30) + sq(abs((g2-g1))*0.59) + sq(abs((b2-b1))*0.11);
      //float d = sq((r2-r1)*0.30) + sq((g2-g1)*0.59) + sq((b2-b1)*0.11);
      if (d<closest_value) {
        closest_value = d;
        closest_pen = (String)me.getKey();
      }
    }
    return closest_pen;
  }
  
}

///////////////////////////////////////////////////////////////////////////////////////////////////////