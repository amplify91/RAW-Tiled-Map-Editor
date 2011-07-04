import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

/**
 * a JComponent view of the map. Supports setting tiles on a map.
 */
public class MapComponent extends JComponent implements MouseListener, MouseMotionListener, MapChangeListener
{
	Map map;
	MapEdit mapEdit;
	
	private int width = 0;
	private int height = 0;
	private int tileWidth = 0;
	private int tileHeight = 0;
	
	private int activeLayer = 0;
	boolean hideLayers = false;
	boolean showGrid = true;
	boolean stateChanged = false;
	
	Stack undoStack;
	Stack redoStack;
	
	int grabX = 0;
	int grabY = 0;
	boolean dragged = false;
	
	int offsetX = 0;
	int offsetY = 0;
	
	JViewport viewport;
	
	public MapComponent(Map m, MapEdit me)
	{
		this.map = m;
		this.mapEdit = me;
		width = m.getWidth();
		height = m.getHeight();
		
		tileWidth = m.getZoomWidth();
		tileHeight = m.getZoomHeight();
		
		setPreferredSize(new Dimension(tileWidth*width, tileHeight*height));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		undoStack = new Stack();
		redoStack = new Stack();
		stateChanged = true;
	}
	
	public void setViewport(JViewport vp)
	{
		this.viewport = vp;
	}
	synchronized public void setMap(Map m)
	{
		this.map = m;
		
		width = m.getWidth();
		height = m.getHeight();
		
		tileWidth = m.getZoomWidth();
		tileHeight = m.getZoomHeight();
		
		setPreferredSize(new Dimension(tileWidth*width, tileHeight*height));
		revalidate();
		undoStack.clear();
		redoStack.clear();
		stateChanged = true;
	}
	
	void refreshZoom() {
		tileWidth = map.getZoomWidth();
		tileHeight = map.getZoomHeight();
		setPreferredSize(new Dimension(tileWidth*width, tileHeight*height));
		revalidate();
		repaint();
	}
	
	/**
	 * paints this component... basically renders the visible portion of the map
	 * onto the given graphics context.
	 * also draws a grid...
	 */
	synchronized public void paintComponent(Graphics g)
	{
		g.setColor(Color.white);
		g.fillRect(0,0,width*tileWidth, height*tileHeight);
		
		//as the tiles are drawn with the origin at the
		//bottom right, but the component's origin is the top left,
		//we need to set the offset so we can see the tiles
		if(hideLayers)
		{
			map.render(g, viewport.getViewPosition(), viewport.getSize(), activeLayer);
		}
		else
		{
			map.render(g, viewport.getViewPosition(), viewport.getSize());
		}
		
		//map.render(g, -tileWidth, -tileHeight);
		if(showGrid)
		{
			g.setColor(Color.gray);
			for(int i=0; i<width; i++)
			{
				g.drawLine(i*tileWidth, 0, i*tileWidth, height*tileHeight);
			}
			
			for(int j=0; j<height; j++)
			{
				g.drawLine(0,j*tileHeight, width*tileWidth, j*tileHeight);
			}
		}
		((Graphics2D)g).setStroke(new BasicStroke(2));
		g.setColor(Color.black);
		g.drawLine(0, 0, width * tileWidth, 0);
		g.drawLine(0, 0, 0, height * tileHeight);
		g.drawLine(width * tileWidth, 0, width * tileWidth, height * tileHeight);
		g.drawLine(0, height * tileHeight, width * tileWidth, height * tileHeight);
		
	}
	
	/**
	 * change the given tile to the one selected in the map editor.
	 */
	public void mapClicked(int x, int y)
	{
		x = x/tileWidth;
		y = y/tileHeight;
		if(x < map.getWidth() && x >= 0
		&& y < map.getHeight() && y >= 0) {
			
			if(mapEdit.getPaintMode() == MapEdit.PAINT_NORMAL) {
				map.setTile(x, y, activeLayer, mapEdit.getSelectedTile());
				stateChanged = true;
			} else if(mapEdit.getPaintMode() == MapEdit.PAINT_FILL) {
				recursiveFlood(x, y, activeLayer, map.getTile(x, y, activeLayer), mapEdit.getSelectedTile());
			} else {
				System.out.println("Invalid paint mode");
			}
		}
	}
	
	
	
	/* Flood fill operation from http://en.wikipedia.org/wiki/Flood_fill
	 * The section used is as follows:
	 
	 Most practical implementations use a loop for the west and east
	 directions as an optimization to avoid the overhead of stack or
	 queue management:
	 
		 1. Set Q to the empty queue.
		 2. If the color of node is not equal to target-color, return.
		 3. Add node to Q.
		 4. For each element n of Q:
		 5.  If the color of n is equal to target-color:
		 6.   Set w and e equal to n.
		 7.   Move w to the west until the color of the node to the west of w no longer matches target-color.
		 8.   Move e to the east until the color of the node to the east of e no longer matches target-color.
		 9.   Set the color of nodes between w and e to replacement-color.
		10.   For each node n between w and e:
		11.    If the color of the node to the north of n is target-color, add that node to Q.
		       If the color of the node to the south of n is target-color, add that node to Q.
		12. Continue looping until Q is exhausted.
		13. Return.
	*/
	public void recursiveFlood(int x, int y, int layer, Tile target, Tile replacement) {
		if(x < 0 || x > map.getWidth() - 1 ||
		   y < 0 || y > map.getHeight() - 1) {
		   	return;
		}
		
		Tile node = map.getTile(x, y, layer);
		
		//1. If the color of node is not equal to target-color, return.
		if(Tile.areEqual(node, replacement) || !Tile.areEqual(node, target)) {
			return;
		}
		
		stateChanged = true;
		map.setTile(x, y, layer, replacement);
		
		/* new version. see method comment for description */
		int left = x - 1;;
		int right = x + 1;;
		
		while(left >= 0 && Tile.areEqual(map.getTile(left, y, layer), target)) {
			map.setTile(left, y, layer, replacement);
			left -= 1;
		}
		while(right < map.getWidth() && Tile.areEqual(map.getTile(right, y, layer), target)) {
			map.setTile(right, y, layer, replacement);
			right += 1;
		}
		/* step back off the walls we have hit */
		left++;
		right--;
		
		/* recursively do this operation above and below. Not so bad as the other one.
		 * the deepest recursion level reachable is the map height. not great either. */
		for(int i = left; i <= right; i++) {
			recursiveFlood(i, y-1, layer, target, replacement);
			recursiveFlood(i, y+1, layer, target, replacement);
		}
	}
	
	
	
	public void setActiveLayer(int layer)
	{
		if (layer >= 0 && layer <3)
		{
			activeLayer = layer;
		}
	}
	public int getActiveLayer()
	{
		return activeLayer;
	}
	
	boolean btn1Pressed = false;
	boolean btn2Pressed = false;
	int oldX = 0;
	int oldY = 0;
	public void mousePressed(MouseEvent e)
	{
		if(stateChanged) {
			saveUndoState();
			stateChanged = false;
		}
		switch(e.getButton())
		{
			case MouseEvent.BUTTON1: btn1Pressed = true;
				mapClicked(e.getX(), e.getY());
				this.repaint();
				break;
				/*
			default:
				btn2Pressed = true;
				Dimension d = viewport.getSize();
				Point newPoint = new Point((int)(e.getX() - d.getWidth()/2), (int)(e.getY() - d.getHeight()/2));
				viewport.setViewPosition(newPoint);
				viewport.repaint();
				break;
				*/
				
				
			default:
				btn2Pressed = true;
				
				
				grabX = e.getX();
				grabY = e.getY();
				System.out.println("Grab at "+grabX+", "+grabY);
				
				
				break;
		}
	}
	public void mouseReleased(MouseEvent e)
	{
		switch(e.getButton())
		{
			case MouseEvent.BUTTON1:
				btn1Pressed = false;
				oldX = e.getX();
				oldY = e.getY();
			break;
			default:
			  btn2Pressed = false;
				oldX = e.getX();
				oldY = e.getY();
				
				if(!dragged) {
					Dimension d = viewport.getSize();
					Point newPoint = new Point((int)(e.getX() - d.getWidth()/2), (int)(e.getY() - d.getHeight()/2));
					viewport.setViewPosition(newPoint);
				}
				dragged = false;
			break;
		}
	}
	public void mouseEntered(MouseEvent e)
	{
	}
	public void mouseDragged(MouseEvent e)
	{
		if(btn1Pressed && mapEdit.getPaintMode() != MapEdit.PAINT_FILL)
		{
			mapClicked(e.getX(), e.getY());
			//System.out.println(mapEdit.getSelectedTile());
			this.repaint();
		}
		else if(btn2Pressed)
		{
			int offX = e.getX() - grabX;
			int offY = e.getY() - grabY;
			Dimension d = viewport.getSize();
			Point p = viewport.getViewPosition();
			Point newPoint = new Point(p.x - offX, p.y - offY);
			viewport.setViewPosition(newPoint);
			
			dragged = true;
		}
	}
	public void mouseExited(MouseEvent e)
	{
		
	}
	public void mouseClicked(MouseEvent e)
	{
		
	}
	public void mouseMoved(MouseEvent e)
	{
		
	}
	
	public void setGrid(boolean grid)
	{
		showGrid = grid;
		
	}
	
	public void setHideLayers(boolean hl)
	{
		hideLayers = hl;
	}
	
	
	public void mapChanging(boolean major) {
		if(!major) {
			saveUndoState();
		} else {
			clearUndoInfo();
		}
	}
	
	public void mapChanged(boolean major) {
		repaint();
	}
	
	
	
	/********
	 * Undo *
	 ********/
	
	public void clearUndoInfo() {
		redoStack.clear();
		undoStack.clear();
	}
	void saveUndoState()
	{
		redoStack.clear();
		undoStack.push(map.toIntArray());
	}
	void undo() {
		if(!undoStack.empty()) {
			redoStack.push(map.toIntArray());
			int[][][] i = (int[][][])undoStack.pop();
			map.setAllTiles(i, mapEdit.scene.tileset);
		}
	}
	void redo() {
		if(!redoStack.empty()) {
			undoStack.push(map.toIntArray());
			int[][][] i = (int[][][])redoStack.pop();
			map.setAllTiles(i, mapEdit.scene.tileset);
		}
	}
	
}