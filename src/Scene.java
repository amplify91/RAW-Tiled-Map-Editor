import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * this class was originally meant to simply hold a reference to a map
 * and a collection of sprites, which it delegated all the work to in the
 * logic and render methods of these objects...<p>
 *
 * so it is reasonably fitting that this class also manage the loading of the
 * maps and sprites it uses from files or URLs.<p>
 *
 * Each scene consists of a Map, and a collection of sprites, as intended, 
 * but may also be loaded or saved.
 */
public class Scene
{
	int offsetX = 0;
	int offsetY = 0;
	
	float effect_rScale = 1;
	float effect_gScale = 1;
	float effect_bScale = 1;
	float effect_hue = 0;
	float effect_sat = 1;
	
	Map map;
	ArrayList sprites;
	
	GraphicsBank tileset;
	
	/**
	 * creates a scene using the given map and sprites.
	 */
	public Scene(Map m, ArrayList s, GraphicsBank gfx)
	{
		this.map = m;
		sprites = s;
		this.tileset = gfx;
		
	}
	
	/* Create a new empty scene */
  public Scene() {
    map    = new Map(10, 10, 32, 32);
    tileset = new GraphicsBank();
  }
	
	
	public GraphicsBank getTileset()
	{
		return tileset;
		
	}
	
	public void setTileset(GraphicsBank gfx) {
		tileset = gfx;
		map.setTileset(gfx);
	}
	
	/**
	 * loads a scene from the given URL. takes tiles from the given GraphicsBank.
	 */
	static Scene loadScene(File f) throws IOException
	{
		boolean hasColourEffect = false;
		float r = 1;
		float g = 1;
		float b = 1;
		float h = 0;
		float s = 1;
	
		BufferedReader reader = new BufferedReader(new FileReader(f));
		
		String line = reader.readLine();
		
		StringTokenizer tokens = new StringTokenizer(line);
		int width = Integer.parseInt(tokens.nextToken());
		int height = Integer.parseInt(tokens.nextToken());
		
		String tileset = tokens.nextToken();
		
		GraphicsBank gfx = new GraphicsBank();
		
		System.out.println("Attempt to load tileset "+tileset);
		
		System.out.println("Working path is "+f.getParentFile());
		
		File ts = new File(f.getParentFile(), tileset);
		System.out.println("Attempt to load tileset "+ts.getAbsoluteFile());
		
		
		gfx.loadTileset(ts);
		
		Map map = new Map(width, height);
		
		line = reader.readLine();
		tokens = new StringTokenizer(line);
		
		if(tokens.nextToken().equalsIgnoreCase("colorization")) {
			hasColourEffect = true;
			r = Float.parseFloat(tokens.nextToken());
			g = Float.parseFloat(tokens.nextToken());
			b = Float.parseFloat(tokens.nextToken());
			h = Float.parseFloat(tokens.nextToken());
			s = Float.parseFloat(tokens.nextToken());
		}
		
		while(! line.equals("."))
		{
			line = reader.readLine();
		}
		
		
		for(int z=0; z<3; z++)
		{
			line = reader.readLine();
			tokens = new StringTokenizer(line);
			
			for(int y=0; y<height; y++)
			{
				for(int x=0; x<width; x++)
				{
					String code = tokens.nextToken();
					map.setTile(x, y, z, gfx.getTile(Integer.parseInt(code)));
				}
			}
		}
		reader.close();
		
		Scene scene = new Scene(map, new ArrayList(), gfx);
		scene.tileset = gfx;
		if(hasColourEffect) {
			System.out.println("Calling setEffect on scene recently loaded.");
			scene.setEffect(r, g, b, h, s, 1f);
		}
		return scene;
		
	}
	
	
    static Scene loadScene(String filename) throws IOException {
      Scene scene = loadScene(new File(filename));
      return scene;
    }
	/**
	 * writes the map only (at the moment) to a file.
	 */
  public void saveScene(File file)
  {
    if(tileset.isUnsaved()) {
      throw new RuntimeException("Tileset is unsaved. Cannot save the scene");
    }
		
		try
		{
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			
			String line = "";
			
			int width = map.getWidth();
			int height = map.getHeight();
			
			
      
      File wd = new File(file.getParentFile().getCanonicalFile().toString());
			File ts = new File(tileset.getFile().getCanonicalFile().toString());
			
			String relativePath = RelativePath.getRelativePath(wd, ts);
			

			
      line = width + " " + height + " " + relativePath;
			writer.println(line);
			
			line = "colorization " + effect_rScale +
			                   " " + effect_gScale +
			                   " " + effect_bScale +
			                   " " + effect_hue    +
			                   " " + effect_sat;
			writer.println(line);

			System.out.println("Colorization red in save is "+effect_rScale);			
			writer.println(".");
			
			for(int z=0; z<3; z++)
			{
				for(int i=0; i<height; i++)
				{
					for(int j=0; j<width; j++)
					{
						Tile t = map.getTile(j, i, z);
						if(t != null)
							writer.print(t.getNumber()+ " ");
						else writer.print("0 ");
						
						
					}
					
				}
				writer.println();
			}
			
			writer.flush();
			writer.close();
			
			
		}
		catch(IOException e)
		{
			throw new RuntimeException("Could not save the level");
		}
		
		System.err.println("Saved");
	}
	
	/**
	 * calls each sprites logic method.
	 */
	void logic()
	{
		for(int i=0; i<sprites.size(); i++)
		{
			Sprite s = (Sprite)sprites.get(i);
			s.logic();
		}
	}
	
	/**
	 * renders the scene to the graphics context.
	 * at the moment, sprites appear above everything else.
	 *
	 * TODO: Fix this up.
	 */
	void render(Graphics g)
	{
		//System.out.println("Render Scene");
		map.render(g, offsetX, offsetY);
		
		for(int i=0; i<sprites.size(); i++)
		{
			Sprite s = (Sprite)sprites.get(i);
			//s.render(g, offsetX, offsetY);
		}
		
	}
	public void render(Graphics g, int offX, int offY)
	{
		map.render(g, offX, offY);
	}
	public void render(Graphics g, Camera c)
	{
		map.render(g, c);
	}
	
	
	public void render(Graphics g, Point origin, Dimension size)
	{
		map.render(g, origin, size);
	}
	
	public void render(Graphics g, Point origin, Dimension size, int layer)
	{
		map.render(g, origin, size, layer);
	}
	
	/**
	 * sets the visible area of the scene.
	 * This will probably not be used once the game is finished.
	 */
	void setViewSize(int width, int height)
	{
		map.setViewSize(width, height);
		
	}
	
	/**
	 * sets the screen offset. Screen offset can be anywhere, not just
	 * following a character around. This is good for cut scenes.
	 */
	void setOffset(int x, int y)
	{
		offsetX = x;
		offsetY = y;
	}
	
	/**
	 * Apply RGB scalars and hue/sat adjustments to the tiles and characters
	 * in the scene. The hue and sat are always applied first
	 **/
	public void setEffect(float r, float g, float b, float h, float s, float z) {
		System.out.println("Scene setEffect called. will call for the gfx bank...r" +r+" g"+g+" b"+b+" z"+z);
		effect_rScale = r;
		effect_gScale = g;
		effect_bScale = b;
		effect_hue = h;
		effect_sat = s;
		tileset.setEffect(r, g, b, h, s, z);
		map.setZoom(z);
	}
	
	/**
	 * returns the map. not really any reason to want to do this except for the
	 * level editor.
	 */
	public Map getMap()
	{
		return map;
	}
}