package termproject;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

//class to draw the image into the panel
public class IMGPanel extends JPanel {
	  
	protected Image m_img = null;
	  
    public IMGPanel() {}

	//set the image to image bi
    public void setBufferedImage(Image img) {
    	if (img == null)
    		return;
    	m_img = img;
    	revalidate();
    	repaint();
    }

	//paint the image onto the panel
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	    Dimension d = getSize();
	    g.setColor(getBackground());
	    g.fillRect(0, 0, d.width, d.height);
	    if (m_img != null)
	    	g.drawImage(m_img, 0, 0, d.width, d.height,
	    		       0, 0, m_img.getWidth(null), m_img.getHeight(null), this);
	}
	    
	public Image getBufferedImage() {
	    return m_img;
	}
}
