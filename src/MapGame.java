import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;


public class MapGame implements KeyListener {
	
	static Scene scene;
	static JFrame mainFrame;
	static PlayerSprite player;
	static Camera cam;
	static BufferStrategy strat;
	static long time;
	
	boolean leftPressed = false;
	boolean rightPressed = false;
	boolean upPressed = false;
	boolean downPressed = false;
	
	
	
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				leftPressed = true;
				break;
			case KeyEvent.VK_RIGHT:
				rightPressed = true;
				break;
			case KeyEvent.VK_UP:
				upPressed = true;
				break;
			case KeyEvent.VK_DOWN:
				downPressed = true;
				break;
			default:
				break;
		}
	}
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				leftPressed = false;
				break;
			case KeyEvent.VK_RIGHT:
				rightPressed = false;
				break;
			case KeyEvent.VK_UP:
				upPressed = false;
				break;
			case KeyEvent.VK_DOWN:
				downPressed = false;
				break;
			default:
				break;
		}}
	
	
	public static void main(String[] a) throws Exception {
		
		mainFrame = new JFrame("MapGame");
		mainFrame.setSize(800, 600);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		mainFrame.setVisible(true);
		Thread.sleep(50);
		mainFrame.createBufferStrategy(2);
		strat = mainFrame.getBufferStrategy();
		
		
		scene = Scene.loadScene("scenes/mySceneTest.dat");
		cam = new Camera();
		player = new PlayerSprite();
		
		cam.trackSprite(player);
		
		int iter = 0;
		
		while(true) {
			time = System.currentTimeMillis();
			
			if(!strat.contentsLost()) { 
				Graphics g = strat.getDrawGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, mainFrame.getWidth(), mainFrame.getHeight());
				
				cam.logic();
				scene.render(g, cam);
				player.render(g, cam);
				
				strat.show();
				g.dispose();
				
				
				player.move(Sprite.DOWN_LEFT, (float) (Math.random() * 10));
					
				System.out.print(".");
				if(iter++ % 100 == 0) {
					cam.setViewSize(mainFrame.getWidth() + 100, mainFrame.getHeight() + 100);
				}
			} else {
				System.out.print("!");
				mainFrame.createBufferStrategy(2);
				strat = mainFrame.getBufferStrategy();
			}
			Thread.sleep(15);
		}
		
	}
}