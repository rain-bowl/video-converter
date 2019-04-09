package termproject;

import java.awt.AWTException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
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
	JButton buttonOpen, buttonQP, buttonQPP, buttonRefresh, buttonIFrame, buttonPFrame, buttonCurrentIFrame, buttonCurrentPFrame;  

	//Creates new file chooser object
	JFileChooser m_fc = new JFileChooser();

	//Variable declaration for video frame extraction
	FFmpegFrameGrabber rawVideo;

	//Array list containing buffered images of the video frames
	ArrayList<BufferedImage> videoFrames = new ArrayList<BufferedImage>();

	//Array list containing the images separated into I, P, and B frames
	ArrayList<BufferedImage> videoIFrames = new ArrayList<BufferedImage>();
	ArrayList<BufferedImage> videoPFrames = new ArrayList<BufferedImage>();
	ArrayList<BufferedImage> videoBFrames = new ArrayList<BufferedImage>();

	//Arraylist of all the I, P, B frames containing an Arraylist of (Y,U,V) for each frame
	ArrayList<ArrayList<int[]>> YUVIFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> YUVPFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> YUVBFrames = new ArrayList<ArrayList<int[]>>();

	//Arraylist of all the I, P, B frames containing an Arraylist of (Y, 4:2:0 U, 4:2:0 V) for each frame
	ArrayList<ArrayList<int[]>> ChromaIFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> ChromaPFrames = new ArrayList<ArrayList<int[]>>();
	ArrayList<ArrayList<int[]>> ChromaBFrames = new ArrayList<ArrayList<int[]>>();

	//Arraylist of all I frames containing the blocks of Y,U,V frames
	ArrayList<ArrayList<ArrayList<int[][]>>> BlockerIFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();
	ArrayList<ArrayList<ArrayList<int[][]>>> BlockerPFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();

	//Arraylist of all I frames containing the intra-predicted frames
	ArrayList<ArrayList<ArrayList<int[][]>>> PredictedIFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();
	
	//Arraylist of all I frames containing the intra-predicted frames
	ArrayList<ArrayList<ArrayList<int[][]>>> ResidualIFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();
	ArrayList<ArrayList<ArrayList<int[][]>>> ResidualPFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();

	//Arraylist of all I frames containing the integer transformed frames
	ArrayList<ArrayList<ArrayList<int[][]>>> IntegerTransformIFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();
	ArrayList<ArrayList<ArrayList<int[][]>>> IntegerTransformPFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();
	
	//Arraylist of all I frames containing the integer transformed frames
	ArrayList<ArrayList<ArrayList<int[][]>>> IntegerInverseTransformIFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();
	ArrayList<ArrayList<ArrayList<int[][]>>> IntegerInverseTransformPFrames = new ArrayList<ArrayList<ArrayList<int[][]>>>();

	//Video Properties
	int width, height;
	int YValues[], UValues[], VValues[], inputValues[];

	// Set IframeDistance
	int IframeDistance = 9;

	// MAX_FRAMES must be a number of (9*n)+1 frames
	int MAX_FRAMES = 10;

	//Set the Quality Factor
	int QP = 6;
	int FRAME_NUM = 0;

	public JPanel createContentPane(){	   

		totalGUI = new JPanel();
		totalGUI.setLayout(null);

		buttonGUI = new JPanel();
		buttonGUI.setLayout(null);
		buttonGUI.setLocation(10, 10);
		buttonGUI.setSize(1920, 60);
		totalGUI.add(buttonGUI);

		buttonOpen = new JButton("1. OPEN FILE");
		buttonOpen.setLocation(0, 0);
		buttonOpen.setSize(150, 40);
		buttonOpen.addActionListener(this);
		buttonGUI.add(buttonOpen);
		
		buttonQP = new JButton("QP I-VALUE = "+QP);
		buttonQP.setLocation(160, 0);
		buttonQP.setSize(150, 40);
		buttonQP.addActionListener(this);
		buttonGUI.add(buttonQP);
		
		buttonIFrame = new JButton("2. ANALYZE IFRAME");
		buttonIFrame.setLocation(320, 0);
		buttonIFrame.setSize(150, 40);
		buttonIFrame.addActionListener(this);
		buttonGUI.add(buttonIFrame);
		
		buttonCurrentIFrame = new JButton("I-FRAME SELECTED = "+FRAME_NUM);
		buttonCurrentIFrame.setLocation(480, 0);
		buttonCurrentIFrame.setSize(180, 40);
		buttonCurrentIFrame.addActionListener(this);
		buttonGUI.add(buttonCurrentIFrame);
		
		buttonQPP = new JButton("QP P-VALUE = "+QP);
		buttonQPP.setLocation(670, 0);
		buttonQPP.setSize(150, 40);
		buttonQPP.addActionListener(this);
		buttonGUI.add(buttonQPP);
		
		buttonPFrame = new JButton("3. ANALYZE PFRAME");
		buttonPFrame.setLocation(830, 0);
		buttonPFrame.setSize(150, 40);
		buttonPFrame.addActionListener(this);
		buttonGUI.add(buttonPFrame);
		
		buttonCurrentPFrame = new JButton("P-FRAME SELECTED = "+FRAME_NUM);
		buttonCurrentPFrame.setLocation(990, 0);
		buttonCurrentPFrame.setSize(180, 40);
		buttonCurrentPFrame.addActionListener(this);
		buttonGUI.add(buttonCurrentPFrame);
		
		
		buttonRefresh = new JButton("REFRESH SCREEN");
		buttonRefresh.setLocation(1720, 0);
		buttonRefresh.setSize(180, 40);
		buttonRefresh.addActionListener(this);
		buttonGUI.add(buttonRefresh);

//		String inputStringQP = JOptionPane.showInputDialog(null, "ENTER QP VALUE");
//        QP = Integer.parseInt(inputStringQP);
//        buttonQP.setText("QP VALUE="+QP);
        
		String inputStringMaxFrames = JOptionPane.showInputDialog(null, "ENTER NUMBER OF FRAMES TO ENCODE (KEEP NUM = (9*N)+1)");
        MAX_FRAMES = Integer.parseInt(inputStringMaxFrames);
		
		totalGUI.setOpaque(true);

		return totalGUI;
	}
	
	public void reset() {
		//Reset Arraylists
		videoFrames.clear();
		videoIFrames.clear();
		videoPFrames.clear();
		videoBFrames.clear();
		YUVIFrames.clear();
		YUVPFrames.clear();
		YUVBFrames.clear();
		ChromaIFrames.clear();
		ChromaPFrames.clear();
		ChromaBFrames.clear();
		BlockerIFrames.clear();
		BlockerPFrames.clear();
		PredictedIFrames.clear();
		ResidualIFrames.clear();
		ResidualPFrames.clear();
		IntegerTransformIFrames.clear();
		IntegerTransformPFrames.clear();
		IntegerInverseTransformIFrames.clear();
		IntegerInverseTransformPFrames.clear();
	}

	public void actionPerformed(ActionEvent evnt) {
		//Performs action if "buttonOpen" is clicked
		if(evnt.getSource() == buttonOpen){
			//Opens window to choose video file
			int result = m_fc.showOpenDialog(totalGUI);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = m_fc.getSelectedFile();
				String filename = file.toString();

				// Grabs the filename for video frame extraction
				rawVideo = new FFmpegFrameGrabber(filename);

				reset();
		
				// Extract video frames and places the frames into the correct section
				extractFrames();
				separateFrames();
				
				JOptionPane.showMessageDialog(totalGUI,
					    "File Successfully Opened",
					    "Success",
					    JOptionPane.PLAIN_MESSAGE);
		
			}
		}
		else if(evnt.getSource() == buttonQP) {
	        String inputStringQP = JOptionPane.showInputDialog(null, "ENTER NEW QP IFRAME VALUE");
	        QP = Integer.parseInt(inputStringQP);
	        
	        buttonQP.setText("QP I-VALUE="+QP);
	        buttonQPP.setText("QP P-VALUE="+QP);
	        
	        IntegerTransformIFrames.clear();
	        IntegerInverseTransformIFrames.clear();
	        IFrameTransform();
	        test();
	        totalGUI.revalidate();
	        totalGUI.repaint();
	        
		}
		else if(evnt.getSource() == buttonQPP) {
	        String inputStringQP = JOptionPane.showInputDialog(null, "ENTER NEW QP PFRAME VALUE");
	        QP = Integer.parseInt(inputStringQP);
	        
	        buttonQP.setText("QP I-VALUE="+QP);
	        buttonQPP.setText("QP P-VALUE="+QP);
	        
	        IntegerTransformPFrames.clear();
	        IntegerInverseTransformPFrames.clear();
	        PFrameTransform();
	        testP();
	        totalGUI.revalidate();
	        totalGUI.repaint();
	        
		}
		else if(evnt.getSource() == buttonCurrentIFrame) {
	        String inputStringFrameNum = JOptionPane.showInputDialog(null, "ENTER I-FRAME NUMBER BETWEEN 0 - "+(videoIFrames.size()-1));
	        FRAME_NUM = Integer.parseInt(inputStringFrameNum);
	        
	        buttonCurrentIFrame.setText("I-FRAME SELECTED = "+FRAME_NUM);
	        test();
			totalGUI.revalidate();
			totalGUI.repaint();
	        
		}
		else if(evnt.getSource() == buttonCurrentPFrame) {
	        String inputStringFrameNum = JOptionPane.showInputDialog(null, "ENTER P-FRAME NUMBER BETWEEN 0 - "+(videoPFrames.size()-1));
	        FRAME_NUM = Integer.parseInt(inputStringFrameNum);
	        
	        buttonCurrentPFrame.setText("P-FRAME SELECTED = "+FRAME_NUM);
	        testP();
			totalGUI.revalidate();
			totalGUI.repaint();
		}
		else if(evnt.getSource() == buttonIFrame) {
			
			//Reset
			YUVIFrames.clear();
			ChromaIFrames.clear();
			BlockerIFrames.clear();
			PredictedIFrames.clear();
			ResidualIFrames.clear();
			
			// I-Frames Encoding
			convertYUV(0);
			subSampling(YUVIFrames, 0);

			// Creates 4x4 MB for all frames
			for(int i=0; i < ChromaIFrames.size(); i++) {
				BlockerIFrames.add(blocker(ChromaIFrames.get(i), 4));
			}

			// Gets a frame
			for(int i=0; i < BlockerIFrames.size(); i++) {

				ArrayList<ArrayList<int[][]>> currentFrame = new ArrayList<ArrayList<int[][]>>();

				// Performs 4x4 Intra-prediction for all Y,U,V 4x4 blocks
				//Gets the YUV arrays in a frame
				//System.out.println(BlockerIFrames.get(i).size());
				for(int j=0; j < BlockerIFrames.get(i).size(); j++) {

					ArrayList<int[][]> Yres = new ArrayList<int[][]>();
					ArrayList<int[][]> Ures = new ArrayList<int[][]>();
					ArrayList<int[][]> Vres = new ArrayList<int[][]>();

					//Gets the 4x4 blocks in each Y,U,V frame
					//System.out.println(BlockerIFrames.get(i).get(j).size());
					for(int k=0; k < BlockerIFrames.get(i).get(j).size(); k++) {

						//For each block, do intra-prediction
						//System.out.println(BlockerIFrames.get(i).get(j).get(k));
						ArrayList<int[][]> blockRes = intra(BlockerIFrames.get(i).get(j).get(k));

						//Choose the best predictor
						int[][] chosenPrediction = residuals(blockRes, BlockerIFrames.get(i).get(j).get(k));

						if(j == 0) {
							Yres.add(chosenPrediction);
						}
						else if(j == 1) {
							Ures.add(chosenPrediction);
						}
						else {
							Vres.add(chosenPrediction);
						}
					}

					if(j == 0) {
						currentFrame.add(Yres);
					}
					else if(j == 1) {
						currentFrame.add(Ures);
					}
					else {
						currentFrame.add(Vres);
					}
				}

				PredictedIFrames.add(currentFrame);
			}

			// Gets a frame
			for(int i=0; i < PredictedIFrames.size(); i++) {

				ArrayList<ArrayList<int[][]>> currentFrame = new ArrayList<ArrayList<int[][]>>();

				// Performs 4x4 Intra-prediction for all Y,U,V 4x4 blocks
				//Gets the YUV arrays in a frame
				System.out.println(PredictedIFrames.get(i).size());
				for(int j=0; j < PredictedIFrames.get(i).size(); j++) {

					ArrayList<int[][]> Yres = new ArrayList<int[][]>();
					ArrayList<int[][]> Ures = new ArrayList<int[][]>();
					ArrayList<int[][]> Vres = new ArrayList<int[][]>();

					//Gets the 4x4 blocks in each Y,U,V frame
					System.out.println(PredictedIFrames.get(i).get(j).size());
					for(int k=0; k < PredictedIFrames.get(i).get(j).size(); k++) {

						//For each block, do intra-prediction
						int chosenPrediction[][] = new int[4][4];
						
						for(int l=0; l < 4; l++) {
							for(int m=0; m < 4; m++) {
								chosenPrediction[l][m] = BlockerIFrames.get(i).get(j).get(k)[l][m]-PredictedIFrames.get(i).get(j).get(k)[l][m];
							}
						}

						if(j == 0) {
							Yres.add(chosenPrediction);
						}
						else if(j == 1) {
							Ures.add(chosenPrediction);
						}
						else {
							Vres.add(chosenPrediction);
						}
					}

					if(j == 0) {
						currentFrame.add(Yres);
					}
					else if(j == 1) {
						currentFrame.add(Ures);
					}
					else {
						currentFrame.add(Vres);
					}
				}

				ResidualIFrames.add(currentFrame);
			}
			
			IFrameTransform();
			
			test();
			totalGUI.revalidate();
		}
		else if(evnt.getSource() == buttonPFrame) {
			
			convertYUV(1);
			subSampling(YUVPFrames, 1);
			
			//System.out.println("buttonPFrame");
			//System.out.println("ChromaPFrames.size()/2: " + ChromaPFrames.size()/2);
			// Creates 4x4 MB for all frames
			for(int j=0; j < ChromaPFrames.size(); j++) {
				BlockerPFrames.add(blocker(ChromaPFrames.get(j), 8));
//				System.out.println("ChromaPFrames.get(j): " + ChromaPFrames.get(j));
			}
			
			for(int i=0; i<BlockerPFrames.size(); i++) {
				
				ArrayList<int[]> currentFrame = new ArrayList<int[]>();
				currentFrame.clear();
				
				//Even Pframes
					if(i % 2 == 0) {
						
						ArrayList<int[]> unblockedCurrentIFrame = unblocker(IntegerInverseTransformIFrames.get(i/2));
						
						ArrayList<ArrayList<int[][]>> reblockedIFrame = blocker(unblockedCurrentIFrame, 8);
						ArrayList<ArrayList<int[][]>> CurrentPFrame = BlockerPFrames.get(i);
						
						ArrayList<int[]> unblockedCurrentPFrame = unblocker(CurrentPFrame);
						
						//Do motion estimation here
						ArrayList<Integer> vector = motionLogSearch(CurrentPFrame, reblockedIFrame);
						
						for(int m=0; m<unblockedCurrentPFrame.size(); m++) {
							
							int[] Yres = new int[width*height];
							int[] Ures = new int[width*height];
							int[] Vres = new int[width*height];
							
							int PResidual[] = new int[width*height];
							int PNewPos[] = new int[width*height];
							
							//Moves current pframe to the new location with the offset motion vector
							for(int k=0; k<width; k++) {
								for(int l=0; l<height; l++) {
									
									if((l*(width+vector.get(1))+k+vector.get(1)) >= (width*height)) {
										PNewPos[(l*width)+k] = 0;
									}
									else {
										PNewPos[(l*width)+k] = unblockedCurrentPFrame.get(m)[(l*(width+vector.get(1))+k+vector.get(1))];
									}
								}
							}
							
							//Take the difference between the current iframe and the motion compensated pframe 
							for(int o=0; o<width; o++) {
								for(int n=0; n<height; n++) {
									PResidual[(n*width)+o] = unblockedCurrentIFrame.get(m)[(n*width)+o]-unblockedCurrentPFrame.get(m)[(n*width)+o];

									if(m == 0) {
										Yres = PResidual;
									}
									else if(m == 1) {
										Ures = PResidual;
									}
									else {
										Vres = PResidual;
									}
								}
							}
							
							if(m == 0) {
								currentFrame.add(Yres);
							}
							else if(m == 1) {
								currentFrame.add(Ures);
							}
							else {
								currentFrame.add(Vres);
							}
						}
						
						//ArrayList<ArrayList<ArrayList<int[][]>>>
//						ArrayList<ArrayList<int[][]>> reblockedPResidual = new ArrayList<ArrayList<int[][]>>();
//						
//						//reblock the pframe so that its in 4x4 blocks for the integer transform
						ArrayList<ArrayList<int[][]>> reblockedPResidualYUV = blocker(currentFrame, 4);
						ResidualPFrames.add(reblockedPResidualYUV);
						PFrameTransform();
						
				}
					// Odd P-frames
					else{
						ArrayList<int[]> unblockedPreviousPFrame = unblocker(IntegerInverseTransformPFrames.get(i-1));
						
						ArrayList<ArrayList<int[][]>> reblockedPFrame = blocker(unblockedPreviousPFrame, 8);
						ArrayList<ArrayList<int[][]>> CurrentPFrame = BlockerPFrames.get(i);
						
						ArrayList<int[]> unblockedCurrentPFrame = unblocker(CurrentPFrame);
						
						//Do motion estimation here
						ArrayList<Integer> vector = motionLogSearch(CurrentPFrame, reblockedPFrame);
						
						for(int m=0; m<unblockedCurrentPFrame.size(); m++) {
							
							int[] Yres = new int[width*height];
							int[] Ures = new int[width*height];
							int[] Vres = new int[width*height];
							
							int PResidual[] = new int[width*height];
							int PNewPos[] = new int[width*height];
							
							//Moves current pframe to the new location with the offset motion vector
							for(int k=0; k<width; k++) {
								for(int l=0; l<height; l++) {
									
									if((l*(width+vector.get(1))+k+vector.get(1)) >= (width*height)) {
										PNewPos[(l*width)+k] = 0;
									}
									else {
										PNewPos[(l*width)+k] = unblockedCurrentPFrame.get(m)[(l*(width+vector.get(1))+k+vector.get(1))];
									}
								}
							}
							
							//Take the difference between the current iframe and the motion compensated pframe 
							for(int o=0; o<width; o++) {
								for(int n=0; n<height; n++) {
									PResidual[(n*width)+o] = unblockedPreviousPFrame.get(m)[(n*width)+o]-unblockedCurrentPFrame.get(m)[(n*width)+o];

									if(m == 0) {
										Yres = PResidual;
									}
									else if(m == 1) {
										Ures = PResidual;
									}
									else {
										Vres = PResidual;
									}
								}
							}
							
							if(m == 0) {
								currentFrame.add(Yres);
							}
							else if(m == 1) {
								currentFrame.add(Ures);
							}
							else {
								currentFrame.add(Vres);
							}
						}
						
						ArrayList<ArrayList<int[][]>> reblockedPResidualYUV = blocker(currentFrame, 4);
						ResidualPFrames.add(reblockedPResidualYUV);
						PFrameTransform();
						
					}
			}

			testP();
			totalGUI.revalidate();
			totalGUI.repaint();
		}
    	else if (evnt.getSource() == buttonRefresh) {
    	    totalGUI.revalidate();
    	    totalGUI.repaint();
    	}
	}

	public void IFrameTransform() {
		for(int i=0; i < ResidualIFrames.size(); i++) {
			ArrayList<ArrayList<int[][]>> currentFrame = new ArrayList<ArrayList<int[][]>>();

			// Performs 4x4 integer transform on all blocks
			//Gets the predicted Y,U,V arrays in a frame
			for(int j=0; j < ResidualIFrames.get(i).size(); j++) {

				ArrayList<int[][]> Yres = new ArrayList<int[][]>();
				ArrayList<int[][]> Ures = new ArrayList<int[][]>();
				ArrayList<int[][]> Vres = new ArrayList<int[][]>();

				//Gets the 4x4 blocks in each Y,U,V frame
				for(int k=0; k < ResidualIFrames.get(i).get(j).size(); k++) {

					//For each block, do intra-prediction
					int[][] blockTransformed = integerTransform(ResidualIFrames.get(i).get(j).get(k), QP);

					if(j == 0) {
						Yres.add(blockTransformed);
					}
					else if(j == 1) {
						Ures.add(blockTransformed);
					}
					else {
						Vres.add(blockTransformed);
					}
				}

				if(j == 0) {
					currentFrame.add(Yres);
				}
				else if(j == 1) {
					currentFrame.add(Ures);
				}
				else {
					currentFrame.add(Vres);
				}
			}

			IntegerTransformIFrames.add(currentFrame);
		}

		
		for(int i=0; i < IntegerTransformIFrames.size(); i++) {

			ArrayList<ArrayList<int[][]>> currentFrame = new ArrayList<ArrayList<int[][]>>();

			// Performs 4x4 integer transform on all blocks
			//Gets the predicted Y,U,V arrays in a frame
			for(int j=0; j < IntegerTransformIFrames.get(i).size(); j++) {

				ArrayList<int[][]> Yres = new ArrayList<int[][]>();
				ArrayList<int[][]> Ures = new ArrayList<int[][]>();
				ArrayList<int[][]> Vres = new ArrayList<int[][]>();

				//Gets the 4x4 blocks in each Y,U,V frame
				for(int k=0; k < IntegerTransformIFrames.get(i).get(j).size(); k++) {

					//For each block, do intra-prediction
					int[][] blockTransformed = inverseIntegerTransform(IntegerTransformIFrames.get(i).get(j).get(k), QP);

					if(j == 0) {
						Yres.add(blockTransformed);
					}
					else if(j == 1) {
						Ures.add(blockTransformed);
					}
					else {
						Vres.add(blockTransformed);
					}
				}

				if(j == 0) {
					currentFrame.add(Yres);
				}
				else if(j == 1) {
					currentFrame.add(Ures);
				}
				else {
					currentFrame.add(Vres);
				}
			}

			IntegerInverseTransformIFrames.add(currentFrame);
		}
	}
	
	public void PFrameTransform() {
		IntegerTransformPFrames.clear();
		IntegerInverseTransformPFrames.clear();
		
		for(int i=0; i < ResidualPFrames.size(); i++) {
			ArrayList<ArrayList<int[][]>> currentFrame = new ArrayList<ArrayList<int[][]>>();

			// Performs 4x4 integer transform on all blocks
			//Gets the predicted Y,U,V arrays in a frame
			for(int j=0; j < ResidualPFrames.get(i).size(); j++) {

				ArrayList<int[][]> Yres = new ArrayList<int[][]>();
				ArrayList<int[][]> Ures = new ArrayList<int[][]>();
				ArrayList<int[][]> Vres = new ArrayList<int[][]>();

				//Gets the 4x4 blocks in each Y,U,V frame
				for(int k=0; k < ResidualPFrames.get(i).get(j).size(); k++) {

					//For each block, do intra-prediction
					int[][] blockTransformed = integerTransform(ResidualPFrames.get(i).get(j).get(k), QP);

					if(j == 0) {
						Yres.add(blockTransformed);
					}
					else if(j == 1) {
						Ures.add(blockTransformed);
					}
					else {
						Vres.add(blockTransformed);
					}
				}

				if(j == 0) {
					currentFrame.add(Yres);
				}
				else if(j == 1) {
					currentFrame.add(Ures);
				}
				else {
					currentFrame.add(Vres);
				}
			}

			IntegerTransformPFrames.add(currentFrame);
		}

		
		for(int i=0; i < IntegerTransformPFrames.size(); i++) {

			ArrayList<ArrayList<int[][]>> currentFrame = new ArrayList<ArrayList<int[][]>>();

			// Performs 4x4 integer transform on all blocks
			//Gets the predicted Y,U,V arrays in a frame
			for(int j=0; j < IntegerTransformPFrames.get(i).size(); j++) {

				ArrayList<int[][]> Yres = new ArrayList<int[][]>();
				ArrayList<int[][]> Ures = new ArrayList<int[][]>();
				ArrayList<int[][]> Vres = new ArrayList<int[][]>();

				//Gets the 4x4 blocks in each Y,U,V frame
				for(int k=0; k < IntegerTransformPFrames.get(i).get(j).size(); k++) {

					//For each block, do intra-prediction
					int[][] blockTransformed = inverseIntegerTransform(IntegerTransformPFrames.get(i).get(j).get(k), QP);

					if(j == 0) {
						Yres.add(blockTransformed);
					}
					else if(j == 1) {
						Ures.add(blockTransformed);
					}
					else {
						Vres.add(blockTransformed);
					}
				}

				if(j == 0) {
					currentFrame.add(Yres);
				}
				else if(j == 1) {
					currentFrame.add(Ures);
				}
				else {
					currentFrame.add(Vres);
				}
			}

			IntegerInverseTransformPFrames.add(currentFrame);
		}
	}
	
	/*
	 * Extracts Frames from video and places output in Arraylist of bufferedImages
	 */
	public void extractFrames() {
		try {
			rawVideo.start();

			//Grabs the first 50 frames of the video
			for (int i = 0 ; i < MAX_FRAMES ; i++) {

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

	/*
	 * Separates the extracted frames and sorts them into I, P, B frames for further processing
	 * Output: Places each corresponding I, P, B frame into its own Arraylist of bufferedImages
	 */
	public void separateFrames() {

		//IBBPBBPBBI
		int Bframe = 0;

		for(int i=0; i < videoFrames.size(); i++) {
			if(i % IframeDistance == 0 || i == 0) {
				videoIFrames.add(videoFrames.get(i));
				Bframe = 1;
			}
			else if(Bframe == 1 || Bframe == 2) {
				videoBFrames.add(videoFrames.get(i));
				Bframe++;
			}
			else {
				videoPFrames.add(videoFrames.get(i));
				Bframe = 1;
			}

		}
	}

	/*
	 * Converts all the frames in an Arraylist of bufferedImages to YUV
	 * Input: integer currentMoethod which tells the function where to output the YUV frames: 0 = I frames, 1 = P frames, 2 = B frames
	 * Output: creates an Arraylist containing the separate Y, U ,V data -> places this Arraylist into an Arraylist containing all the (I/P/B) frames
	 */
	public void convertYUV(int currentMethod) {
		
		ArrayList<BufferedImage> videoFramesTemp = new ArrayList<BufferedImage>();
		
		if(currentMethod == 0) {
			videoFramesTemp = videoIFrames;
		}
		else if(currentMethod == 1) {
			videoFramesTemp = videoPFrames;
		}
		else {
			videoFramesTemp = videoBFrames;
		}
		
		for(int i = 0; i < videoFramesTemp.size(); i++) {
			width = videoFramesTemp.get(i).getWidth(null);
			height = videoFramesTemp.get(i).getHeight(null);

			ArrayList<int[]> currentYUVFrame = new ArrayList<int[]>();
			inputValues = new int[width*height];
			YValues = new int[width*height];
			UValues = new int[width*height];
			VValues = new int[width*height];

			// Grab Original Image Pixel Values
			PixelGrabber grabber = new PixelGrabber(videoFramesTemp.get(i).getSource(), 0, 0, width, height, inputValues, 0, width);
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
		int newWidth = ((int) Math.floor(width/4)) * 4;
		int newHeight = ((int) Math.floor(height/4)) * 4;
		
		for(int i=0; i<inputYUVFrames.size(); i++) {
			ArrayList<int[]> outputRes = new ArrayList<int[]>();

			// set UV values for chroma use
			int[] UChroma = new int[newWidth*newHeight];
			int[] VChroma = new int[newWidth*newHeight];

			// adding every other U and V value to a block of 4
			for (int y = 1; y < newHeight; y+=2)
			{
				for (int x = 1; x < newWidth; x+=2)
				{
					UChroma[((y - 1)*newWidth + (x - 1))] = (inputYUVFrames.get(i).get(1)[(y - 1)*newWidth + (x - 1)]);
					UChroma[((y - 1)*newWidth + x)] = (inputYUVFrames.get(i).get(1)[(y - 1)*newWidth + (x - 1)]);
					UChroma[(y*newWidth + (x - 1))] = (inputYUVFrames.get(i).get(1)[(y - 1)*newWidth + (x - 1)]);
					UChroma[(y*newWidth + x)] = (inputYUVFrames.get(i).get(1)[(y - 1)*newWidth + (x - 1)]);

					VChroma[((y - 1)*newWidth + (x - 1))] = (inputYUVFrames.get(i).get(2)[(y - 1)*newWidth + (x - 1)]);
					VChroma[((y - 1)*newWidth + x)] = (inputYUVFrames.get(i).get(2)[(y - 1)*newWidth + (x - 1)]);
					VChroma[(y*newWidth + (x - 1))] = (inputYUVFrames.get(i).get(2)[(y - 1)*newWidth + (x - 1)]);
					VChroma[(y*newWidth + x)] = (inputYUVFrames.get(i).get(2)[(y - 1)*newWidth + (x - 1)]);    	
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
	public ArrayList<ArrayList<int[][]>> blocker(ArrayList<int[]> frame, int size)
	{
		int newWidth = ((int) Math.floor(width/size)) * size;
		int newHeight = ((int) Math.floor(height/size)) * size;
		int xcount = 0;
		int ycount = 0;
		int blockNum = (newWidth * newHeight) / (size * size);
		ArrayList<ArrayList<int[][]>> res = new ArrayList<ArrayList<int[][]>>();
		ArrayList<int[][]> resY = new ArrayList<int[][]>();
		ArrayList<int[][]> resU = new ArrayList<int[][]>();
		ArrayList<int[][]> resV = new ArrayList<int[][]>();
		int[] frameY = frame.get(0); // get all corresponding YUV values of current frame
		int[] frameU = frame.get(1);
		int[] frameV = frame.get(2);

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

					blockY[e][f] = frameY[(ycount + e) * width + ((x % (newWidth/size)) * size) + f];
					blockU[e][f] = frameU[(ycount + e) * width + ((x % (newWidth/size)) * size) + f];
					blockV[e][f] = frameV[(ycount + e) * width + ((x % (newWidth/size)) * size) + f];
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


	public ArrayList<int[]> unblocker(ArrayList<ArrayList<int[][]>> frame) {
    	ArrayList<int[]> res = new ArrayList<int[]>();    	
    	ArrayList<int[][]> Yblocks = frame.get(0);
    	ArrayList<int[][]> Ublocks = frame.get(1);
    	ArrayList<int[][]> Vblocks = frame.get(2);

    	int blockDimension = Yblocks.get(0).length;
    	int blockSize = blockDimension * blockDimension;
    	int newWidth = ((int) Math.floor(width/blockDimension)) * blockDimension;
    	int newHeight = ((int) Math.floor(height/blockDimension)) * blockDimension;
    	int xcount = 0;
    	int count = 0;

    	int[] unblockedY = new int[blockSize];
    	int[] unblockedU = new int[blockSize];
    	int[] unblockedV = new int[blockSize];

    	int[] resY = new int[newWidth * newHeight];
    	int[] resU = new int[newWidth * newHeight];
    	int[] resV = new int[newWidth * newHeight];

//    	System.out.println("		blockNum: " + blockNum + "		blockSize: " + blockSize + "		blockDimension: " + blockDimension);
    	
    	for (int x = 0; x < Yblocks.size(); x++) { // iterate through each block

    		unblockedY = flatten2D(Yblocks.get(x));
    		unblockedU = flatten2D(Ublocks.get(x));
    		unblockedV = flatten2D(Vblocks.get(x));

			for(int y = 0; y < blockSize; y++)
			{
				int remainder = y % blockDimension;

				if (x < newWidth/blockDimension)
				{
					xcount = (x % (newWidth/blockDimension)) * blockDimension;
				}
				else if (x == newWidth/blockDimension)
				{
					xcount = x * blockSize;
				}
				else
				{
					int rowCount = x/(newWidth/blockDimension);
					xcount = rowCount * newWidth * blockDimension + (x % (newWidth/blockDimension)) * blockDimension;
				}

				if (remainder == 0 && y != 0)
				{
					count += newWidth;
				}
				
				int index = count + remainder + xcount;
				if (index > newWidth * newHeight) {
					break;
				}
//				System.out.println("index: " + index + "	count: " + count  + "	remainder: "  + remainder + "	xcount: " + xcount) ;
				resY[index] = unblockedY[y];
				resU[index] = unblockedU[y];
				resV[index] = unblockedV[y];
			}
			count = 0;
//			System.out.println("newWidth = " + newWidth + "x= " + x + "	Yblocks.size(): " + Yblocks.size());
    	}

    	res.add(resY);
    	res.add(resU);
    	res.add(resV);
    	return res;
    }

    // helper for unblocker
    static public int[] flatten2D(int[][] array) {
    	int[] res = new int[array.length * array[0].length];

        for (int y = 0; y < array.length; ++y) {
	        for (int x = 0; x < array[y].length; ++x) {
	        	res[(y * array[0].length + x)] = array[y][x];
	        }
        }
        return res;
    }

	// performs intra prediction on 4x4 block (5x5 block used to get neighbouring info
	public static ArrayList<int[][]> intra(int f[][]) {
		ArrayList<int[][]> result = new ArrayList<int[][]>();
		int[][] current = new int[4][4];

		// vertical mode, set all predicted pixels in column A to be pixel A, B as B, & etc.
		for (int y = 0; y < 4; y++) {
			current[0][y] = f[0][0];  // f[1][0] == pixel A 	[M][A][B][C][D]
			current[1][y] = f[1][0];				 	   	 // [I] |  |  |  | 
			current[2][y] = f[2][0];		   			  	 // [J] |  |  |  | 
			current[3][y] = f[3][0];					 	 // [K] |  |  |  | 
		}												  	 // [L] V  V  V  V 
		result.add(current);

		// horizontal mode, set all predicted in row I as pixel I, J as J, etc.
		for (int x = 0; x < 4; x++) {
			current[x][0] = f[0][0];  // f[0][1] == pixel I 	[M][A][B][C][D]
			current[x][1] = f[0][1];				 	   	 // [I] --------->
			current[x][2] = f[0][2];				 	   	 // [J] --------->
			current[x][3] = f[0][3];				 	   	 // [K] --------->
		}											 	 	 // [L] --------->
		result.add(current);

		// average mode, set all predicted in 4x4 block as average of 8 neighbours (A-D, I-L)
		int average = (f[0][0] + f[1][0] + f[2][0] + f[3][0] + f[0][0] + f[0][1] + f[0][2] + f[0][3]) / 8;

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


	// logarithmic search for motion vector, returns arraylist with integer of final motion vector
	public static ArrayList<Integer> motionLogSearch(ArrayList<ArrayList<int[][]>> currentFrame, ArrayList<ArrayList<int[][]>> referenceFrame){
		ArrayList<int[][]> Yblocks = currentFrame.get(0);
		int numBlocks = Yblocks.size(); // get number of Y blocks
		int rowSize = (int) Math.sqrt(numBlocks);
		int p = (int) Math.ceil(Math.sqrt(numBlocks)/2);		
		int offset = (int) Math.ceil(p/2);
		ArrayList<int[][]> nineBlocks = new ArrayList<int[][]>();
		ArrayList<int[][]> refBlocks = new ArrayList<int[][]>();
		ArrayList<int[][]> refY = referenceFrame.get(0);
		ArrayList<Integer> result = new ArrayList<Integer>(); 
		boolean last = false;
		
		int centerCoord = (int) numBlocks/2 - 1; //find middle point
		//int[][] center = Yblocks.get(centerCoord);
		double minMAD = 666;
		int minIndex = 0;
		int lastPosition = centerCoord;
			int tl = (int)centerCoord/2 - offset;
			int tm = (int)centerCoord/2;
			int tr = (int)centerCoord/2 + offset;
			int l = centerCoord - offset;
			int m = centerCoord;
			int r = centerCoord + offset;
			int bl = (int)centerCoord/2 + centerCoord - offset;
			int bm = (int)centerCoord/2 + centerCoord;
			int br = (int)centerCoord/2 + centerCoord + offset;
		
		while (last != true) {
			tl = centerCoord - offset * rowSize - offset;
			tm = centerCoord - offset * rowSize;
			tr = centerCoord - offset * rowSize + offset;
			l  = centerCoord - offset;
			m  = centerCoord;
			r  = centerCoord + offset;
			bl = centerCoord + offset * rowSize - offset;
			bm = centerCoord + offset * rowSize;
			br = centerCoord + offset * rowSize + offset;
			
			int[] positions = new int [] {tl, tm, tr, l, m, r, bl, bm, br};
			System.out.println("offset: tl, tm, tr, l, m, r, bl, bm, br: " 
								+ offset + " " + tl + " " + tm + " " + tr + " " + l + " " + m + " " + r + " " + bl + " " + bm + " " + br);
			nineBlocks.add(0, Yblocks.get(tl));
			nineBlocks.add(1, Yblocks.get(tm));							// 1/4 spot
			nineBlocks.add(2, Yblocks.get(tr));
			nineBlocks.add(3, Yblocks.get(l));
			nineBlocks.add(4, Yblocks.get(m));													// 1/2 spot
			nineBlocks.add(5, Yblocks.get(r));
			nineBlocks.add(6, Yblocks.get(bl));
			nineBlocks.add(7, Yblocks.get(bm));			// 3/4 spot
			nineBlocks.add(8, Yblocks.get(br));	
			
			refBlocks.add(0, refY.get(tl));
			refBlocks.add(1, refY.get(tm));
			refBlocks.add(2, refY.get(tr));	
			refBlocks.add(3, refY.get(l));	
			refBlocks.add(4, refY.get(m));
			refBlocks.add(5, refY.get(r));	
			refBlocks.add(6, refY.get(bl));	
			refBlocks.add(7, refY.get(bm));	
			refBlocks.add(8, refY.get(br));	
			
			for (int x = 0; x < 9; x++) {
				double temp = meanAD(nineBlocks.get(x), refBlocks.get(x));
				if (temp < minMAD) {
					minMAD = temp;
					minIndex = x;
				}
			}
			
			if (offset == 1) {
				last = true;
				lastPosition = positions[minIndex];
			}
			System.out.println("centerCoord: " + centerCoord);
			centerCoord = positions[minIndex];
			offset = (int) Math.ceil(offset/2);
			
		}
		
		result = motionVector((int) numBlocks/2, lastPosition, rowSize);
		System.out.println("motionvector result: " + result.get(0) + ", " + result.get(1));
		
		return result;
	}
	
	public static double meanAD(int[][] currentFrame, int[][] referenceFrame) {
		double MAD = 0;
		int difference = 0;
		int blockSize = currentFrame.length;
		int blockDimension = (int) Math.sqrt(blockSize);
		
		for (int y = 0; y < blockDimension; y++) {
			for (int x = 0; x < blockDimension; x++) {
				difference += currentFrame[x][y] - referenceFrame[x][y];
			}
		}
		
		MAD = Math.abs(difference) / blockSize;
		
		return MAD;
	}
	
	public static ArrayList<Integer> motionVector(int center, int last, int rowSize) {
		int centerX = center % rowSize;
		int centerY = (int) Math.floor(center / rowSize);
		int lastX = last % rowSize;
		int lastY = (int) Math.floor(last / rowSize);
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(Math.abs(centerX - lastX));
		result.add(Math.abs(centerY - lastY));

		return result;
	}
	
	public static int[][] integerTransform(int [][] f, int QP) {
		int [][] H = {{1,1,1,1},{2,1,-1,-2},{1,-1,-1,1},{1,-2,2,-1}};
		int [][] Ht = {{1,2,1,1},{1,1,-1,-2},{1,-1,-1,2},{1,-2,1,-1}};
		double [][] M = {{13107,5243,8066},{11916,4660,7490},{10082,4194,6554},{9362,3647,5825},{8192,3355,5243},{7282,2893,4559}};
		double [][] intRes = new double[4][4];
		double [][] intRes2 = new double[4][4];
		int [][] res = new int[4][4];


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

	public static int[][] inverseIntegerTransform(int [][] F, int QP) {
		double [][] HInv = {{1,1,1,1/2},{1,1/2,-1,-1},{1,-1/2,-1,1},{1,-1,1,-1/2}};
		double [][] HtInv = {{1,1,1,1},{1,1/2,-1/2,-1},{1,-1,-1,1},{1/2,-1,1,-1/2}};
		double [][] V = {{10,16,13},{11,18,14},{13,20,16},{14,23,18},{16,25,20},{18,29,23}};
		double [][] intRes = new double[4][4];
		double [][] intRes2 = new double[4][4];
		double [][] intRes3 = new double[4][4];
		int [][] res = new int[4][4];
		//int QP = 0;

		//int [][] F = {{507,-12,-2,2},{0,-7,-14,5},{2,0,-8,-11},{-1,8,4,3}};

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

	public int[] addRGBChroma(ArrayList<int[]> frame, int mode) {
		
		//mode 0 = res
		//mode 1 = noLumares
		
		int[] res = new int[width*height*3];
    	int[] noLumaRes = new int[width*height*3];
		
		int[] frameY = frame.get(0);
		int[] frameU = frame.get(1);
		int[] frameV = frame.get(2);
		
        for (int x = 0; x < width * height; x++)
        {
        	int resR = (int)( frameY[x] 					  + 1.13983 * frameV[x] );
        	int resG = (int)( frameY[x] - 0.39465 * frameU[x] - 0.58060 * frameV[x] );
        	int resB = (int)( frameY[x] + 2.03211 * frameU[x]);
        	
        	int nolumaR = (int)(  1.13983 *                       frameV[x] );
        	int nolumaG = (int)(- 0.39465 * frameU[x] - 0.58060 * frameV[x] );
        	int nolumaB = (int)(+ 2.03211 * frameU[x]);
        	
        	// ensure RGB values are within range
        	if (resR > 255) {resR = 255;}
        	if (resR < 0) {resR = 0;}
        	if (resG> 255) {resG = 255;}
        	if (resG < 0) {resG = 0;}
        	if (resB > 255) {resB = 255;}
        	if (resB < 0) {resB = 0;}
        	
        	if (nolumaR > 255) {nolumaR = 255;}
        	if (nolumaR < 0) {nolumaR = 0;}
        	if (nolumaG> 255) {nolumaG = 255;}
        	if (nolumaG < 0) {nolumaG = 0;}
        	if (nolumaB > 255) {nolumaB = 255;}
        	if (nolumaB < 0) {nolumaB = 0;}

        	// set RGB values in respective locations
        	res[x * 3] = resR;
        	res[x * 3 + 1] = resG;
        	res[x * 3 + 2] = resB;
        	
        	noLumaRes[x * 3] = nolumaR;
        	noLumaRes[x * 3 + 1] = nolumaG;
        	noLumaRes[x * 3 + 2] = nolumaB;
        }
        
        if(mode == 0) {
            return res;
        }
        else {
            return noLumaRes;
        }

	}
	
	public void test() {

		JPanel OutputImg, OutputYUVOriginal, OutputYUVChroma, OutputPrediction, OutputResidual, OutputTransform, OutputInverseTransform;
		
		IMGPanel 	m_panelImgOutputY, m_panelImgOutputU, m_panelImgOutputV, m_panelImgOutputYUV,
					m_panelImgOutputYChroma, m_panelImgOutputUChroma, m_panelImgOutputVChroma, m_panelImgOutputYUVChroma,
					m_panelImgOutputYPrediction, m_panelImgOutputUPrediction, m_panelImgOutputVPrediction, m_panelImgOutputYUVPrediction,
					m_panelImgOutputYResidual, m_panelImgOutputUResidual, m_panelImgOutputVResidual, m_panelImgOutputYUVResidual,
					m_panelImgOutputYTransform, m_panelImgOutputUTransform, m_panelImgOutputVTransform, m_panelImgOutputYUVTransform,
					m_panelImgOutputYInverse, m_panelImgOutputUInverse, m_panelImgOutputVInverse, m_panelImgOutputYUVInverse
					;
					//m_panelImgOutputPredictedIFrame, m_panelImgOutputIntegerTransformIFrame;
		
		BufferedImage 	m_imgOutputY, m_imgOutputU, m_imgOutputV, m_imgOutputYUV, 
						m_imgOutputYChroma, m_imgOutputUChroma, m_imgOutputVChroma, m_imgOutputYUVChroma,
						m_imgOutputYPrediction, m_imgOutputUPrediction, m_imgOutputVPrediction, m_imgOutputYUVPrediction,
						m_imgOutputYResidual, m_imgOutputUResidual, m_imgOutputVResidual, m_imgOutputYUVResidual,
						m_imgOutputYTransform, m_imgOutputUTransform, m_imgOutputVTransform,
						m_imgOutputYInverse, m_imgOutputUInverse, m_imgOutputVInverse, m_imgOutputYUVInverse
						; 
						//m_imgOutputPredictedIFrame, m_imgOutputIntegerTransformIFrame;
		
		totalGUI.removeAll(); 
		totalGUI.add(buttonGUI);
		
		//Set Panels
		OutputImg = new JPanel();
		OutputImg.setLayout(null);
		OutputImg.setLocation(0, 70);
		OutputImg.setSize(1910, 850);
		totalGUI.add(OutputImg);

		OutputYUVOriginal = new JPanel();
		OutputYUVOriginal.setLocation(0, 930);
		OutputYUVOriginal.setSize(310, 80);
		totalGUI.add(OutputYUVOriginal);

		OutputYUVChroma = new JPanel();
		OutputYUVChroma.setLocation(310, 930);
		OutputYUVChroma.setSize(310, 80);
		totalGUI.add(OutputYUVChroma);
		
		OutputPrediction = new JPanel();
		OutputPrediction.setLocation(620, 930);
		OutputPrediction.setSize(310, 80);
		totalGUI.add(OutputPrediction);
		
		OutputResidual = new JPanel();
		OutputResidual.setLocation(930, 930);
		OutputResidual.setSize(310, 80);
		totalGUI.add(OutputResidual);
		
		OutputTransform = new JPanel();
		OutputTransform.setLocation(1240, 930);
		OutputTransform.setSize(310, 80);
		totalGUI.add(OutputTransform);
		
		OutputInverseTransform = new JPanel();
		OutputInverseTransform.setLocation(1550, 930);
		OutputInverseTransform.setSize(310, 80);
		totalGUI.add(OutputInverseTransform);
		
		//Column 1
		m_panelImgOutputY = new IMGPanel();
		m_panelImgOutputY.setLocation(10, 10);
		m_panelImgOutputY.setSize(300, 200);
		OutputImg.add(m_panelImgOutputY);

		m_panelImgOutputU = new IMGPanel();
		m_panelImgOutputU.setLocation(10, 220);
		m_panelImgOutputU.setSize(300, 200);
		OutputImg.add(m_panelImgOutputU);

		m_panelImgOutputV = new IMGPanel();
		m_panelImgOutputV.setLocation(10, 430);
		m_panelImgOutputV.setSize(300, 200);
		OutputImg.add(m_panelImgOutputV);
		
		m_panelImgOutputYUV = new IMGPanel();
		m_panelImgOutputYUV.setLocation(10, 640);
		m_panelImgOutputYUV.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUV);
		
		JLabel YUVOriginal = new JLabel("Original YUV");
		OutputYUVOriginal.add(YUVOriginal);

		//Column 2
		m_panelImgOutputYChroma = new IMGPanel();
		m_panelImgOutputYChroma.setLocation(320, 10);
		m_panelImgOutputYChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYChroma);

		m_panelImgOutputUChroma = new IMGPanel();
		m_panelImgOutputUChroma.setLocation(320, 220);
		m_panelImgOutputUChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUChroma);

		m_panelImgOutputVChroma = new IMGPanel();
		m_panelImgOutputVChroma.setLocation(320, 430);
		m_panelImgOutputVChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVChroma);
		
		m_panelImgOutputYUVChroma = new IMGPanel();
		m_panelImgOutputYUVChroma.setLocation(320, 640);
		m_panelImgOutputYUVChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVChroma);
		
		JLabel YUVChroma = new JLabel("YUV 4:2:0 Chroma");
		OutputYUVChroma.add(YUVChroma);
		
		
		//Column 3
		m_panelImgOutputYPrediction = new IMGPanel();
		m_panelImgOutputYPrediction.setLocation(630, 10);
		m_panelImgOutputYPrediction.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYPrediction);

		m_panelImgOutputUPrediction = new IMGPanel();
		m_panelImgOutputUPrediction.setLocation(630, 220);
		m_panelImgOutputUPrediction.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUPrediction);

		m_panelImgOutputVPrediction = new IMGPanel();
		m_panelImgOutputVPrediction.setLocation(630, 430);
		m_panelImgOutputVPrediction.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVPrediction);
		
		m_panelImgOutputYUVPrediction = new IMGPanel();
		m_panelImgOutputYUVPrediction.setLocation(630, 640);
		m_panelImgOutputYUVPrediction.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVPrediction);
		
		JLabel Prediction = new JLabel("Prediction Image");
		OutputPrediction.add(Prediction);
		
		//Column 4
		m_panelImgOutputYResidual = new IMGPanel();
		m_panelImgOutputYResidual.setLocation(940, 10);
		m_panelImgOutputYResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYResidual);
		
		m_panelImgOutputUResidual = new IMGPanel();
		m_panelImgOutputUResidual.setLocation(940, 220);
		m_panelImgOutputUResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUResidual);
		
		m_panelImgOutputVResidual = new IMGPanel();
		m_panelImgOutputVResidual.setLocation(940, 430);
		m_panelImgOutputVResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVResidual);
		
		m_panelImgOutputYUVResidual = new IMGPanel();
		m_panelImgOutputYUVResidual.setLocation(940, 640);
		m_panelImgOutputYUVResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVResidual);
		
		JLabel Residual = new JLabel("Residual Image");
		OutputResidual.add(Residual);
		
		//Column 5
		m_panelImgOutputYTransform = new IMGPanel();
		m_panelImgOutputYTransform.setLocation(1250, 10);
		m_panelImgOutputYTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYTransform);

		m_panelImgOutputUTransform = new IMGPanel();
		m_panelImgOutputUTransform.setLocation(1250, 220);
		m_panelImgOutputUTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUTransform);
		
		m_panelImgOutputVTransform = new IMGPanel();
		m_panelImgOutputVTransform.setLocation(1250, 430);
		m_panelImgOutputVTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVTransform);
		
		m_panelImgOutputYUVTransform = new IMGPanel();
		m_panelImgOutputYUVTransform.setLocation(1250, 640);
		m_panelImgOutputYUVTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVTransform);
		
		JLabel IntegerTransform = new JLabel("Integer Transform Image");
		OutputTransform.add(IntegerTransform);
		
		//Column 6
		m_panelImgOutputYInverse = new IMGPanel();
		m_panelImgOutputYInverse.setLocation(1560, 10);
		m_panelImgOutputYInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYInverse);
		
		m_panelImgOutputUInverse = new IMGPanel();
		m_panelImgOutputUInverse.setLocation(1560, 220);
		m_panelImgOutputUInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUInverse);
		
		m_panelImgOutputVInverse = new IMGPanel();
		m_panelImgOutputVInverse.setLocation(1560, 430);
		m_panelImgOutputVInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVInverse);
		
		m_panelImgOutputYUVInverse = new IMGPanel();
		m_panelImgOutputYUVInverse.setLocation(1560, 640);
		m_panelImgOutputYUVInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVInverse);
		
		JLabel InverseTransform = new JLabel("Inverse Integer Transform Image");
		OutputInverseTransform.add(InverseTransform);
		
		//OutputImg.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.RED));
		//OutputYUVOriginal.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.RED));
		//OutputYUVChroma.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.RED));
		///////

		int newWidth = ((int) Math.floor(width/4)) * 4;
		int newHeight = ((int) Math.floor(height/4)) * 4;
		
		//Column 1
		m_imgOutputY = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterY = (WritableRaster) m_imgOutputY.getData();
		rasterY.setPixels(0, 0, width, height, YUVIFrames.get(FRAME_NUM).get(0));
		m_imgOutputY.setData(rasterY);
		m_panelImgOutputY.setBufferedImage(m_imgOutputY);		

		m_imgOutputU = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterU = (WritableRaster) m_imgOutputU.getData();
		rasterU.setPixels(0, 0, width, height, YUVIFrames.get(FRAME_NUM).get(1));
		m_imgOutputU.setData(rasterU);
		m_panelImgOutputU.setBufferedImage(m_imgOutputU);	

		m_imgOutputV = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterV = (WritableRaster) m_imgOutputV.getData();
		rasterV.setPixels(0, 0, width, height, YUVIFrames.get(FRAME_NUM).get(2));
		m_imgOutputV.setData(rasterV);
		m_panelImgOutputV.setBufferedImage(m_imgOutputV);	
		
		int[] YUVRes = addRGBChroma(YUVIFrames.get(FRAME_NUM), 0);
		
		m_imgOutputYUV = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUV = (WritableRaster) m_imgOutputYUV.getData();
		rasterYUV.setPixels(0, 0, width, height, YUVRes);
		m_imgOutputYUV.setData(rasterYUV);
		m_panelImgOutputYUV.setBufferedImage(m_imgOutputYUV);	
		
		//Column 2
		m_imgOutputYChroma = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYChroma = (WritableRaster) m_imgOutputYChroma.getData();
		rasterYChroma.setPixels(0, 0, width, height, ChromaIFrames.get(FRAME_NUM).get(0));
		m_imgOutputYChroma.setData(rasterYChroma);
		m_panelImgOutputYChroma.setBufferedImage(m_imgOutputYChroma);		

		m_imgOutputUChroma = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUChroma = (WritableRaster) m_imgOutputUChroma.getData();
		rasterUChroma.setPixels(0, 0, width, height, ChromaIFrames.get(FRAME_NUM).get(1));
		m_imgOutputUChroma.setData(rasterUChroma);
		m_panelImgOutputUChroma.setBufferedImage(m_imgOutputUChroma);	

		m_imgOutputVChroma = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVChroma = (WritableRaster) m_imgOutputVChroma.getData();
		rasterVChroma.setPixels(0, 0, width, height, ChromaIFrames.get(FRAME_NUM).get(2));
		m_imgOutputVChroma.setData(rasterVChroma);
		m_panelImgOutputVChroma.setBufferedImage(m_imgOutputVChroma);	
		
		int[] YUVChromaRGB = addRGBChroma(ChromaIFrames.get(FRAME_NUM), 0);
		
		m_imgOutputYUVChroma = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUVChroma = (WritableRaster) m_imgOutputYUVChroma.getData();
		rasterYUVChroma.setPixels(0, 0, width, height, YUVChromaRGB);
		m_imgOutputYUVChroma.setData(rasterYUVChroma);
		m_panelImgOutputYUVChroma.setBufferedImage(m_imgOutputYUVChroma);	
		
		//Column 3
		ArrayList<int[]> YUVPredictionImage = unblocker(PredictedIFrames.get(FRAME_NUM));
		
		m_imgOutputYPrediction = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYPrediction = (WritableRaster) m_imgOutputYPrediction.getData();
		rasterYPrediction.setPixels(0, 0, newWidth, newHeight, YUVPredictionImage.get(0));
		m_imgOutputYPrediction.setData(rasterYPrediction);
		m_panelImgOutputYPrediction.setBufferedImage(m_imgOutputYPrediction);	
		
		m_imgOutputUPrediction = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUPrediction = (WritableRaster) m_imgOutputUPrediction.getData();
		rasterUPrediction.setPixels(0, 0, width, height, YUVPredictionImage.get(1));
		m_imgOutputUPrediction.setData(rasterUPrediction);
		m_panelImgOutputUPrediction.setBufferedImage(m_imgOutputUPrediction);
		
		m_imgOutputVPrediction = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVPrediction = (WritableRaster) m_imgOutputVPrediction.getData();
		rasterVPrediction.setPixels(0, 0, width, height, YUVPredictionImage.get(2));
		m_imgOutputVPrediction.setData(rasterVPrediction);
		m_panelImgOutputVPrediction.setBufferedImage(m_imgOutputVPrediction);
		
		int[] YUVPredictionImageRGB = addRGBChroma(YUVPredictionImage, 0);
		
		m_imgOutputYUVPrediction = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUVPrediction = (WritableRaster) m_imgOutputYUVPrediction.getData();
		rasterYUVPrediction.setPixels(0, 0, width, height, YUVPredictionImageRGB);
		m_imgOutputYUVPrediction.setData(rasterYUVPrediction);
		m_panelImgOutputYUVPrediction.setBufferedImage(m_imgOutputYUVPrediction);	
		
		//Column 4
		ArrayList<int[]> YUVResidualImage = unblocker(ResidualIFrames.get(FRAME_NUM));
		
		m_imgOutputYResidual = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYResidual = (WritableRaster) m_imgOutputYResidual.getData();
		rasterYResidual.setPixels(0, 0, newWidth, newHeight, YUVResidualImage.get(0));
		m_imgOutputYResidual.setData(rasterYResidual);
		m_panelImgOutputYResidual.setBufferedImage(m_imgOutputYResidual);	
		
		m_imgOutputUResidual = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUResidual = (WritableRaster) m_imgOutputUResidual.getData();
		rasterUResidual.setPixels(0, 0, newWidth, newHeight, YUVResidualImage.get(1));
		m_imgOutputUResidual.setData(rasterUResidual);
		m_panelImgOutputUResidual.setBufferedImage(m_imgOutputUResidual);	
		
		m_imgOutputVResidual = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVResidual = (WritableRaster) m_imgOutputVResidual.getData();
		rasterVResidual.setPixels(0, 0, newWidth, newHeight, YUVResidualImage.get(2));
		m_imgOutputVResidual.setData(rasterVResidual);
		m_panelImgOutputVResidual.setBufferedImage(m_imgOutputVResidual);	
		
		int[] YUVResidualImageRGB = addRGBChroma(YUVResidualImage, 0);
		
		m_imgOutputYUVResidual = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUVResidual = (WritableRaster) m_imgOutputYUVResidual.getData();
		rasterYUVResidual.setPixels(0, 0, width, height, YUVResidualImageRGB);
		m_imgOutputYUVResidual.setData(rasterYUVResidual);
		m_panelImgOutputYUVResidual.setBufferedImage(m_imgOutputYUVResidual);
		
		//Column 5
		ArrayList<int[]> YUVTransformImage = unblocker(IntegerTransformIFrames.get(FRAME_NUM));

		m_imgOutputYTransform = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYTransform = (WritableRaster) m_imgOutputYTransform.getData();
		rasterYTransform.setPixels(0, 0, width, height, YUVTransformImage.get(0));
		m_imgOutputYTransform.setData(rasterYTransform);
		m_panelImgOutputYTransform.setBufferedImage(m_imgOutputYTransform);	
		
		m_imgOutputUTransform = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUTransform = (WritableRaster) m_imgOutputUTransform.getData();
		rasterUTransform.setPixels(0, 0, width, height, YUVTransformImage.get(1));
		m_imgOutputUTransform.setData(rasterUTransform);
		m_panelImgOutputUTransform.setBufferedImage(m_imgOutputUTransform);	
		
		m_imgOutputVTransform = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVTransform = (WritableRaster) m_imgOutputVTransform.getData();
		rasterVTransform.setPixels(0, 0, width, height, YUVTransformImage.get(2));
		m_imgOutputVTransform.setData(rasterVTransform);
		m_panelImgOutputVTransform.setBufferedImage(m_imgOutputVTransform);	
		
//		int[] YUVTransformImageRGB = addRGBChroma(YUVTransformImage, 0);
//		
//		m_imgOutputYUVTransform = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		WritableRaster rasterYUVTransform = (WritableRaster) m_imgOutputYUVTransform.getData();
//		rasterYUVTransform.setPixels(0, 0, width, height, YUVTransformImageRGB);
//		m_imgOutputYUVTransform.setData(rasterYUVTransform);
//		m_panelImgOutputYUVTransform.setBufferedImage(m_imgOutputYUVTransform);	
		
		//Column 6
		ArrayList<int[]> YUVInverseImage = unblocker(IntegerInverseTransformIFrames.get(FRAME_NUM));

		m_imgOutputYInverse = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYInverse = (WritableRaster) m_imgOutputYInverse.getData();
		rasterYInverse.setPixels(0, 0, width, height, YUVInverseImage.get(0));
		m_imgOutputYInverse.setData(rasterYInverse);
		m_panelImgOutputYInverse.setBufferedImage(m_imgOutputYInverse);	
		
		m_imgOutputUInverse = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUInverse = (WritableRaster) m_imgOutputUInverse.getData();
		rasterUInverse.setPixels(0, 0, width, height, YUVInverseImage.get(1));
		m_imgOutputUInverse.setData(rasterUInverse);
		m_panelImgOutputUInverse.setBufferedImage(m_imgOutputUInverse);	
		
		m_imgOutputVInverse = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVInverse = (WritableRaster) m_imgOutputVInverse.getData();
		rasterVInverse.setPixels(0, 0, width, height, YUVInverseImage.get(2));
		m_imgOutputVInverse.setData(rasterVInverse);
		m_panelImgOutputVInverse.setBufferedImage(m_imgOutputVInverse);	
		
		int[] YUVInverseImageRGB = addRGBChroma(YUVInverseImage, 0);
		
		m_imgOutputYUVInverse = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUVInverse = (WritableRaster) m_imgOutputYUVInverse.getData();
		rasterYUVInverse.setPixels(0, 0, newWidth, newHeight, YUVInverseImageRGB);
		m_imgOutputYUVInverse.setData(rasterYUVInverse);
		m_panelImgOutputYUVInverse.setBufferedImage(m_imgOutputYUVInverse);	
	}

	public void testP() {

		JPanel OutputImg, OutputYUVOriginal, OutputYUVChroma, OutputResidual, OutputTransform, OutputInverseTransform;
		
		IMGPanel 	m_panelImgOutputY, m_panelImgOutputU, m_panelImgOutputV, m_panelImgOutputYUV,
					m_panelImgOutputYChroma, m_panelImgOutputUChroma, m_panelImgOutputVChroma, m_panelImgOutputYUVChroma,
					m_panelImgOutputYResidual, m_panelImgOutputUResidual, m_panelImgOutputVResidual, m_panelImgOutputYUVResidual,
					m_panelImgOutputYTransform, m_panelImgOutputUTransform, m_panelImgOutputVTransform, m_panelImgOutputYUVTransform,
					m_panelImgOutputYInverse, m_panelImgOutputUInverse, m_panelImgOutputVInverse, m_panelImgOutputYUVInverse
					;
					//m_panelImgOutputPredictedIFrame, m_panelImgOutputIntegerTransformIFrame;
		
		BufferedImage 	m_imgOutputY, m_imgOutputU, m_imgOutputV, m_imgOutputYUV, 
						m_imgOutputYChroma, m_imgOutputUChroma, m_imgOutputVChroma, m_imgOutputYUVChroma,
						m_imgOutputYResidual, m_imgOutputUResidual, m_imgOutputVResidual, m_imgOutputYUVResidual,
						m_imgOutputYTransform, m_imgOutputUTransform, m_imgOutputVTransform,
						m_imgOutputYInverse, m_imgOutputUInverse, m_imgOutputVInverse, m_imgOutputYUVInverse
						; 
						//m_imgOutputPredictedIFrame, m_imgOutputIntegerTransformIFrame;
		
		totalGUI.removeAll(); 
		totalGUI.revalidate();
		totalGUI.repaint();
		totalGUI.add(buttonGUI);
		
		//Set Panels
		OutputImg = new JPanel();
		OutputImg.setLayout(null);
		OutputImg.setLocation(0, 70);
		OutputImg.setSize(1910, 850);
		totalGUI.add(OutputImg);

		OutputYUVOriginal = new JPanel();
		OutputYUVOriginal.setLocation(0, 930);
		OutputYUVOriginal.setSize(310, 80);
		totalGUI.add(OutputYUVOriginal);

		OutputYUVChroma = new JPanel();
		OutputYUVChroma.setLocation(310, 930);
		OutputYUVChroma.setSize(310, 80);
		totalGUI.add(OutputYUVChroma);
		
		OutputResidual = new JPanel();
		OutputResidual.setLocation(620, 930);
		OutputResidual.setSize(310, 80);
		totalGUI.add(OutputResidual);
		
		OutputTransform = new JPanel();
		OutputTransform.setLocation(930, 930);
		OutputTransform.setSize(310, 80);
		totalGUI.add(OutputTransform);
		
		OutputInverseTransform = new JPanel();
		OutputInverseTransform.setLocation(1240, 930);
		OutputInverseTransform.setSize(310, 80);
		totalGUI.add(OutputInverseTransform);
		
		//Column 1
		m_panelImgOutputY = new IMGPanel();
		m_panelImgOutputY.setLocation(10, 10);
		m_panelImgOutputY.setSize(300, 200);
		OutputImg.add(m_panelImgOutputY);

		m_panelImgOutputU = new IMGPanel();
		m_panelImgOutputU.setLocation(10, 220);
		m_panelImgOutputU.setSize(300, 200);
		OutputImg.add(m_panelImgOutputU);

		m_panelImgOutputV = new IMGPanel();
		m_panelImgOutputV.setLocation(10, 430);
		m_panelImgOutputV.setSize(300, 200);
		OutputImg.add(m_panelImgOutputV);
		
		m_panelImgOutputYUV = new IMGPanel();
		m_panelImgOutputYUV.setLocation(10, 640);
		m_panelImgOutputYUV.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUV);
		
		JLabel YUVOriginal = new JLabel("Original YUV");
		OutputYUVOriginal.add(YUVOriginal);

		//Column 2
		m_panelImgOutputYChroma = new IMGPanel();
		m_panelImgOutputYChroma.setLocation(320, 10);
		m_panelImgOutputYChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYChroma);

		m_panelImgOutputUChroma = new IMGPanel();
		m_panelImgOutputUChroma.setLocation(320, 220);
		m_panelImgOutputUChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUChroma);

		m_panelImgOutputVChroma = new IMGPanel();
		m_panelImgOutputVChroma.setLocation(320, 430);
		m_panelImgOutputVChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVChroma);
		
		m_panelImgOutputYUVChroma = new IMGPanel();
		m_panelImgOutputYUVChroma.setLocation(320, 640);
		m_panelImgOutputYUVChroma.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVChroma);
		
		JLabel YUVChroma = new JLabel("YUV 4:2:0 Chroma");
		OutputYUVChroma.add(YUVChroma);
		
		
		//Column 3
		m_panelImgOutputYResidual = new IMGPanel();
		m_panelImgOutputYResidual.setLocation(630, 10);
		m_panelImgOutputYResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYResidual);
		
		m_panelImgOutputUResidual = new IMGPanel();
		m_panelImgOutputUResidual.setLocation(630, 220);
		m_panelImgOutputUResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUResidual);
		
		m_panelImgOutputVResidual = new IMGPanel();
		m_panelImgOutputVResidual.setLocation(630, 430);
		m_panelImgOutputVResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVResidual);
		
		m_panelImgOutputYUVResidual = new IMGPanel();
		m_panelImgOutputYUVResidual.setLocation(630, 640);
		m_panelImgOutputYUVResidual.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVResidual);
		
		JLabel Residual = new JLabel("Residual Image");
		OutputResidual.add(Residual);
		
		//Column 5
		m_panelImgOutputYTransform = new IMGPanel();
		m_panelImgOutputYTransform.setLocation(940, 10);
		m_panelImgOutputYTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYTransform);

		m_panelImgOutputUTransform = new IMGPanel();
		m_panelImgOutputUTransform.setLocation(940, 220);
		m_panelImgOutputUTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUTransform);
		
		m_panelImgOutputVTransform = new IMGPanel();
		m_panelImgOutputVTransform.setLocation(940, 430);
		m_panelImgOutputVTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVTransform);
		
		m_panelImgOutputYUVTransform = new IMGPanel();
		m_panelImgOutputYUVTransform.setLocation(940, 640);
		m_panelImgOutputYUVTransform.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVTransform);
		
		JLabel IntegerTransform = new JLabel("Integer Transform Image");
		OutputTransform.add(IntegerTransform);
		
		//Column 6
		m_panelImgOutputYInverse = new IMGPanel();
		m_panelImgOutputYInverse.setLocation(1250, 10);
		m_panelImgOutputYInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYInverse);
		
		m_panelImgOutputUInverse = new IMGPanel();
		m_panelImgOutputUInverse.setLocation(1250, 220);
		m_panelImgOutputUInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputUInverse);
		
		m_panelImgOutputVInverse = new IMGPanel();
		m_panelImgOutputVInverse.setLocation(1250, 430);
		m_panelImgOutputVInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputVInverse);
		
		m_panelImgOutputYUVInverse = new IMGPanel();
		m_panelImgOutputYUVInverse.setLocation(1250, 640);
		m_panelImgOutputYUVInverse.setSize(300, 200);
		OutputImg.add(m_panelImgOutputYUVInverse);
		
		JLabel InverseTransform = new JLabel("Inverse Integer Transform Image");
		OutputInverseTransform.add(InverseTransform);
		
		//OutputImg.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.RED));
		//OutputYUVOriginal.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.RED));
		//OutputYUVChroma.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.RED));
		///////

		int newWidth = ((int) Math.floor(width/4)) * 4;
		int newHeight = ((int) Math.floor(height/4)) * 4;
		
		//Column 1
		m_imgOutputY = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterY = (WritableRaster) m_imgOutputY.getData();
		rasterY.setPixels(0, 0, width, height, YUVPFrames.get(FRAME_NUM).get(0));
		m_imgOutputY.setData(rasterY);
		m_panelImgOutputY.setBufferedImage(m_imgOutputY);		

		m_imgOutputU = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterU = (WritableRaster) m_imgOutputU.getData();
		rasterU.setPixels(0, 0, width, height, YUVPFrames.get(FRAME_NUM).get(1));
		m_imgOutputU.setData(rasterU);
		m_panelImgOutputU.setBufferedImage(m_imgOutputU);	

		m_imgOutputV = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterV = (WritableRaster) m_imgOutputV.getData();
		rasterV.setPixels(0, 0, width, height, YUVPFrames.get(FRAME_NUM).get(2));
		m_imgOutputV.setData(rasterV);
		m_panelImgOutputV.setBufferedImage(m_imgOutputV);	
		
		int[] YUVRes = addRGBChroma(YUVPFrames.get(FRAME_NUM), 0);
		
		m_imgOutputYUV = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUV = (WritableRaster) m_imgOutputYUV.getData();
		rasterYUV.setPixels(0, 0, width, height, YUVRes);
		m_imgOutputYUV.setData(rasterYUV);
		m_panelImgOutputYUV.setBufferedImage(m_imgOutputYUV);	
		
		//Column 2
		m_imgOutputYChroma = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYChroma = (WritableRaster) m_imgOutputYChroma.getData();
		rasterYChroma.setPixels(0, 0, width, height, ChromaPFrames.get(FRAME_NUM).get(0));
		m_imgOutputYChroma.setData(rasterYChroma);
		m_panelImgOutputYChroma.setBufferedImage(m_imgOutputYChroma);		

		m_imgOutputUChroma = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUChroma = (WritableRaster) m_imgOutputUChroma.getData();
		rasterUChroma.setPixels(0, 0, width, height, ChromaPFrames.get(FRAME_NUM).get(1));
		m_imgOutputUChroma.setData(rasterUChroma);
		m_panelImgOutputUChroma.setBufferedImage(m_imgOutputUChroma);	

		m_imgOutputVChroma = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVChroma = (WritableRaster) m_imgOutputVChroma.getData();
		rasterVChroma.setPixels(0, 0, width, height, ChromaPFrames.get(FRAME_NUM).get(2));
		m_imgOutputVChroma.setData(rasterVChroma);
		m_panelImgOutputVChroma.setBufferedImage(m_imgOutputVChroma);	
		
		int[] YUVChromaRGB = addRGBChroma(ChromaPFrames.get(FRAME_NUM), 0);
		
		m_imgOutputYUVChroma = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUVChroma = (WritableRaster) m_imgOutputYUVChroma.getData();
		rasterYUVChroma.setPixels(0, 0, width, height, YUVChromaRGB);
		m_imgOutputYUVChroma.setData(rasterYUVChroma);
		m_panelImgOutputYUVChroma.setBufferedImage(m_imgOutputYUVChroma);	
//		System.out.println("ResidualPFrames.get(FRAME_NUM): " + ResidualPFrames.get(FRAME_NUM));		
		//Column 3
		ArrayList<int[]> YUVResidualImage = unblocker(ResidualPFrames.get(FRAME_NUM));

		
		m_imgOutputYResidual = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYResidual = (WritableRaster) m_imgOutputYResidual.getData();
		rasterYResidual.setPixels(0, 0, newWidth, newHeight, YUVResidualImage.get(0));
		m_imgOutputYResidual.setData(rasterYResidual);
		m_panelImgOutputYResidual.setBufferedImage(m_imgOutputYResidual);	
		
		m_imgOutputUResidual = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUResidual = (WritableRaster) m_imgOutputUResidual.getData();
		rasterUResidual.setPixels(0, 0, newWidth, newHeight, YUVResidualImage.get(1));
		m_imgOutputUResidual.setData(rasterUResidual);
		m_panelImgOutputUResidual.setBufferedImage(m_imgOutputUResidual);	
		
		m_imgOutputVResidual = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVResidual = (WritableRaster) m_imgOutputVResidual.getData();
		rasterVResidual.setPixels(0, 0, newWidth, newHeight, YUVResidualImage.get(2));
		m_imgOutputVResidual.setData(rasterVResidual);
		m_panelImgOutputVResidual.setBufferedImage(m_imgOutputVResidual);	
		
		int[] YUVResidualImageRGB = addRGBChroma(YUVResidualImage, 0);
		
		m_imgOutputYUVResidual = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUVResidual = (WritableRaster) m_imgOutputYUVResidual.getData();
		rasterYUVResidual.setPixels(0, 0, width, height, YUVResidualImageRGB);
		m_imgOutputYUVResidual.setData(rasterYUVResidual);
		m_panelImgOutputYUVResidual.setBufferedImage(m_imgOutputYUVResidual);
		
		//Column 5
		ArrayList<int[]> YUVTransformImage = unblocker(IntegerTransformPFrames.get(FRAME_NUM));

		m_imgOutputYTransform = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYTransform = (WritableRaster) m_imgOutputYTransform.getData();
		rasterYTransform.setPixels(0, 0, width, height, YUVTransformImage.get(0));
		m_imgOutputYTransform.setData(rasterYTransform);
		m_panelImgOutputYTransform.setBufferedImage(m_imgOutputYTransform);	
		
		m_imgOutputUTransform = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUTransform = (WritableRaster) m_imgOutputUTransform.getData();
		rasterUTransform.setPixels(0, 0, width, height, YUVTransformImage.get(1));
		m_imgOutputUTransform.setData(rasterUTransform);
		m_panelImgOutputUTransform.setBufferedImage(m_imgOutputUTransform);	
		
		m_imgOutputVTransform = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVTransform = (WritableRaster) m_imgOutputVTransform.getData();
		rasterVTransform.setPixels(0, 0, width, height, YUVTransformImage.get(2));
		m_imgOutputVTransform.setData(rasterVTransform);
		m_panelImgOutputVTransform.setBufferedImage(m_imgOutputVTransform);	
		
//		int[] YUVTransformImageRGB = addRGBChroma(YUVTransformImage, 0);
//		
//		m_imgOutputYUVTransform = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		WritableRaster rasterYUVTransform = (WritableRaster) m_imgOutputYUVTransform.getData();
//		rasterYUVTransform.setPixels(0, 0, width, height, YUVTransformImageRGB);
//		m_imgOutputYUVTransform.setData(rasterYUVTransform);
//		m_panelImgOutputYUVTransform.setBufferedImage(m_imgOutputYUVTransform);	
		
		//Column 6
		ArrayList<int[]> YUVInverseImage = unblocker(IntegerInverseTransformPFrames.get(FRAME_NUM));

		m_imgOutputYInverse = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterYInverse = (WritableRaster) m_imgOutputYInverse.getData();
		rasterYInverse.setPixels(0, 0, width, height, YUVInverseImage.get(0));
		m_imgOutputYInverse.setData(rasterYInverse);
		m_panelImgOutputYInverse.setBufferedImage(m_imgOutputYInverse);	
		
		m_imgOutputUInverse = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterUInverse = (WritableRaster) m_imgOutputUInverse.getData();
		rasterUInverse.setPixels(0, 0, width, height, YUVInverseImage.get(1));
		m_imgOutputUInverse.setData(rasterUInverse);
		m_panelImgOutputUInverse.setBufferedImage(m_imgOutputUInverse);	
		
		m_imgOutputVInverse = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster rasterVInverse = (WritableRaster) m_imgOutputVInverse.getData();
		rasterVInverse.setPixels(0, 0, width, height, YUVInverseImage.get(2));
		m_imgOutputVInverse.setData(rasterVInverse);
		m_panelImgOutputVInverse.setBufferedImage(m_imgOutputVInverse);	
		
		int[] YUVInverseImageRGB = addRGBChroma(YUVInverseImage, 0);
		
		m_imgOutputYUVInverse = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		WritableRaster rasterYUVInverse = (WritableRaster) m_imgOutputYUVInverse.getData();
		rasterYUVInverse.setPixels(0, 0, newWidth, newHeight, YUVInverseImageRGB);
		m_imgOutputYUVInverse.setData(rasterYUVInverse);
		m_panelImgOutputYUVInverse.setBufferedImage(m_imgOutputYUVInverse);	
	}
	/*
	 * 
	 */
	private static void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("CMPT 365 Term Project:Video Compression");

		mainWindow window = new mainWindow();
		frame.setContentPane(window.createContentPane());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1920, 1020);
		frame.setVisible(true);
		//frame.setExtendedState(frame.getExtendedState() | frame.MAXIMIZED_BOTH);
	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
