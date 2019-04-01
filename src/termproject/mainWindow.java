package termproject;

import java.awt.AWTException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.util.List;

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
	ArrayList<BufferedImage> videoFrames = new ArrayList<BufferedImage>();
	ArrayList<ArrayList<int[]>> YUVFrames = new ArrayList<ArrayList<int[]>>();
	
	//Video Properties
	int width, height;

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
				extractFrames();
				
				try {
					convertYUV();
				} catch (AWTException e) {
					e.printStackTrace();
				}
				
				subSampling();
				
				//System.out.println(YUVFrames.get(0).get(0)[0]);

				return;
//				for(int i=0; i<50; i++) {
//					System.out.println(videoFrames.get(i));
//				}
				
			}
		}
	}
	
	
	public void extractFrames() {
		try {
			rawVideo.start();
			
			//Grabs the first 50 frames of the video
			for (int i = 0 ; i < 50 ; i++) {
				
				//Creates a buffered image from the video frame
				BufferedImage currentFrame = new Java2DFrameConverter().convert(rawVideo.grabImage());
			    
				//Place the current frame into an array list
				videoFrames.add(currentFrame);	
				
				/*
				  //Testing purposes: saves the buffered image into a png file
				 	int returnVal = m_fc.showSaveDialog(mainWindow.this);
	        	 	if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	 		File fileSave = m_fc.getSelectedFile();	
	        	 		try {
	        	 			ImageIO.write(currentFrame, "png", fileSave);
		         		} catch (IOException e) {
		            	
		            	}
	        	 	}
	        	 */
	        	
			}

			rawVideo.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void convertYUV() throws AWTException {
		for(int i = 0; i < videoFrames.size(); i++) {
	    	width = videoFrames.get(i).getWidth(null);
	    	height = videoFrames.get(i).getHeight(null);
	    	
	    	ArrayList<int[]> currentYUVFrame = new ArrayList<int[]>();
	    	int[] inputValues = new int[width*height];
	        int[] YValues = new int[width*height];
	        int[] UValues = new int[width*height];
	        int[] VValues = new int[width*height];
	        
	    	// Grab Original Image Pixel Values
	    	PixelGrabber grabber = new PixelGrabber(videoFrames.get(i).getSource(), 0, 0, width, height, inputValues, 0, width);
	        try{
	          if(grabber.grabPixels() != true){
	            throw new AWTException("Grabber returned false: " + grabber.status());
	          }
	        } catch (InterruptedException e) {
	        	
	        };
	        
	        // set YUV values 
	        for (int index = 0; index < height * width; ++index)
	        {
	        	int red = ((inputValues[index] & 0x00ff0000) >> 16);
	        	int green =((inputValues[index] & 0x0000ff00) >> 8);
	        	int blue = ((inputValues[index] & 0x000000ff) );
	        	YValues[index] = (int)((0.299 * (float)red) + (0.587 * (float)green) + (0.114 * (float)blue)); 
	        	UValues[index] = (int)((-0.14713 * (float)red) + (-0.28886 * (float)green) + (0.436 * (float)blue)); 
	        	VValues[index] = (int)((0.615 * (float)red) + (-0.51499 * (float)green) + (-0.10001 * (float)blue)); 
	        }
	        
	        // Add Y,U,V data into an arraylist
	        currentYUVFrame.add(YValues);
	        currentYUVFrame.add(UValues);
	        currentYUVFrame.add(VValues);
	        
	        // Add the YUV arraylist to a global arraylist containing all frames
	        YUVFrames.add(currentYUVFrame);
		}
	}
	
	public void subSampling() {
		
	}
	
	
	public static double[][] DCT(int f[][]) {
        double[][] F = new double[8][8];
        double Cu = 0;
        double Cv = 0;
        double partialDCT = 0;
        double sum = 0;

        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
	            if (u == 0) {
	                Cu = Math.sqrt(2) / 2;
	            } else {
	                Cu = 1;
	            }
	
	            if (v == 0) {
	                Cv = Math.sqrt(2) / 2;
	            } else {
	                Cv = 1;
	            }
	
	            sum = 0;
	            for (int i = 0; i < 8; i++) {
	                for (int j = 0; j < 8; j++) {
		                partialDCT = Math.cos((2 * i + 1) * u * Math.PI / 16) * Math.cos((2 * j + 1) * v * Math.PI / 16)
		                    * (f[i][j]);
		                sum = sum + partialDCT;
	                }
	            }
	            F[u][v] = (Cu * Cv * sum) / 4;
            }
        }
        return F;
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
