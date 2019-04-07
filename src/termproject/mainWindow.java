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
import java.util.stream.DoubleStream;

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
    int YValues[], UValues[], VValues[], inputValues[];
    
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

		        inverseIntegerTransform();
				extractFrames();
				separateFrames();

				// I-Frames Encoding
				convertYUV(0);
				subSampling(YUVIFrames, 0);
		        //TESTING: integerTransform();
				
				//test();
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
			inputValues = new int[width*height];
			YValues = new int[width*height];
			UValues = new int[width*height];
			VValues = new int[width*height];

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

    // split image input into blocks of size x size (ex size = 8, 8 x 8 blocks)
	public ArrayList<ArrayList<int[][]>> blocker(int size)
    {
		int newWidth = ((int)width/size)*size;
		int newHeight = ((int)height/size)*size;
		int xcount = 0;
		int ycount = 0;
		int blockNum = newWidth/size * newHeight/size;
		ArrayList<ArrayList<int[][]>> res = new ArrayList<ArrayList<int[][]>>();
		ArrayList<int[][]> resY = new ArrayList<int[][]>();
		ArrayList<int[][]> resU = new ArrayList<int[][]>();
		ArrayList<int[][]> resV = new ArrayList<int[][]>();

    	for (int x = 0; x < blockNum; x++)
    	{
            int[][] blockY = new int[size][size];
            int[][] blockU = new int[size][size];
            int[][] blockV = new int[size][size];

            for (int e = 0; e < size; e++)
            {
                for (int f = 0; f < size; f++)
                {
                    if (xcount % newWidth == 0 && x != 0 && xcount != 0)
                    {
                        ycount += size;
                        xcount = 0;
                    }
            
                    blockY[e][f] = YValues[(ycount + e) * width + ((x % (newWidth/size)) * size) + f];
                    blockU[e][f] = UValues[(ycount + e) * width + ((x % (newWidth/size)) * size) + f];
                    blockV[e][f] = VValues[(ycount + e) * width + ((x % (newWidth/size)) * size) + f];
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
	
	// performs intra prediction on 4x4 block (5x5 block used to get neighbouring info
	public static ArrayList<int[][]> intra(int f[][]) {
		ArrayList<int[][]> result = new ArrayList<int[][]>();
		int[][] current = new int[4][4];

		// vertical mode, set all predicted pixels in column A to be pixel A, B as B, & etc.
		for (int y = 0; y < 4; y++) {
			current[0][y] = f[1][0]; // f[1][0] == pixel A 	[M][A][B][C][D]
			current[1][y] = f[2][0];				 	   	 // [I] |  |  |  | 
			current[2][y] = f[3][0];		   			  	 // [J] |  |  |  | 
			current[3][y] = f[4][0];					 	 // [K] |  |  |  | 
		}												  	 // [L] V  V  V  V 
		result.add(current);
		
		// horizontal mode, set all predicted in row I as pixel I, J as J, etc.
		for (int x = 0; x < 4; x++) {
			current[x][0] = f[0][1];// f[0][1] == pixel I 	[M][A][B][C][D]
			current[x][1] = f[0][2];				 	   	 // [I] --------->
			current[x][2] = f[0][3];				 	   	 // [J] --------->
			current[x][3] = f[0][4];				 	   	 // [K] --------->
		}											 	 	 // [L] --------->
		result.add(current);
		
		// average mode, set all predicted in 4x4 block as average of 8 neighbours (A-D, I-L)
		int average = (f[1][0] + f[2][0] + f[3][0] + f[4][0] + f[0][1] + f[0][2] + f[0][3] + f[0][4]) / 8;
		
		for (int x = 0; x < 4; x++) {
			current[x][0] = average;
			current[x][1] = average;
			current[x][2] = average;
			current[x][3] = average;
		}
		result.add(current);
		
		return result;
	}
	
	public static int[][] residuals(ArrayList<int[][]> predicted, int[][] actual) {
		ArrayList<int[][]> residuals = new ArrayList<int[][]>();
		int[] errorSum = new int[predicted.size()];
		int[][] error = new int[4][4];
		int[][] current = new int[4][4];
		int ideal = 0;
		
		// get residual values (prediction error) and save
		for (int q = 0; q < predicted.size(); q++) {
			current = predicted.get(q);
			
			for (int y = 0; y < 4; y++) {
				for (int x = 0; x < 4; x++) {
					error[x][y] = actual[x][y] - current[x][y];
					errorSum[q] += error[x][y];
				}
			}
			residuals.add(error);
		}
		
		// find index of smallest error
		for (int q = 1; q < predicted.size(); q++) {
			if (errorSum[q] < errorSum[ideal]) {
				ideal = q;
			}
		}
		
		return residuals.get(ideal);
	}
	
	public static int[][] integerTransform(int [][] f) {
		int [][] H = {{1,1,1,1},{2,1,-1,-2},{1,-1,-1,1},{1,-2,2,-1}};
		int [][] Ht = {{1,2,1,1},{1,1,-1,-2},{1,-1,-1,2},{1,-2,1,-1}};
		double [][] M = {{13107,5243,8066},{11916,4660,7490},{10082,4194,6554},{9362,3647,5825},{8192,3355,5243},{7282,2893,4559}};
		double [][] intRes = new double[4][4];
		double [][] intRes2 = new double[4][4];
		int [][] res = new int[4][4];
		int QP = 6;
		
		//int [][] f = {{72,82,85,79},{74,75,86,82},{84,73,78,80},{77,81,76,84}};
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
                for (int k = 0; k < 4; k++) {
                	intRes[i][j] += f[i][k] * Ht[k][j];
                }
			}
		}
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
                for (int k = 0; k < 4; k++) {
                	intRes2[i][j] += H[i][k] * intRes[k][j];
                }
			}
		}
		
		if (QP >= 0 && QP < 6) {
			res[0][0] = (int) Math.round(intRes2[0][0]*(M[QP][0]/(1 << 15)));
			res[0][1] = (int) Math.round(intRes2[0][1]*(M[QP][2]/(1 << 15)));
			res[0][2] = (int) Math.round(intRes2[0][2]*(M[QP][0]/(1 << 15)));
			res[0][3] = (int) Math.round(intRes2[0][3]*(M[QP][2]/(1 << 15)));
			res[1][0] = (int) Math.round(intRes2[1][0]*(M[QP][2]/(1 << 15)));
			res[1][1] = (int) Math.round(intRes2[1][1]*(M[QP][1]/(1 << 15)));
			res[1][2] = (int) Math.round(intRes2[1][2]*(M[QP][2]/(1 << 15)));
			res[1][3] = (int) Math.round(intRes2[1][3]*(M[QP][1]/(1 << 15)));
			res[2][0] = (int) Math.round(intRes2[2][0]*(M[QP][0]/(1 << 15)));
			res[2][1] = (int) Math.round(intRes2[2][1]*(M[QP][2]/(1 << 15)));
			res[2][2] = (int) Math.round(intRes2[2][2]*(M[QP][0]/(1 << 15)));
			res[2][3] = (int) Math.round(intRes2[2][3]*(M[QP][2]/(1 << 15)));
			res[3][0] = (int) Math.round(intRes2[3][0]*(M[QP][2]/(1 << 15)));
			res[3][1] = (int) Math.round(intRes2[3][1]*(M[QP][1]/(1 << 15)));
			res[3][2] = (int) Math.round(intRes2[3][2]*(M[QP][2]/(1 << 15)));
			res[3][3] = (int) Math.round(intRes2[3][3]*(M[QP][1]/(1 << 15)));
		}
		else {
			res[0][0] = (int) Math.round(intRes2[0][0]*(M[QP%6][0]/(1 << QP/6)/(1 << 15)));
			res[0][1] = (int) Math.round(intRes2[0][1]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[0][2] = (int) Math.round(intRes2[0][2]*(M[QP%6][0]/(1 << QP/6)/(1 << 15)));
			res[0][3] = (int) Math.round(intRes2[0][3]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[1][0] = (int) Math.round(intRes2[1][0]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[1][1] = (int) Math.round(intRes2[1][1]*(M[QP%6][1]/(1 << QP/6)/(1 << 15)));
			res[1][2] = (int) Math.round(intRes2[1][2]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[1][3] = (int) Math.round(intRes2[1][3]*(M[QP%6][1]/(1 << QP/6)/(1 << 15)));
			res[2][0] = (int) Math.round(intRes2[2][0]*(M[QP%6][0]/(1 << QP/6)/(1 << 15)));
			res[2][1] = (int) Math.round(intRes2[2][1]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[2][2] = (int) Math.round(intRes2[2][2]*(M[QP%6][0]/(1 << QP/6)/(1 << 15)));
			res[2][3] = (int) Math.round(intRes2[2][3]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[3][0] = (int) Math.round(intRes2[3][0]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[3][1] = (int) Math.round(intRes2[3][1]*(M[QP%6][1]/(1 << QP/6)/(1 << 15)));
			res[3][2] = (int) Math.round(intRes2[3][2]*(M[QP%6][2]/(1 << QP/6)/(1 << 15)));
			res[3][3] = (int) Math.round(intRes2[3][3]*(M[QP%6][1]/(1 << QP/6)/(1 << 15)));
		}
		
		return res;
	}
	
	public static int[][] inverseIntegerTransform(/*int [][] F*/) {
		double [][] HInv = {{1,1,1,1/2},{1,1/2,-1,-1},{1,-1/2,-1,1},{1,-1,1,-1/2}};
		double [][] HtInv = {{1,1,1,1},{1,1/2,-1/2,-1},{1,-1,-1,1},{1/2,-1,1,-1/2}};
		double [][] V = {{10,16,13},{11,18,14},{13,20,16},{14,23,18},{16,25,20},{18,29,23}};
		double [][] intRes = new double[4][4];
		double [][] intRes2 = new double[4][4];
		double [][] intRes3 = new double[4][4];
		int [][] res = new int[4][4];
		int QP = 0;
		
		int [][] F = {{507,-12,-2,2},{0,-7,-14,5},{2,0,-8,-11},{-1,8,4,3}};
		
		if (QP >= 0 && QP < 6) {
			intRes[0][0] = F[0][0]*V[QP][0];
			intRes[0][1] = F[0][1]*V[QP][2];
			intRes[0][2] = F[0][2]*V[QP][0];
			intRes[0][3] = F[0][3]*V[QP][2];
			intRes[1][0] = F[1][0]*V[QP][2];
			intRes[1][1] = F[1][1]*V[QP][1];
			intRes[1][2] = F[1][2]*V[QP][2];
			intRes[1][3] = F[1][3]*V[QP][1];
			intRes[2][0] = F[2][0]*V[QP][0];
			intRes[2][1] = F[2][1]*V[QP][2];
			intRes[2][2] = F[2][2]*V[QP][0];
			intRes[2][3] = F[2][3]*V[QP][2];
			intRes[3][0] = F[3][0]*V[QP][2];
			intRes[3][1] = F[3][1]*V[QP][1];
			intRes[3][2] = F[3][2]*V[QP][2];
			intRes[3][3] = F[3][3]*V[QP][1];
		}
		else {
			intRes[0][0] = F[0][0]*(V[QP%6][0]*(1 << QP/6));
			intRes[0][1] = F[0][1]*(V[QP%6][2]*(1 << QP/6));
			intRes[0][2] = F[0][2]*(V[QP%6][0]*(1 << QP/6));
			intRes[0][3] = F[0][3]*(V[QP%6][2]*(1 << QP/6));
			intRes[1][0] = F[1][0]*(V[QP%6][2]*(1 << QP/6));
			intRes[1][1] = F[1][1]*(V[QP%6][1]*(1 << QP/6));
			intRes[1][2] = F[1][2]*(V[QP%6][2]*(1 << QP/6));
			intRes[1][3] = F[1][3]*(V[QP%6][1]*(1 << QP/6));
			intRes[2][0] = F[2][0]*(V[QP%6][0]*(1 << QP/6));
			intRes[2][1] = F[2][1]*(V[QP%6][2]*(1 << QP/6));
			intRes[2][2] = F[2][2]*(V[QP%6][0]*(1 << QP/6));
			intRes[2][3] = F[2][3]*(V[QP%6][2]*(1 << QP/6));
			intRes[3][0] = F[3][0]*(V[QP%6][2]*(1 << QP/6));
			intRes[3][1] = F[3][1]*(V[QP%6][1]*(1 << QP/6));
			intRes[3][2] = F[3][2]*(V[QP%6][2]*(1 << QP/6));
			intRes[3][3] = F[3][3]*(V[QP%6][1]*(1 << QP/6));
		}
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
                for (int k = 0; k < 4; k++) {
                	intRes2[i][j] += intRes[i][k] * HtInv[k][j];
                }
			}
		}
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
                for (int k = 0; k < 4; k++) {
                	intRes3[i][j] += HInv[i][k] * intRes2[k][j];
                }
			}
		}

		for(int i=0; i<4; i++) {
			for(int j=0; j<4; j++) {
				res[i][j] = (int) Math.round(intRes3[i][j]/(1 << 6));
			}
		}
		
		return res;
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
