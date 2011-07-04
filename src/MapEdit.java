import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.beans.*;

/**
 * map editor. This class may be quite hacky. <p>
 * Map editor is curently able to do one layer maps.
 * I intend to extend this to three-layer maps, and
 * to introduce some more interesting tiles to the set.
 */
public class MapEdit implements ActionListener, ChangeListener, KeyListener
{
	boolean compactToolbars = true;
	boolean borderedButtons = true;
	
	public static final int PAINT_NORMAL = 0;
	public static final int PAINT_FILL   = 1;
	
	JFrame       mainFrame;     // The window.
	MapComponent mapPanel;      // Special panel for rendering map with a viewport.
	JFileChooser chooser;       // For save and load alike
	JFileChooser tschooser;     // For tilesets
	TileChooser  tileChooser;
	JSplitPane   split;         // provides the movable divider next to tile chooser
	JScrollPane  mapScroll;     // ScrollPane for the Map
	JPanel       chooserPanel;  // The panel the tilechooser goes in.
	JPanel       settingsPanel;
	JPanel       tilesetSettingsPanel;
	JPanel       colorDialog;
	JSlider      r, g, b, h, s; // Red, green, blue, hue, saturation
	boolean ignoreEffects = false;
	
	File         openFile;      // Currently open file.
	Map          map;           // Map. The focus of this program.
	Scene        scene;         // Scene in which the map is found
	GraphicsBank gfx;           // Graphics bank used by this scene. All the tiles.
	
	
	/* Toolbar buttons, self-explanatory */
	JToolBar      outerToolBar;
	JToolBar      innerToolBar;
	JButton       newBtn;
	JButton       openBtn;
	JButton       saveBtn;
	JButton       clearBtn;
	JToggleButton layerButtons[];
	JToggleButton hideBtn;
	JToggleButton gridBtn;
	JButton       shiftRightBtn;
	JButton       shiftLeftBtn;
	JButton       shiftUpBtn;
	JButton       shiftDownBtn;
	JButton       increaseWidthBtn;
	JButton       decreaseWidthBtn;
	JButton       increaseHeightBtn;
	JButton       decreaseHeightBtn;
	JToggleButton palletteBtn;
	
	/* Second toolbar buttons */
	JButton       zoomInBtn;
	JButton       zoomOutBtn;
	JButton       zoomFullBtn;
	JToggleButton fillBtn;
	JButton       undoBtn;
	JButton       redoBtn;
	float         zoomLevel;
	
	/* Menu bar */
	JMenuBar  menuBar;
	JMenu     fileMenu;
	JMenu     editMenu;
	JMenu     toolMenu;
	JMenu     helpMenu;
	JMenuItem undoMI;
	JMenuItem redoMI;
	JMenuItem openMI;
	JMenuItem newMI;
	JMenuItem saveMI;
	JMenuItem saveAsMI;
	JMenuItem exitMI;
	JMenuItem about;
	JMenuItem howToUse;
	
	/* tileset settings */
	JPanel tilesetInfoPane;
  JLabel tilesetFileLabel;
  JButton tilesetOpenBtn;
  JButton tilesetNewBtn;
  JButton tilesetSaveBtn;
  JSpinner tilesetGridWField;
  JSpinner tilesetGridHField;
	
	/* for the dialog */
	JButton effectsResetBtn;
	
	
	
	
	public MapEdit()
	{
		//gfx = new GraphicsBank();
		zoomLevel = 1;
		openFile = null;
		try {
		  scene = Scene.loadScene("lastOpenScene.dat");
		} catch(IOException e) {
		  scene = new Scene();
		}
		
		map = scene.getMap();
		gfx = scene.getTileset();
		/* so that the window contents resize while you drag. */
		Toolkit.getDefaultToolkit().setDynamicLayout(true); 
		
		mainFrame = new JFrame();
		mainFrame.setTitle("Map Editor by Judd");
		
		tileChooser = new TileChooser(gfx, mainFrame);
		chooser = new JFileChooser("scenes");
		tschooser = new JFileChooser("gfx");
		
		/* outer-most containers actually reserved for docking the toolbars.
		 * so "cp" is actually not the contentpane of the JPanel, but let's
		 * ignore that. */
		JPanel outerToolPane = (JPanel)mainFrame.getContentPane();
		JPanel innerToolPane = new JPanel(new BorderLayout());
		JPanel cp = new JPanel(new BorderLayout());
		
		outerToolPane.setLayout(new BorderLayout());
		outerToolPane.add(innerToolPane, BorderLayout.CENTER);
		innerToolPane.add(cp, BorderLayout.CENTER);
		
		colorDialog = createColorDialog();
		
		setupMenus();
		
		setupToolbars();
		
		cp.setLayout(new BorderLayout());
		
		/* Toolbar placement */
		innerToolPane.add(innerToolBar, BorderLayout.NORTH);
		outerToolPane.add(outerToolBar, BorderLayout.NORTH);
		
		
		
		
		
		/* TileChooser placement */
		chooserPanel = new JPanel(new BorderLayout());
		
	  //chooserPanel.setBorder(new TitledBorder("chooserPanel"));
		JScrollPane tileScroll = new JScrollPane(tileChooser);
		chooserPanel.add(tileScroll, BorderLayout.CENTER);
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.add("Tiles", chooserPanel);
		
		tilesetFileLabel = new JLabel("Tileset: * unsaved *");
		tilesetInfoPane = new JPanel(new BorderLayout());
	  //tilesetInfoPane.setBorder(new TitledBorder("tilesetInfoPane"));
		JPanel tilesetInfoBtnPane = new JPanel(new FlowLayout());
	  //tilesetInfoBtnPane.setBorder(new TitledBorder("tilesetInfoBtnPane"));
		tilesetInfoPane.add(tilesetInfoBtnPane, BorderLayout.CENTER);
		chooserPanel.add(tilesetInfoPane, BorderLayout.SOUTH);
		tilesetInfoPane.add(tilesetFileLabel, BorderLayout.NORTH);
		
		tilesetOpenBtn = makeBtn("Open", "icons/opents.gif", "Load Tileset");
		tilesetNewBtn  = makeBtn("New",  "icons/newts.gif",  "New Tileset");
		tilesetSaveBtn = makeBtn("Save", "icons/savets.gif", "Save Tileset");
		tilesetInfoBtnPane.add(tilesetOpenBtn);
		tilesetInfoBtnPane.add(tilesetNewBtn);
		tilesetInfoBtnPane.add(tilesetSaveBtn);
		
		
		/* Settings panel */
		settingsPanel = new JPanel(new BorderLayout());
	  settingsPanel.setBorder(new TitledBorder("Settings"));
	  settingsPanel.add(colorDialog, BorderLayout.CENTER);
    
    
    
    
    
		tabPane.add("Settings", settingsPanel);
		
		
		/* Scrollable map panel creation and placement */
		mapPanel = new MapComponent(map, this);
		
		
		mapScroll = new JScrollPane(mapPanel);
		mapPanel.setViewport(mapScroll.getViewport());
		
		/* Divider between map panel and tile chooser */
		split = new JSplitPane();
		split.setDividerLocation(250);
		split.setLeftComponent(tabPane);
		split.setRightComponent(mapScroll);
		cp.add(split, BorderLayout.CENTER);
		/* NOTE: Creation of an anonymous inner class */
		split.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				tileChooser.setWidth(split.getDividerLocation());
			}
		});
		
		/* Sizing and positioning the window, general JFrame setup */
		mainFrame.setSize(new Dimension(830, 650));
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/* Positioning of the colour chooser dialog */
		//colorDialog.setLocationRelativeTo(mainFrame);
		
		
		/* Continuous repaint every second. This should be eliminated.
		 * If it is even necessary at all, it's only due to bugs. */
		while(true)
		{
			mapPanel.repaint();
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
				System.out.println("wow... an interrupted exception. Never seen one before.");
			}
		}
	}
	
	
	/**
	 * Create all the menu items
	 **/
	private void setupMenus()
	{
		menuBar  = new JMenuBar();
		fileMenu = new JMenu("File");
		toolMenu = new JMenu("Tools");
		editMenu = new JMenu("Edit");
		helpMenu = new JMenu("Help");
		openMI   = new JMenuItem("Open...");
		newMI    = new JMenuItem("New");
		saveMI   = new JMenuItem("Save");
		saveAsMI = new JMenuItem("Save As...");
		exitMI   = new JMenuItem("Exit");
		undoMI   = new JMenuItem("Undo");
		redoMI   = new JMenuItem("Redo");
		about    = new JMenuItem("About...(N/A)");
		howToUse = new JMenuItem("How to use LevelEdit (N/A)");
		
		  openMI.addActionListener(this);
		   newMI.addActionListener(this);
		  saveMI.addActionListener(this);
		saveAsMI.addActionListener(this);
		  exitMI.addActionListener(this);
	    undoMI.addActionListener(this);
		  redoMI.addActionListener(this);
		   about.addActionListener(this);
		howToUse.addActionListener(this);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		
		fileMenu.add(openMI);
		fileMenu.add(newMI);
		fileMenu.add(saveMI);
		fileMenu.add(saveAsMI);
		fileMenu.add(exitMI);
		editMenu.add(undoMI);
		editMenu.add(redoMI);
		helpMenu.add(about);
		helpMenu.add(howToUse);
		mainFrame.setJMenuBar(menuBar);
	}
	
	
	
	class undoAction extends AbstractAction
	{
		public void actionPerformed( ActionEvent ae ) {
			System.out.println( "CTRL Z pressed" );
			undoBtn.doClick();
		}
	}
	
	class redoAction extends AbstractAction
	{
		public void actionPerformed( ActionEvent ae ) {
			System.out.println( "CTRL Y pressed" );
			redoBtn.doClick();
		}
	}
	
	
	/**
	 * Create both toolbars and
	 **/
	private void setupToolbars()
	{
		outerToolBar = new JToolBar();
		innerToolBar = new JToolBar();
		
		/* Map file buttons */
		saveBtn  = makeBtn("Save",    "icons/save.gif",  "Save map");
		openBtn  = makeBtn("Open...", "icons/open.gif",  "Open map...");
		newBtn   = makeBtn("New",     "icons/new.gif",   "New map");
		clearBtn = makeBtn("Clear",   "icons/clear.gif", "Reset map (Delete all tiles)");
		
		/* Layer buttons. */
		ButtonGroup layerGroup = new ButtonGroup();
		layerButtons = new JToggleButton[Map.LAYERS];
		layerButtons[2] = makeToggleBtn("Layer 3", "icons/top.gif",    "Edit the top layer");
		layerButtons[1] = makeToggleBtn("Layer 2", "icons/mid.gif",    "Edit the middle layer");
		layerButtons[0] = makeToggleBtn("Layer 1", "icons/bottom.gif", "Edit the bottom layer");
		layerGroup.add(layerButtons[0]);
		layerGroup.add(layerButtons[1]);
		layerGroup.add(layerButtons[2]);
		
		/* Visual buttons */
		gridBtn     = makeToggleBtn("Grid",              "icons/grid.gif",    "Show/Hide Grid");
		hideBtn     = makeToggleBtn("Hide other layers", "icons/hideoth.gif", "Hide other layers");
		/*
		palletteBtn = makeToggleBtn("Colours", "icons/pallette.png",
			"Adjust Hue, Saturation, and RGB channels of the tileset");
		*/
		
		zoomInBtn   = makeBtn("Zoom in",   "icons/zoomin.gif",   "Zoom in");
		zoomFullBtn = makeBtn("Zoom 100%", "icons/zoomfull.gif", "Zoom to 100%");
		zoomOutBtn  = makeBtn("Zoom out",  "icons/zoomout.gif",  "Zoom out");
		
		/* One-shot map manipulation buttons */
		shiftRightBtn = makeBtn("->",  "icons/shiftRight.gif", "Move tiles right");
		shiftLeftBtn  = makeBtn("<-",  "icons/shiftLeft.gif",  "Move tiles left");
		shiftUpBtn    = makeBtn("^",   "icons/shiftUp.gif",    "Move tiles up");
		shiftDownBtn  = makeBtn("\\/", "icons/shiftDown.gif",  "Move tiles down");
		increaseWidthBtn  = makeBtn("<- ->", "icons/increaseWidth.gif",  "Increase field width");
		decreaseWidthBtn  = makeBtn("-> <-", "icons/decreaseWidth.gif",  "Decrease field width");
		increaseHeightBtn = makeBtn("\\/ +", "icons/increaseHeight.gif", "Increase field height");
		decreaseHeightBtn = makeBtn("^^ -",  "icons/decreaseHeight.gif", "Decrease field height");
		
		
		/* Other map manipulation buttons */
		fillBtn = makeToggleBtn("Flood Fill", "icons/fill.gif", "Flood fill mode");
		undoBtn = makeBtn("Undo", "icons/undo.gif", "Undo");
		redoBtn = makeBtn("Redo", "icons/redo.gif", "Redo");
		
		undoMI.setAccelerator(KeyStroke.getKeyStroke(new Character('Z'), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		redoMI.setAccelerator(KeyStroke.getKeyStroke(new Character('Y'), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		
		
		
		((JPanel)mainFrame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK ), "Undo" );
		((JPanel)mainFrame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK ), "Redo" );
		((JPanel)mainFrame.getContentPane()).getActionMap().put("Undo", new undoAction());
		((JPanel)mainFrame.getContentPane()).getActionMap().put("Redo", new redoAction());
		
		
		
		outerToolBar.add(newBtn);
		outerToolBar.add(openBtn);
		outerToolBar.add(saveBtn);
		outerToolBar.add(clearBtn);
		
		outerToolBar.addSeparator();
		outerToolBar.add(undoBtn);
		outerToolBar.add(redoBtn);
		outerToolBar.addSeparator();
		outerToolBar.add(fillBtn);
		
		outerToolBar.addSeparator();
		outerToolBar.add(layerButtons[2]);
		outerToolBar.add(layerButtons[1]);
		outerToolBar.add(layerButtons[0]);
		
		outerToolBar.addSeparator();
		outerToolBar.add(hideBtn);
		outerToolBar.add(gridBtn);
		
		outerToolBar.addSeparator();
		outerToolBar.add(zoomInBtn);
		outerToolBar.add(zoomFullBtn);
		outerToolBar.add(zoomOutBtn);
		
		
		//outerToolBar.addSeparator();
		//outerToolBar.add(palletteBtn);
		
		/* quick hack */
		
		if(compactToolbars) {
			innerToolBar = outerToolBar;
			outerToolBar.addSeparator();
		}
		
		innerToolBar.add(shiftLeftBtn);
		innerToolBar.add(shiftRightBtn);
		innerToolBar.add(shiftUpBtn);
		innerToolBar.add(shiftDownBtn);
		
		innerToolBar.addSeparator();
		innerToolBar.add(increaseWidthBtn);
		innerToolBar.add(decreaseWidthBtn);
		innerToolBar.add(increaseHeightBtn);
		innerToolBar.add(decreaseHeightBtn);
		
		innerToolBar.addSeparator();
		
		gridBtn.setSelected(true);
		layerButtons[0].setSelected(true);
	}
	
	
	public void keyPressed(KeyEvent e) {
		System.out.println("pressed");
	}
	public void keyReleased(KeyEvent e) {
		
		System.out.println("released");
	}
	public void keyTyped(KeyEvent e) {
		System.out.println("typed");
		
	}
	
	
	/* This method now handles all buttons rather than
	 * using anonymous inner classes to do it. */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		/*******************
		 * Toolbar buttons *
		 *******************/
		if(source == zoomInBtn) {
			/* Actually, we could support any max zoom level we like, but let's be sensible */
			if(zoomLevel < 5) {
				zoomLevel *= 1.2;
			}
		} else if (source == zoomOutBtn) {
			if(zoomLevel > 0.05) {
				zoomLevel *= .8;
			}
		} else if (source == zoomFullBtn) {
			zoomLevel = 1;
		} else if (source == undoBtn || source == undoMI) {
			mapPanel.undo();
			mapPanel.repaint();
		} else if (source == redoBtn || source == redoMI) {
			mapPanel.redo();
			mapPanel.repaint();
		} else if (source == openBtn || source == openMI) {
			int success = chooser.showOpenDialog(mainFrame);
			if (success == JFileChooser.APPROVE_OPTION)
			openFile(chooser.getSelectedFile());
		} else if (source == saveBtn || source == saveMI) {
			if (openFile == null) {
				/* File's never been saved, use save as instead.*/
				actionPerformed(new ActionEvent(saveAsMI, e.getID(), e.getActionCommand()));
			}
			else saveFile(openFile);
		} else if (source == newBtn || source == newMI) {
			newFile();
			openFile = null;
		} else if (source == clearBtn) {
			map.clear();
			mapPanel.repaint();
		} else if (source == layerButtons[0]) {
			mapPanel.setActiveLayer(0);
		} else if (source == layerButtons[1]) {
			mapPanel.setActiveLayer(1);
		} else if (source == layerButtons[2]) {
			mapPanel.setActiveLayer(2);
		} else if (source == hideBtn) {
			mapPanel.setHideLayers(hideBtn.isSelected());
			mapPanel.repaint();
		} else if (source == gridBtn) {
			mapPanel.setGrid(gridBtn.isSelected());
			mapPanel.repaint();
		} else if (source == shiftRightBtn) {
			map.shift(1, 0);
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == shiftLeftBtn) {
			map.shift(-1, 0);
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == shiftUpBtn) {
			map.shift(0, -1);
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == shiftDownBtn) {
			map.shift(0, 1);
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == increaseWidthBtn) {
			map.resize(map.getWidth() + 1, map.getHeight());
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == decreaseWidthBtn) {
			map.resize(map.getWidth() - 1, map.getHeight());
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == increaseHeightBtn) {
			map.resize(map.getWidth(), map.getHeight() + 1);
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == decreaseHeightBtn) {
			map.resize(map.getWidth(), map.getHeight() - 1);
			mapPanel.setMap(map);
			mapPanel.repaint();
		} else if (source == palletteBtn) {
			colorDialog.setVisible(!colorDialog.isVisible());
			palletteBtn.setSelected(colorDialog.isVisible());
		} 
		
		/************************************
		 * Reset button in pallette control *
		 ************************************/
		else if (source == effectsResetBtn) {
				setIgnoreEffectChanges(true);
				r.setValue(100);
				g.setValue(100);
				b.setValue(100);
				h.setValue(0);
				s.setValue(100);
				setIgnoreEffectChanges(false);
				
		}
		
		/**************
		 * Menu Items *
		 **************/
		else if (source == saveAsMI) {
			int success = chooser.showSaveDialog(mainFrame);
			if (success == JFileChooser.APPROVE_OPTION) {
				saveFile(chooser.getSelectedFile());
			}
		} else if (source == exitMI) {
			mainFrame.dispose(); /* TODO: "Do you want to save?" */
			System.exit(0);
		}
		
		
		
		
		/*********************
		 * Tiles tab buttons *
		 *********************/
		else if (source == tilesetNewBtn) {
			newTileset();
		} else if (source == tilesetOpenBtn) {
			openTileset();
		} else if (source == tilesetSaveBtn) {
			saveTileset();
		}
		
		
		
		/*********************
		 * None of the above *
		 *********************/
		
		else {
			System.err.println("Unknown source of actionEvent. (The button you just clicked does nothing)");
		}
		/* Multiple buttons may fire the following code */
		
		if(source == zoomInBtn || source == zoomOutBtn || source == zoomFullBtn || source == effectsResetBtn) {
			scene.setEffect(r.getValue() / 100f,
                g.getValue() / 100f,
                b.getValue() / 100f,
                h.getValue() / 360f,
                s.getValue() / 100f,
                zoomLevel);
      map.setZoom(zoomLevel);
			mapPanel.refreshZoom();
		}
	}
	
	/**
	 * Makes a JButton with the given icon and tooltop.
	 * If the icon cannot be loaded, then the text will be used instead.
	 *
	 * Adds this mapeditor as an actionListener.
	 *
	 * @return a shiny new JButton
	 **/
	JButton makeBtn(String text, String icon, String tooltip) {
		JButton newBtn;
		try {
			newBtn = new JButton(new ImageIcon(getClass().getResource(icon)));
		} catch (Exception e) {
			newBtn = new JButton(text);
		}
		newBtn.setToolTipText(tooltip);
		newBtn.addActionListener(this);
		if(borderedButtons) {
			newBtn.setBorder(new LineBorder(Color.gray, 1, false));
		} else if (compactToolbars) {
			newBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		//newBtn.setBorderPainted(false);
		return newBtn;
	}
	
	/**
	 * Makes a JToggleButton with the given icon and tooltop.
	 * If the icon cannot be loaded, then the text will be used instead.
	 *
	 * Adds this mapeditor as an actionListener.
	 *
	 * @return a shiny new JToggleButton
	 **/
	JToggleButton makeToggleBtn(String text, String icon, String tooltip) {
		JToggleButton newBtn;
		try {
			newBtn = new JToggleButton(new ImageIcon(getClass().getResource(icon)));
		} catch (Exception e) {
			newBtn = new JToggleButton(text);
		}
		newBtn.setToolTipText(tooltip);
		newBtn.addActionListener(this);
		if(borderedButtons) {
			newBtn.setBorder(new LineBorder(Color.gray, 1, false));
		} else if (compactToolbars) {
			newBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		return newBtn;
	}
	
	/**
	 * returns the currently selected tile in the tileChooser.
	 **/
	public Tile getSelectedTile()
	{
		return tileChooser.getSelectedTile();
	}
	
	private void setGraphicsBank(GraphicsBank gfx)
	{
		this.gfx = gfx;
		scene.setTileset(gfx);
		chooserPanel.removeAll();
		tileChooser = new TileChooser(gfx, mainFrame);
		chooserPanel.add(tilesetInfoPane, BorderLayout.SOUTH);
		JScrollPane tileScroll = new JScrollPane(tileChooser);
		chooserPanel.add(tileScroll, BorderLayout.CENTER);
		mainFrame.repaint();
		if(gfx.getFile() != null) {
			tilesetFileLabel.setText("Tileset: " + gfx.getFile().getName());
			tilesetFileLabel.setToolTipText(gfx.getFile().toString());
		} else {
			tilesetFileLabel.setText("Tileset: * Unsaved *");
			tilesetFileLabel.setToolTipText("");
		}
	}
	
	public GraphicsBank getCurrentGraphicsBank() {
		return gfx;
	}
	
	/**
	 * Saves the file... (or tells the scene to save it actually)
	 **/
	public void saveFile(File file)
	{
	  System.err.println("Saving scene as "+file);
	  
    if(scene.getTileset().isUnsaved()) {
    	System.out.println("?? 2 " + (scene.getTileset() == gfx));
    	PromptDialog.tell("Please save your tileset first.", "OK");
    	return;
    }
	  try {
			scene.saveScene(file);
			openFile = file;
			mainFrame.validate();
		} catch (Exception e) {
			PromptDialog.tell("Could not save: "+e, "OK");
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens the map file.
	 **/
	public void openFile(File file)
	{
		try
		{
			zoomLevel = 1;
			scene = Scene.loadScene(file);
			map = scene.getMap();
			setGraphicsBank(scene.getTileset());
			System.out.println("Scene caused tileset "+gfx.getFile()+" to be loaded");
			
			mapPanel.setMap(map);
			
			setIgnoreEffectChanges(true);
			r.setValue((int)(scene.effect_rScale * 100));
			g.setValue((int)(scene.effect_gScale * 100));
			b.setValue((int)(scene.effect_bScale * 100));
			h.setValue((int)(scene.effect_hue * 360));
			setIgnoreEffectChanges(false);
			s.setValue((int)(scene.effect_sat * 100));
			
			openFile = file; /* TODO: bad variable name */
			mainFrame.validate();
			mapPanel.validate();
			mainFrame.repaint();
		}
		catch(IOException e)
		{
			System.out.println("Invalid Map File. " + e);
		}
		
	}
	
	/**
	 * creates a new scene with a new map, 10 by 10 null tiles,
	 * and an empty list of sprites.
	 **/
  public void newFile()
  {
  	/*
    GraphicsBank gfx = new GraphicsBank();
    try {
      //gfx.loadTileset(new File("gfx/outdoors.dat"));
    } catch(Exception e) {
      System.err.println("Could not load default graphics bank, using blank one.");
      gfx = new GraphicsBank();
    } */
    scene = new Scene(new Map(10,10), new ArrayList(), gfx);
    zoomLevel = 1;
    map = scene.getMap();
    setGraphicsBank(scene.getTileset());
    mapPanel.setMap(map);
    mapPanel.repaint();
    mainFrame.validate();
  }
	
	
	/**
	 * Create the colour adjust dialog
	 **/	
	JPanel createColorDialog() {
		
		JPanel op;
		JPanel cp;
		/*
		JDialog dialog = new JDialog(mainFrame, "Adjust Colour");
		dialog.setModal(false);
		*/
		op = new JPanel();
		
		r = new JSlider(JSlider.VERTICAL, 0, 400, 100);
		g = new JSlider(JSlider.VERTICAL, 0, 400, 100);
		b = new JSlider(JSlider.VERTICAL, 0, 400, 100);
		h = new JSlider(JSlider.VERTICAL, 0, 360, 0);
		s = new JSlider(JSlider.VERTICAL, 0, 400, 100);
		
		r.setBackground(Color.red);
		g.setBackground(Color.green);
		b.setBackground(Color.blue);
		s.setBackground(Color.gray);
		
		r.setBorder(new TitledBorder("R"));
		g.setBorder(new TitledBorder("G"));
		TitledBorder blueBorder = new TitledBorder("B");
		blueBorder.setTitleColor(Color.white);
		b.setBorder(blueBorder);
		h.setBorder(new TitledBorder("H"));
		s.setBorder(new TitledBorder("S"));
		
		r.setToolTipText("Red channel");
		
		r.setPaintTrack(false);
		g.setPaintTrack(false);
		b.setPaintTrack(false);
		
		//op = (JPanel)dialog.getContentPane();
		op.setLayout(new BorderLayout());
		cp = new JPanel(new FlowLayout());
		op.add(cp, BorderLayout.CENTER);
		cp.add(r);
		cp.add(g);
		cp.add(b);
		cp.add(h);
		cp.add(s);
		
		r.addChangeListener(this);
		g.addChangeListener(this);
		b.addChangeListener(this);
		h.addChangeListener(this);
		s.addChangeListener(this);
		
		effectsResetBtn = new JButton("Reset");
		effectsResetBtn.addActionListener(this);
		op.add(effectsResetBtn, BorderLayout.SOUTH);
		
		//dialog.pack();
		//dialog.setResizable(false);
		
		/* NOTE: source of an anonymous inner class */
		/*
		dialog.addWindowListener(new WindowListener(){
			public void windowClosing(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {
				palletteBtn.setSelected(false);
			}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
		});
		*/
		//return dialog;
		return op;
	}
	
	/* State listener for the sliders in the colour pallette dialog */
	public void stateChanged(ChangeEvent e) {
		if(!ignoreEffects) {
		scene.setEffect(r.getValue() / 100f,
		                g.getValue() / 100f,
		                b.getValue() / 100f,
		                h.getValue() / 360f,
		                s.getValue() / 100f,
		                zoomLevel);
		}
		                
		mapPanel.refreshZoom();
	}
	
	int getPaintMode() {
		if(fillBtn.isSelected()) {
			return PAINT_FILL;
		} else {
			return PAINT_NORMAL;
		}
	}
	
	private void setIgnoreEffectChanges(boolean ign) {
		ignoreEffects = ign;
	}
	
	void newTileset() {
		gfx = new GraphicsBank();
		setGraphicsBank(gfx);
	}
	
	void openTileset() {
		try {
			int success = tschooser.showOpenDialog(mainFrame);
			if (success == JFileChooser.APPROVE_OPTION) {
				GraphicsBank g = new GraphicsBank();
				g.loadTileset(tschooser.getSelectedFile());
				setGraphicsBank(g);
			}
		} catch(FileNotFoundException e) {
			PromptDialog.tell("Selected file could not be found", "OK");
			System.out.println(e);
			e.printStackTrace();
		} catch(IOException e) {
			PromptDialog.tell("Could not read the file", "OK");
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	void saveTileset() {
		try {
			int success = tschooser.showSaveDialog(mainFrame);
			if (success == JFileChooser.APPROVE_OPTION) {
				gfx.saveTileset(tschooser.getSelectedFile());
				setGraphicsBank(gfx);
			}
		} catch(IOException e) {
			PromptDialog.tell("Could not read the file", "OK");
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	
	
	//String userHome = System.getProperty("user.home");
	
	public static void main(String[] a)
	{
		//System.out.print("The default OpenGL setting on this system is ");
		//System.out.print(System.getProperty("sun.java2d.opengl"));
		//System.setProperty("sun.java2d.opengl", "true");
		
		new MapEdit();
	}
	
	
	
}