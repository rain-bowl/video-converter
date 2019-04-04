package termproject;

import java.awt.AWTException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
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

	ArrayList<BufferedImage> videoIFrames = new ArrayList<BufferedImage>();
	ArrayList<BufferedImage> videoPFrames = new ArrayList<BufferedImage>();
	ArrayList<BufferedImage> videoBFrames = new ArrayList<BufferedImage>();

	ArrayList<ArrayList<int[]>> YUVIFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> YUVPFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> YUVBFrames = new ArrayList<ArrayList<int[]>>();

	ArrayList<ArrayList<int[]>> ChromaIFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> ChromaPFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> ChromaBFrames = new ArrayList<ArrayList<int[]>>();

	//Video Properties
	int width, height;

	//Set IframeDistance
	int IframeDistance = 10;

	public JPanel createContentPane(){	   

		totalGUI = new JPanel();
		totalGUI.setLayout(null);
		
		buttonGUI = new JPanel();
		buttonGUI.setLayout(null);
		buttonGUI.setLocation(10, 10);
		buttonGUI.setSize(1920, 60);
		totalGUI.add(buttonGUI);

		buttonOpen = new JButton("Open Video File");
		buttonOpen.setLocation(0, 0);
		buttonOpen.setSize(150, 40);
		buttonOpen.addActionListener(this);
		buttonGUI.add(buttonOpen);
		
	    totalGUI.setOpaque(true);

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

			    String ext = null;
			    String s = file.getName();
			    int i = s.lastIndexOf('.');

		        if (i > 0 &&  i < s.length() - 1) {
		            ext = s.substring(i+1).toLowerCase();
		        }
			        
		        if(ext.equals("yuv")) {
		        	System.out.println("YUV file");
		        	//rawVideo = new FrameGrabber(filename);
		        }
		        else {
			    
		        	// Grabs the filename for video frame extraction
		        	rawVideo = new FFmpegFrameGrabber(filename);
				
		        }

				extractFrames();
				separateFrames();

				// I-Frames Encoding
				convertYUV(0);
				subSampling(YUVIFrames, 0);
				//DCT(ChromaIFrames, 0);
				
				test();
			}
		}
	}

	public void extractFrames() {
		try {
			rawVideo.start();

			//Grabs the first 50 frames of the video
			for (int i = 0 ; i < 2 ; i++) {

				//Creates a buffered image from the video frame
				BufferedImage currentFrame = new Java2DFrameConverter().convert(rawVideo.grabImage());

				//Place the current frame into an array list
				videoFrames.add(currentFrame);	

			}

			rawVideo.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void separateFrames() {
		int PframeDistance = IframeDistance/2;

		for(int i=0; i < videoFrames.size(); i++) {
			if(i % IframeDistance == 0) {
				videoIFrames.add(videoFrames.get(i));
			}
			else if(i % PframeDistance == 0) {
				videoPFrames.add(videoFrames.get(i));
			}
			else {
				videoBFrames.add(videoFrames.get(i));
			}

		}
	}

	public void convertYUV(int currentMethod) {
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
	                try {
						throw new AWTException("Grabber returned false: " + grabber.status());
					} catch (AWTException e) {
						e.printStackTrace();
					};
	              }
	            } catch (InterruptedException e) {};


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

			// Add Y,U,V data into an array list
			currentYUVFrame.add(YValues);
			currentYUVFrame.add(UValues);
			currentYUVFrame.add(VValues);

			if(currentMethod == 0) {
				// Add the YUV array list to a global array list containing all frames
				YUVIFrames.add(currentYUVFrame);
			}
			else if(currentMethod == 1) {
				// Add the YUV array list to a global array list containing all frames
				YUVPFrames.add(currentYUVFrame);
			}
			else {
				// Add the YUV array list to a global array list containing all frames
				YUVBFrames.add(currentYUVFrame);
			}
		}
	}

	public void subSampling(ArrayList<ArrayList<int[]>> inputYUVFrames, int currentMethod) {
		
		// set result data size
		int[] res = new int[width*height*3];
		int[] noLumaRes = new int[width*height*3];
		
		for(int i=0; i<inputYUVFrames.size(); i++) {
			ArrayList<int[]> outputRes = new ArrayList<int[]>();
			
			// set UV values for chroma use
			int[] UChroma = new int[width*height];
			int[] VChroma = new int[width*height];
	
			// adding every other U and V value to a block of 4
			for (int y = 1; y < height; y+=2)
			{
				for (int x = 1; x < width; x+=2)
				{
					UChroma[((y - 1)*width + (x - 1))] = (inputYUVFrames.get(i).get(1)[(y - 1)*width + (x - 1)]);
					UChroma[((y - 1)*width + x)] = (inputYUVFrames.get(i).get(1)[(y - 1)*width + (x - 1)]);
					UChroma[(y*width + (x - 1))] = (inputYUVFrames.get(i).get(1)[(y - 1)*width + (x - 1)]);
					UChroma[(y*width + x)] = (inputYUVFrames.get(i).get(1)[(y - 1)*width + (x - 1)]);
	
					VChroma[((y - 1)*width + (x - 1))] = (inputYUVFrames.get(i).get(2)[(y - 1)*width + (x - 1)]);
					VChroma[((y - 1)*width + x)] = (inputYUVFrames.get(i).get(2)[(y - 1)*width + (x - 1)]);
					VChroma[(y*width + (x - 1))] = (inputYUVFrames.get(i).get(2)[(y - 1)*width + (x - 1)]);
					VChroma[(y*width + x)] = (inputYUVFrames.get(i).get(2)[(y - 1)*width + (x - 1)]);    	
				}
			}
			
			outputRes.add(inputYUVFrames.get(i).get(0));
			outputRes.add(UChroma);
			outputRes.add(VChroma);
			
			if(currentMethod == 0) {
				// Add the ChromaSubsampled array list to a global array list containing all frames
				ChromaIFrames.add(outputRes);
			}
			else if(currentMethod == 1) {
				// Add the ChromaSubsampled array list to a global array list containing all frames
				ChromaPFrames.add(outputRes);
			}
			else {
				// Add the ChromaSubsampled array list to a global array list containing all frames
				ChromaBFrames.add(outputRes);
			}
			
		}

	}

    // split image input into blocks of 8x8
	public ArrayList<ArrayList<int[][]>> eightBlocks()
    {
		int newWidth = ((int)width/8)*8;
		int newHeight = ((int)height/8)*8;
		int xcount = 0;
		int ycount = 0;
		int blockNum = newWidth/8 * newHeight/8;
		ArrayList<ArrayList<int[][]>> res = new ArrayList<ArrayList<int[][]>>();
		ArrayList<int[][]> resY = new ArrayList<int[][]>();
		ArrayList<int[][]> resU = new ArrayList<int[][]>();
		ArrayList<int[][]> resV = new ArrayList<int[][]>();

    	for (int x = 0; x < blockNum; x++)
    	{
            int[][] blockY = new int[8][8];
            int[][] blockU = new int[8][8];
            int[][] blockV = new int[8][8];

            for (int e = 0; e < 8; e++)
            {
                for (int f = 0; f < 8; f++)
                {
                    if (xcount % newWidth == 0 && x != 0 && xcount != 0)
                    {
                        ycount += 8;
                        xcount = 0;
                    }
            
//                    blockY[e][f] = YValues[(ycount + e) * w + ((x % (newWidth/8)) * 8) + f];
//                    blockU[e][f] = UValues[(ycount + e) * w + ((x % (newWidth/8)) * 8) + f];
//                    blockV[e][f] = VValues[(ycount + e) * w + ((x % (newWidth/8)) * 8) + f];
                }
                xcount++;
            }
            resY.add(blockY);
            resU.add(blockU);
            resV.add(blockV);
    	}

    		res.add(resY);
    		res.add(resU);
    		res.add(resV);
    		
		return res;
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

	
	public void test() {
		
		JPanel OutputImg;
		IMGPanel m_panelImgOutputY, m_panelImgOutputU, m_panelImgOutputV;
		BufferedImage m_imgOutputY, m_imgOutputU, m_imgOutputV;
		
		OutputImg = new JPanel();
		OutputImg.setLayout(null);
		OutputImg.setLocation(10, 60);
		OutputImg.setSize(1920, 320);
		totalGUI.add(OutputImg);
		
        m_panelImgOutputY = new IMGPanel();
        m_panelImgOutputY.setLocation(0, 10);
        m_panelImgOutputY.setSize(400, 300);
        OutputImg.add(m_panelImgOutputY);
        
        m_panelImgOutputU = new IMGPanel();
        m_panelImgOutputU.setLocation(410, 10);
        m_panelImgOutputU.setSize(400, 300);
        OutputImg.add(m_panelImgOutputU);
        
        m_panelImgOutputV = new IMGPanel();
        m_panelImgOutputV.setLocation(820, 10);
        m_panelImgOutputV.setSize(400, 300);
        OutputImg.add(m_panelImgOutputV);
		
        m_imgOutputY = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    	WritableRaster rasterY = (WritableRaster) m_imgOutputY.getData();
    	rasterY.setPixels(0, 0, width, height, ChromaIFrames.get(0).get(0));
    	m_imgOutputY.setData(rasterY);
    	m_panelImgOutputY.setBufferedImage(m_imgOutputY);		
    	
        m_imgOutputU = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    	WritableRaster rasterU = (WritableRaster) m_imgOutputU.getData();
    	rasterU.setPixels(0, 0, width, height, ChromaIFrames.get(0).get(1));
    	m_imgOutputU.setData(rasterU);
    	m_panelImgOutputU.setBufferedImage(m_imgOutputU);	
    	
        m_imgOutputV = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    	WritableRaster rasterV = (WritableRaster) m_imgOutputV.getData();
    	rasterV.setPixels(0, 0, width, height, ChromaIFrames.get(0).get(2));
    	m_imgOutputV.setData(rasterV);
    	m_panelImgOutputV.setBufferedImage(m_imgOutputV);	
		
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
