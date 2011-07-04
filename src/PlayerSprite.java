import java.awt.*;

/**
 * all types of sprites extend this interface. This is
 * necessary to be able to render them easily.
 *
 * All sprites also have a logic() method. this is used by AI
 * to update positions, or simply might check a value on the 
 * controller or keyboard and move the character...
 */
public class PlayerSprite implements Sprite
{
	float facingAngle;
	float animDist;
	
	float x, y;
	
	/**
	 * render this sprite to the graphics.
	 */
	public void render(Graphics g, int offsetX, int offsetY) {
		g.setColor(Color.BLACK);
		g.fillRect((int)(x-5) - offsetX, (int)(y-5) - offsetY, 100, 100);
		
	}
	
	public void render(Graphics g, Camera c) {
		render(g, (int)c.viewx - c.viewWidth/2, (int)c.viewy - c.viewWidth/2);
	}
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void move(float angle, float amount) {
		x += Math.cos(angle) * amount;
		y += Math.sin(angle) * amount;
		facingAngle = angle;
		animDist += amount;
	}
	
	
	
	
	
	/**
	 * override this method to update AI. Only use this method,
	 * as the render method may be called even when the game is
	 * paused.
	 */
	public void logic() {
		
		
	}
}