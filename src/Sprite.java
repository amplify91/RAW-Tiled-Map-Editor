import java.awt.Graphics;

/**
 * all types of sprites extend this interface. This is
 * necessary to be able to render them easily.
 *
 * All sprites also have a logic() method. this is used by AI
 * to update positions, or simply might check a value on the 
 * controller or keyboard and move the character...
 */
public interface Sprite
{
	final static float RIGHT       = (float) (0 * (2 * Math.PI) / 8);
	final static float DOWN_RIGHT  = (float) (1 * (2 * Math.PI) / 8);
	final static float DOWN        = (float) (2 * (2 * Math.PI) / 8);
	final static float DOWN_LEFT   = (float) (3 * (2 * Math.PI) / 8);
	final static float LEFT        = (float) (4 * (2 * Math.PI) / 8);
	final static float UP_LEFT     = (float) (5 * (2 * Math.PI) / 8);
	final static float UP          = (float) (6 * (2 * Math.PI) / 8);
	final static float UP_RIGHT    = (float) (7 * (2 * Math.PI) / 8);
	
	/**
	 * render this sprite to the graphics.
	 */
	public void render(Graphics g, Camera c);
	
	public float getX();
	public float getY();
	
	/**
	 * override this method to update AI. Only use this method,
	 * as the render method may be called even when the game is
	 * paused.
	 */
	public void logic();
}