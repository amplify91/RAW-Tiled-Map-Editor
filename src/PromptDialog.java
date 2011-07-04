import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Not finished!
 **/
public class PromptDialog {
	
  /* Note: NOT threadsafe. */
  static String ans = null;
	
	
	
	
	static void tell(String question, String btnText) {
		final JDialog d = new JDialog();
		d.setLocationRelativeTo(null);
		JPanel bpane = new JPanel(new FlowLayout());
		JLabel l = new JLabel(question);
		
		JPanel cp = (JPanel)d.getContentPane();
		
		cp.setLayout(new FlowLayout());
		cp.add(l, BorderLayout.CENTER);
		cp.add(bpane, BorderLayout.SOUTH);
    
    JButton b1 = new JButton("OK");
		bpane.add(b1);
		
		d.pack();
		
		d.setVisible(true);
		
    b1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ans = ((JButton)e.getSource()).getText();
        d.dispose();
      }
    });
		
	}
	
	
}



