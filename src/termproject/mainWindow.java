package termproject;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javax.swing.JFileChooser;
import java.util.ArrayList;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

@SuppressWarnings("serial")
public class mainWindow extends JFrame implements ActionListener{

	//Variable declaration for panels/buttons
	JPanel totalGUI, buttonGUI;
	JButton buttonOpen;  

	//Creates new file chooser object
	JFileChooser m_fc = new JFileChooser();
	
	//Variable declaration for video frame extraction
	FFmpegFrameGrabber rawVideo;
	
	//Array list containing buffered images of the video frames
	ArrayList<BufferedImage> videoFrames;

	public JPanel createContentPane(){	   
		
		//Create the panels/buttons
		totalGUI = new JPanel();
		buttonGUI = new JPanel();
		buttonOpen = new JButton("Open Video File");
		
		//Adds event listeners to the buttons for user interaction
		buttonOpen.addActionListener(this);
		
		//Places the buttons/panels into the UI
		buttonGUI.add(buttonOpen);
		totalGUI.add(buttonGUI);
		
		return totalGUI;
	}

	public void actionPerformed(ActionEvent evnt) {
		//Performs action if "buttonOpen" is clicked
		if(evnt.getSource() == buttonOpen){
			//Opens window to choose video file
			int result = m_fc.showOpenDialog(totalGUI);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = m_fc.getSelectedFile();
				String filename = file.toString();
				
				//Grabs the filename for video frame extraction
				rawVideo = new FFmpegFrameGrabber(filename);
				
				try {
					rawVideo.start();
					
					//Grabs the first 50 frames of the video
					for (int i = 0 ; i < 50 ; i++) {
						
						//Creates a buffered image from the video frame
						BufferedImage currentFrame = new Java2DFrameConverter().convert(rawVideo.grabImage());
					    
						//Place the current frame into an array list
						videoFrames.add(currentFrame);
						
						/*
						 * //Testing purposes: saves the buffered image into a png file
						 *	int returnVal = m_fc.showSaveDialog(mainWindow.this);
			        	 *	if (returnVal == JFileChooser.APPROVE_OPTION) {
			        	 *		File fileSave = m_fc.getSelectedFile();	
			        	 *		try {
			        	 *			ImageIO.write(currentFrame, "png", fileSave);
				         *		} catch (IOException e) {
				         *   	
				         *   	}
			        	 *	}
			        	*/
					}

					rawVideo.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	private static void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("CMPT 365 Term Project:Video Compression");

		mainWindow window = new mainWindow();
		frame.setContentPane(window.createContentPane());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1920, 1050);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
