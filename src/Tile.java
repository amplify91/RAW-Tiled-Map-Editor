import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * a Tile is simply a square of land or object on a map.
 * Tiles have an image, a name, and a type. They also have a number.
 */
public class Tile
{
  /* Allow global disable of colorization effects on tiles */
  public static boolean effects_enabled = true;
  
  BufferedImage effectImage = null;
  float effect_rScale;
  float effect_gScale;
  float effect_bScale;
  float effect_zoom;
  float effect_hue;
  float effect_sat;
  
  private int imageWidth = 0;
  private int imageHeight = 0;
  int zoomWidth, zoomHeight;
  
  String name = null;
  int number = -1;
  String type = null;
  String path = null;
  String info = null;
  
  Image image = null;
  
  public Tile(int number, String path, String name, String type)
  {
    this.type = type;
    this.number = number;
    this.name = name;
    this.path = path;
      //System.out.println("load image" + path);
    this.image = new ImageIcon(path).getImage();
    
    if(image == null) {
      throw new RuntimeException("Could not load image" + path);
    }
    
    imageWidth = image.getWidth(null);
    imageHeight = image.getHeight(null);
    zoomWidth = imageWidth;
    zoomHeight = imageHeight;
    
    //System.out.println(imageWidth);
  }
  
  public Tile(int number, String path, String name, String type, String info)
  {
    this(number, path, name, type);
    if(info.indexOf(',') >= 0) {
      throw new RuntimeException("Info string cannot contain \",\" characters");
    }
    this.info = info;
  }
  /**
   * Creates a shallow copy of the given tile.
   * It shouldn't really be necessary to reproduce a tile, as
   * the same tile may appear more than once in a map without
   * causing any trouble.
   **/
  public Tile(Tile t)
  {
    System.err.println("WARNING: Creating shallow copy of tile");
    
    this.number = t.number;
    this.type = t.type;
    this.name = t.name;
    this.path = t.path;
    this.image = t.image;
  }
  
  String getImageLocation() {
    return path;
  }
  
  /**
   * Compare tiles. if the number is the same,
   * return true. else return false.
   **/
  public boolean equals(Tile t) {
    if(t == null) return false;
    if(this.number == t.number/* &&
       this.name.equals(t.name) &&
       this.type == t.type &&
       this.image == t.image*/) {
        return true;
    } else {
      return false;
    }
  }
  
  
  static boolean areEqual(Tile t1, Tile t2) {
    if(t1 == null && t2 == null) {
      return true;
    } else if (t1 != null) {
      return t1.equals(t2);
    } else {
      return false;
    }
  }
  
  /**
   * creates a null tile. this tile does not render itself,
   * and has type of 99999, and name "noname".
   */
  public Tile()
  {
    image = null;
  }
  
  /**
   * returns the Image that is rendered by this tile.
   **/
  public Image getImage()
  {
    return image;
  }
  /**
   *returns the type of tile.
   */
  public String getType()
  {
    return type;
  }
  /**
   *returns the tile's number.
   */
  public int getNumber()
  {
    return number;
  }
  /**
   * returns the tile's name.
   */
  public String getName()
  {
    return name;
  }
  
  public String getInfo() {
    return info;
  }
  public String getPath() {
    return path;
  }
  
  /**
   * Copies a fresh effect image, which is a zoomed version
   * from the original.
   **/
  void initEffectImage()
  {
    //System.out.println("Zoom is: " + effect_zoom);
    effectImage = new BufferedImage((int)(imageWidth * effect_zoom), (int)(imageHeight * effect_zoom), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = (Graphics2D)effectImage.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(image, 0, 0, (int)(imageWidth * effect_zoom), (int)(imageHeight * effect_zoom), null, (ImageObserver)null);
    //effectImage.getGraphics().drawImage(image, 0, 0, (ImageObserver)null);
  }
  
  /**
   * Colourizes and zooms the image
   **/
  void adjustRGBHS(float rScale, float gScale, float bScale,
                   float hueOffset, float satScale, float zoom)
  {
    if(effects_enabled) {
      
      if(zoom   == effect_zoom &&
         rScale == effect_rScale &&
         gScale == effect_gScale &&
         bScale == effect_bScale) {
          return;
      }
      
      effect_zoom = zoom;
      zoomWidth  = (int)(imageWidth  * zoom);
      zoomHeight = (int)(imageHeight * zoom);
      
      initEffectImage();
      
      
      float[] hsbVals = new float[3];
      Color rgbVals;
      
      WritableRaster raster = effectImage.getRaster();
      
      int pixelData[] = new int[(zoomWidth * zoomHeight * 4)];
    
      pixelData = raster.getPixels(0, 0, zoomWidth, zoomHeight, pixelData);
      
      
      for(int i = 0; i < pixelData.length / 4; i++) {
        
        if(hueOffset != 0 || satScale != 1) {
          hsbVals = Color.RGBtoHSB(pixelData[i * 4], pixelData[i * 4 + 1], pixelData[i * 4 + 2], hsbVals);
          hsbVals[0] += hueOffset;
          hsbVals[1] *= satScale;
          if(hsbVals[1] > 1) {
            hsbVals[1] = 1;
          }
          rgbVals = new Color(Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]));
          pixelData[i * 4 + 0] = rgbVals.getRed();
          pixelData[i * 4 + 1] = rgbVals.getGreen();
          pixelData[i * 4 + 2] = rgbVals.getBlue();
        }
        
        pixelData[i * 4] = (int)(pixelData[i * 4] * rScale);
        if(pixelData[i * 4] > 255) {
          pixelData[i * 4] = 255;
        }
        pixelData[i * 4 + 1] = (int)(pixelData[i * 4 + 1] * gScale);
        if(pixelData[i * 4 + 1] > 255) {
          pixelData[i * 4 + 1] = 255;
        }
        
        pixelData[i * 4 + 2] = (int)(pixelData[i * 4 + 2] * bScale);
        if(pixelData[i * 4 + 2] > 255) {
          pixelData[i * 4 + 2] = 255;
        }
        
      }
      
      raster.setPixels(0, 0, zoomWidth, zoomHeight, pixelData);
      
    }
  }
  
  
  /**
   * renders the tile's image if it exists.
   */
  public void render(Graphics g, int x, int y)
  {
    if(effects_enabled && effectImage != null) {
      g.drawImage(effectImage, x-zoomWidth, y-zoomHeight, null);
    } else if(image != null) {
      g.drawImage(image, x-imageWidth, y-imageHeight, null);
    }
  }
  
}