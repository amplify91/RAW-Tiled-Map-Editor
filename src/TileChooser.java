import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import javax.imageio.ImageIO;

public class TileChooser extends JPanel implements ActionListener, GraphicsBankChangeListener
{
	
	
	//DropTarget dropTarget;
	
	ArrayList tiles;
	GridLayout layout;
	GraphicsBank gfx;
	int tileWidth = 32;
	Tile selectedTile;
	ButtonGroup group;
	JPanel tilePanel;
	JPanel spacer;
	
	
	/* For tile properties dialog */
	JDialog propertiesDialog;
	Tile propertyTile;
	JTextField userText;
	JSpinner tileNumber;
	JTextField tileName;
	JTextField tileType;
	JLabel tileImg;
	JButton applyBtn;
	JButton cancelBtn;
	JButton deleteBtn;
	JTextField imageFile;
	
	
	FileDropHandler fileDrop;
	
	public TileChooser(GraphicsBank gfx)
	{
		tilePanel = new JPanel();
		layout = new GridLayout(0,5);
		tilePanel.setLayout(layout);
		
		this.setLayout(new BorderLayout());
		this.add(tilePanel, BorderLayout.NORTH);
		
		/* Put in spacer to enlarge the panel as a drop target */
		spacer = new JPanel();
		spacer.add(new JLabel("  Drop new tiles here"));
		spacer.setToolTipText("Drop image files here to create more tiles.");
		//spacer.setPreferredSize(gfx.getBaseTileSize());
		this.add(spacer, BorderLayout.CENTER);
		
		
		this.gfx = gfx;
		reset();
		
		fileDrop = new FileDropHandler();
		setTransferHandler(fileDrop);
		gfx.addChangeListener(this);
		
		propertiesDialog = null;
		
	}
	
	public TileChooser(GraphicsBank gfx, JFrame dialogOwner) {
		this(gfx);
		createPropertiesDialog(dialogOwner);
	}
	
	void createPropertiesDialog(JFrame dialogOwner) {
		
		/* Setup for the proeprties dialog */
		propertiesDialog = new JDialog(dialogOwner, "Tile Properties");
		propertiesDialog.setSize(300, 300); //to set location better
		propertiesDialog.setLocationRelativeTo(null);
		tileName = new JTextField("", 20);
		tileType = new JTextField("", 20);
		imageFile = new JTextField("", 20);
		imageFile.setEditable(false);
		tileNumber = new JSpinner();
		userText = new JTextField("", 20);
		propertyTile = null;
		tileImg = new JLabel();
		tileImg.setHorizontalAlignment(SwingConstants.CENTER);
		tileImg.setBorder(new TitledBorder("Image"));
		JPanel cp = (JPanel)propertiesDialog.getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel p2 = new JPanel(new BorderLayout());
		cp.add(p2, BorderLayout.CENTER);
		cp.add(tileImg, BorderLayout.NORTH);
		JPanel btns1 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(3, 3, 3, 3);
		
		c.gridx = 0;
		c.gridy = 0;
		
		
		btns1.add(new JLabel("ID"), c);
		c.gridx = 1;
		c.ipadx = 30;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		btns1.add(tileNumber, c);
		c.ipadx = 0;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy = 1;
		btns1.add(new JLabel("Type"), c);
		c.gridx = 1;
		btns1.add(tileType, c);
		c.gridx = 0;
		c.gridy = 2;
		
		btns1.add(new JLabel("Name"), c);
		c.gridx = 1;
		btns1.add(tileName, c);
		
		c.gridx = 0;
		c.gridy = 3;
		btns1.add(new JLabel("User Text"), c);
		c.gridx = 1;
		btns1.add(userText, c);
		
		p2.add(btns1, BorderLayout.NORTH);
		
		
		/* The buttons */
		applyBtn = new JButton("Save");
		deleteBtn = new JButton("Delete Tile");
		cancelBtn = new JButton("Cancel");
		applyBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		deleteBtn.addActionListener(this);
		
		JPanel btns2 = new JPanel(new GridLayout(1, 3));
		btns2.add(deleteBtn);
		btns2.add(applyBtn);
		btns2.add(cancelBtn);
		
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		
		btns1.add(btns2, c);
		
		propertiesDialog.setSize(300, 500);
		propertiesDialog.setResizable(false);
		
	}
	
	/**
	 * Rebuild the entire panel from the graphics bank
	 **/
	public void reset() {
		int count = 0;
		tilePanel.removeAll();
		group = new ButtonGroup();
		
		TileButton b = new TileButton(null);
		tilePanel.add(b);
		group.add(b);
		count ++;
		
		Iterator i = gfx.iterator();
		while(i.hasNext())
		{
			b = new TileButton((Tile)i.next());
			tilePanel.add(b);
			group.add(b);
			count ++;
		}
		
		if(count <= 0) {
			//spacer.setText("  No Tiles");
			spacer.setPreferredSize(new Dimension(1, 100));
			//spacer.setBorder(new LineBorder(Color.black));
		} else {
			//spacer.setText("  Drop new tiles");
			spacer.setPreferredSize(new Dimension(1, 30));
		}
		
		
		tilePanel.revalidate();
		repaint();
	}
	
	/**
	 * Large change to the tileset, so we just remove
	 * all the buttons and rebuild them
	 **/
	public void tilesetUpdated(GraphicsBank g) {
		System.out.println("tilset updated");
		if(g == gfx) {
			reset();
		}
	}
	/**
	 * Reset. we don't keep track of the buttons so
	 * we have to remove everything and rebuild it. 
	 **/
	public void tileRemoved(GraphicsBank g, Tile t) {
		System.out.println("tilset updated");
		if(g == gfx) {
			reset();
		}
	}
	/**
	 * Add a single tile button 
	 **/
	public void tileAdded(GraphicsBank g, Tile t) {
		System.out.println("tilset updated");
		TileButton b = new TileButton(t);
		tilePanel.add(b);
		group.add(b);
		
		//spacer.setText("  Drop new tiles");
		spacer.setPreferredSize(new Dimension(1, 30));
		
		tilePanel.revalidate();
		repaint();
	}
	
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == applyBtn && propertyTile != null) {
			propertyTile.name = tileName.getText();
			propertyTile.type = tileType.getText();
			propertyTile.number = ((Integer)tileNumber.getValue()).intValue();
			propertyTile.info = userText.getText();
			propertiesDialog.dispose();
			propertyTile = null;
		} else if(e.getSource() == cancelBtn) {
			propertiesDialog.dispose();
			propertyTile = null;
		} else if(e.getSource() == deleteBtn) {
			if(propertyTile != null) {
				gfx.remove(propertyTile);
				propertyTile = null;
			}
			propertiesDialog.dispose();
		} else {
			System.err.println("Unknown button fired action. "+e);
		}
	}
	
	public void setWidth(int width)
	{
		if(width >= tileWidth+8)
		{
			layout.setColumns(width/(tileWidth+15));
			tilePanel.revalidate();
		}
		
	}
	
	public Tile getSelectedTile()
	{
		return selectedTile;
	}
	
	void showProperties(Tile t) {
		propertyTile = t;
		
		if(t != null) {
			userText.setText(t.getInfo());
			tileNumber.setValue(new Integer(t.getNumber()));
			tileName.setText(t.getName());
			tileType.setText(t.getType());
			tileImg.setIcon(new ImageIcon(t.getImage()));
			
			applyBtn.setEnabled(true);
			deleteBtn.setEnabled(true);
			userText.setEditable(true);
			tileNumber.setEnabled(true);
			tileName.setEditable(true);
			tileType.setEditable(true);
		} else {
			userText.setText("");
			tileNumber.setValue(new Integer(0));
			tileName.setText("Null (Erases existing tiles)");
			tileType.setText("");
			tileImg.setIcon(null);
			
			userText.setEditable(false);
			tileNumber.setEnabled(false);
			tileName.setEditable(false);
			tileType.setEditable(false);
			applyBtn.setEnabled(false);
			deleteBtn.setEnabled(false);
		}
		
		propertiesDialog.pack();
		propertiesDialog.setVisible(true);
		
	}
	
	public void importImageAsTile(File f) throws IOException {
		importImageAsTile(f, 0);
	}
	
	public void importImageAsTile(File f, int level) throws IOException {		
		
		if(f.isDirectory()) {
			File[] contents = f.listFiles();
			
			for(int num = 0; num < contents.length; num++) {
				importImageAsTile(contents[num]);
			}
		}
			
		
		
		System.out.println("Import "+f);
		try {
			ImageIO.read(f);
		} catch(Exception e) {
			System.out.println("FAIL");
			return;
		}
		
		System.out.println("getbasedir.... ahuh!");
		//File base = new File(gfx.getBaseDirectory().getCanonicalPath());
		
		System.out.println("?1");
		int n = gfx.getUnusedNumber();
		System.out.println("?2");
		Tile t = new Tile(n, f.getAbsolutePath(), "New Tile "+n, "No Type");
		
		
		System.out.println("Adding "+f);
		gfx.add(t);
		
		if(propertiesDialog != null) {
			showProperties(t);
		}
	}
	
	
	
	
	
	
	
	/********************************************************
	 * Inner class. The buttons that appear on the chooser. *
	 ********************************************************/
	
	class TileButton extends JToggleButton implements ActionListener, MouseListener
	{
		Tile tile;
		public TileButton(Tile t)
		{
			super();
			
			Image i2 = new BufferedImage(gfx.getBaseTileSize().width, gfx.getBaseTileSize().height, BufferedImage.TYPE_INT_ARGB);
			
			if(t != null) {
				Image i = t.getImage();
				i2.getGraphics().drawImage(i, 0, 0, 32, 32, null);
				setToolTipText(t.getName());
			}
			
			setIcon(new ImageIcon(i2));
			
			
			setMargin(new Insets(2,2,2,2));
			tile = t;
			
			this.addMouseListener(this);
			this.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			selectedTile = tile;
		}
		
		
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
		
		public void mouseClicked(MouseEvent e)
		{
			if(SwingUtilities.isRightMouseButton(e))
			{
				showProperties(tile);
			}
		}
		
		
		public Tile getTile()
		{
			return tile;
		}
		
	}
	
	
	
	
	
	/**************************************
	 * Inner class - the transfer handler *
	 **************************************/
	 
	class FileDropHandler extends TransferHandler {
		
		/**
		 * Determine whether we can or cannot accept the stuff the user is dragging
		 * onto our panel. In this case we accept only file lists.
		 **/
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			//System.out.println("Got query by component: "+comp);
			for(int i = 0; i < transferFlavors.length; i++) {
				//System.out.println("Available flavor: "+transferFlavors[i]);
				if(transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
					//System.out.println("I can use this");
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Import info about the file.
		 * Makes the assumption that the "canImport" has worked correctly and
		 * this data can in fact be served up as a File List.
		 **/
  	public boolean importData(JComponent comp, Transferable t) {
//System.out.println("Import data");
  		try {
	  		java.util.List files = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
	  		if(files.size() > 4) {
	  			/* Does not work. Forever halts the AWT thread.
		  		if(!(PromptDialog.ask("Really process "+files.size()+" files?", "Yes", "Cancel").equals("Yes"))) {
		  			return false;
		  		} */
	  		}
	  		Iterator itr = files.iterator();
//System.out.println("itr, size = "+files.size());
	  		while(itr.hasNext()) {
	  			File f = (File)itr.next();
//System.out.println("imported "+f);
	  			importImageAsTile(f);
//System.out.println("done "+f);
	  		}
      } catch (UnsupportedFlavorException e) {
        System.err.println("Unsupported drop content: " + e);
      } catch (IOException e) {
      	System.err.println("Unexpected IO Exception while importing tile: " + e);
      	e.printStackTrace();
      	//PromptDialog.tell("Sorry, something broke. Please save your work and restart the program.", "OK");
      }
  		return true;
  	}
	}
	/************************
	 * End of inner classes *
	 ************************/
	
}
	