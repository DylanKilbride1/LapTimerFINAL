

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class LapTimer extends JFrame {

	private Font counterFont = new Font("Arial", Font.BOLD, 20);
	private Font totalFont = new Font("Arial", Font.PLAIN, 14);

	private JLabel lapLabel = new JLabel("Seconds running:");
	private JTextField lapField = new JTextField(15);
	private JLabel totalLabel = new JLabel("Total seconds:");
	private JTextField totalField = new JTextField(15);


	private JButton startButton = new JButton("START");
	private JButton lapButton = new JButton("LAP");
	private JButton stopButton = new JButton("STOP");

	// The text area and the scroll pane in which it resides
	private JTextArea display;

	private JScrollPane myPane;

	// These represent the menus
	private JMenuItem saveData = new JMenuItem("Save data", KeyEvent.VK_S);
	private JMenuItem displayData = new JMenuItem("Display data", KeyEvent.VK_D);
	
	private JMenu options = new JMenu("Options");

	private JMenuBar menuBar = new JMenuBar();

	private boolean started;

	private float totalSeconds = (float)0.0;
	private float lapSeconds = (float)0.0;

	private int lapCounter = 0;
	File file = new File("Last_Lap_Set.txt");

	private LapTimerThread lapThread;

	private Session currentSession;
	
	private final JLabel lblLapTimeGoal = new JLabel("Lap time goal:");
	private final JTextField goalTextField = new JTextField();
	private final JPanel SetTimePanel = new JPanel();
	private final JLabel lblSeconds = new JLabel("seconds");
	
	private String[] goal_message = {"GOAL REACHED", "GOAL NOT REACHED"};


	public LapTimer() {

		setTitle("Lap Timer Application");

		MigLayout layout = new MigLayout("fillx");
		JPanel panel = new JPanel(layout);
		getContentPane().add(panel);

		options.add(saveData);
		options.add(displayData);
		menuBar.add(options);

		panel.add(menuBar, "spanx, north, wrap");

		MigLayout centralLayout = new MigLayout("fillx");

		JPanel centralPanel = new JPanel(centralLayout);

		GridLayout timeLayout = new GridLayout(0,2);

		JPanel timePanel = new JPanel(timeLayout);

		lapField.setEditable(false);
		lapField.setFont(counterFont);
		lapField.setText("00:00:00.0");

		totalField.setEditable(false);
		totalField.setFont(totalFont);
		totalField.setText("00:00:00.0");
		timePanel.add(lblLapTimeGoal);
		
		timePanel.add(SetTimePanel);
		goalTextField.setText("60");
		goalTextField.setColumns(10);
		SetTimePanel.add(goalTextField);
		SetTimePanel.add(lblSeconds);

		// Setting the alignments of the components
		lblLapTimeGoal.setHorizontalAlignment(SwingConstants.RIGHT);
		goalTextField.setHorizontalAlignment(SwingConstants.CENTER);
		totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lapLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lapField.setHorizontalAlignment(JTextField.CENTER);
		totalField.setHorizontalAlignment(JTextField.CENTER);

		timePanel.add(lapLabel);
		timePanel.add(lapField);
		timePanel.add(totalLabel);
		timePanel.add(totalField);

		centralPanel.add(timePanel, "wrap");

		GridLayout buttonLayout = new GridLayout(1, 3);

		JPanel buttonPanel = new JPanel(buttonLayout);

		buttonPanel.add(startButton);
		buttonPanel.add(lapButton);
		buttonPanel.add(stopButton);

		centralPanel.add(buttonPanel, "spanx, growx, wrap");

		panel.add(centralPanel, "wrap");

		display = new JTextArea(100,150);
		display.setMargin(new Insets(5,5,5,5));
		display.setEditable(false);
		myPane = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(myPane, "alignybottom, h 100:320, wrap");


		// Initial state of system
		started = false;
		currentSession = new Session();

		// Allowing interface to be displayed
		setSize(400, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		/* This method should allow the user to specify a file name to which 
		   to save the contents of the text area using a  JFileChooser. You 
		   should check to see that the file does not already exist in the 
		   system. */
		saveData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					int jopSave = JOptionPane.showConfirmDialog(null, "Are you sure you want "
							+ "to\nsave these lap times?", "Save Confirmation", JOptionPane.YES_NO_OPTION);
			        if (jopSave == JOptionPane.YES_OPTION) {
			          JOptionPane.showMessageDialog(null, "This lap set has been saved!");
			          writeDataFile(file);
			        }
			        else {
			           JOptionPane.showMessageDialog(null, "This lap set has not been saved!");
			        }
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		
			}
		});

		/* This method should retrieve the contents of a file representing a 
		   previous report using a JFileChooser. The result should be displayed 
		   as the contents of a dialog object. */
		displayData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Lap Times Browser");
			      int returnValue = fileChooser.showOpenDialog(null);
			      if (returnValue == JFileChooser.APPROVE_OPTION) {
			        File selectedFile = fileChooser.getSelectedFile();
			        try {
						try {
							display.setText(readDataFile(selectedFile));
						} catch (ClassNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}//<-- here
			      }
			
			}
		});

		/* This method should check to see if the application is already running, 
		   and if not, launch a LapTimerThread object, but if there is another 
		   session already under way, it should ask the user whether they want to 
		   restart - if they do then the existing thread and session should be 
		   reset. The lap counter should be set to 1 and a new Session object 
		   should be created. A new LapTimerThread object should be created with 
		   totalSeconds set to 0.0 and the display area should be cleared. When the 
		   new thread is started, make sure the goal textField is disabled */
			
		
				startButton.addActionListener(new ActionListener() {         //FINSIHED
					public void actionPerformed(ActionEvent e) {
						
					if(lapThread == null){
						lapThread = new LapTimerThread(LapTimer.this, 0.0f);
						currentSession.setAverage();
						currentSession.setTotalTime();
						EnableGoalEditing(false);
					
						
					} else if(lapThread.isRunning()){
						
						int jop = JOptionPane.showInternalConfirmDialog(getContentPane(), "By clicking yes, you will restart the timer.\nAll"
								+ " progress will be lost.\nAre you sure you want to restart?\n", "WARNING", JOptionPane.WARNING_MESSAGE);
							if(jop == JOptionPane.YES_OPTION){
							
								lapThread.setRunning(false);
								lapThread = new LapTimerThread(LapTimer.this, 0.0f);
								display.setText("");
								currentSession.setAverage();
								currentSession.setTotalTime();
							} 
					
					} else {
						lapThread = new LapTimerThread(LapTimer.this, 0.0f);
						display.setText("");
						currentSession.setAverage();
						currentSession.setTotalTime();
						}
					}
				});
	

		/* 1) This method should only work if a session has been started. Once started,
		   2) clicking the Lap button should cause the length of the current lap to be
		   retrieved and used to create a new Lap object 3) which is added to the 
		   session collection. 4) The old LapTimerThread object should be stopped and 
		   a new thread should be started with the updated value of total seconds.
		   5) The lap number and time should be added to the display area. The message 
		   saying if the goal was reached also need to be added */
		lapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try{
				/*1)*/ if(lapThread.isRunning()){ // System.out.println();
						
							lapCounter ++; //Increments lap e.g. 'Lap: 1, Lap: 2' etc
						/*2)*/	Lap newLapForVector = new Lap(lapCounter, lapThread.getLapSeconds() -.1f); //Creates a lap object //LAP SECONDS ARE STILL IN FLOAT FORMAT!!
								//System.out.println(lapField.getText() + "\t" + convertToHMSString(lapThread.getLapSeconds() -.1f)); //Subtracting .1f corrects the 100ms latency
						/*5)*/  // DON'T FORGET ABOUT THE GOALS - goal is in seconds
									if(lapThread.getLapSeconds() > Integer.valueOf(goalTextField.getText())){
										display.append("Lap " + lapCounter + ":\t" + lapField.getText() + "\tGOAL NOT REACHED\n"); //Using getText gives the correct time - otherwise 100ms latency
									} else if(lapThread.getLapSeconds() < Integer.valueOf(goalTextField.getText())){
										display.append("Lap " + lapCounter + ":\t" + lapField.getText() + "\tGOAL REACHED\n");
								}
						/*3)*/	currentSession.addLap(newLapForVector); //Adds each lap object to the vector
						/*4)*/  lapThread.setRunning(false); //Stops the current thread
								float temporaryTotal = lapThread.getTotalSeconds(); //Sets the value in total seconds to - temporaryTotal
								lapThread = new LapTimerThread(LapTimer.this, temporaryTotal); //Starts a new thread with temporaryTotal as an arg
						/*5)*/  // DON'T FORGET ABOUT THE GOALS - goal is in seconds
								
							
						
					}
				}catch(NullPointerException npe1){
					JOptionPane.showMessageDialog(null, "Lap button will only function once\nthe clock has started!", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});

		/* This method should have most of the same functionality as the Lap
		   button's action listener, except that a new LapTimerThread object is
		   NOT started. In addition, the total time for all the laps should be 
		   calculated and displayed in the text area, along with the average lap
		   time and the numbers and times of the fastest and slowest laps. */
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try{
					if(lapThread.isRunning()){
						lapThread.setRunning(false); //Stop the thread - stops timer
						lapCounter++; //Resets lap counter to 0 incase user wants to start a new session
					
						Lap newLapForVector = new Lap(lapCounter, lapThread.getLapSeconds() -.1f);
						currentSession.addLap(newLapForVector);
						if(lapThread.getLapSeconds() > Integer.valueOf(goalTextField.getText())){
							display.append("Lap " + lapCounter + ":\t" + lapField.getText() + "\tGOAL NOT REACHED\n"); //Using getText gives the correct time - otherwise 100ms latency 
						} else if(lapThread.getLapSeconds() < Integer.valueOf(goalTextField.getText())){
							display.append("Lap " + lapCounter + ":\t" + lapField.getText() + "\tGOAL REACHED\n");
						}
						lapCounter = 0;
						display.append("\n\nStopped at:\t\t" + convertToHMSString(lapThread.getTotalSeconds()));
						display.append("\nAverageTime:\t\t" + convertToHMSString(currentSession.calculateAverageTime()));
						display.append("\n\nQuickest Lap: " + currentSession.getFastestLap().getId() +
								"\t\t" + convertToHMSString(currentSession.getFastestLap().getLapTime()));
						display.append("\n\nSlowest Lap: " + currentSession.getSlowestLap().getId() +
								"\t\t" + convertToHMSString(currentSession.getSlowestLap().getLapTime()));
						EnableGoalEditing(true);
					}
					}catch(NullPointerException npe2){
						JOptionPane.showMessageDialog(null, "Stop button will only function once\nthe clock has started!", "ERROR", JOptionPane.ERROR_MESSAGE);
					}
								
			}
		});

	}
	
	/* These two methods are used by the LapTimerThread to update the values
	   displayed in the two text fields. Each value is formatted as a 
	   hh:mm:ss.S string by calling the convertToHMSString method below/. */

	public void updateLapDisplay(float value) {

		lapField.setText(convertToHMSString(value));

	}

	public void updateTotalDisplay(float value) {

		totalField.setText(convertToHMSString(value));

	}
	
	/* These methods are here to help access the
	 *  goaltextField in the GUI */
	
	public String getGoalValue(){
		return  goalTextField.getText();
	}
	
	public void EnableGoalEditing(boolean makeEditable){
		goalTextField.setEditable(makeEditable);
	}
	
	public void setTextArea(String str){
		display.setText(str);
	}

	private String convertToHMSString(float seconds) {
		long msecs, secs, mins, hrs;
		// String to be displayed
		String returnString = "";

		// Split time into its components

		long secondsAsLong = (long)(seconds * 10);

		msecs = secondsAsLong % 10;
		secs = (secondsAsLong / 10) % 60;
		mins = ((secondsAsLong / 10) / 60) % 60;
		hrs = ((secondsAsLong / 10) / 60) / 60;

		// Insert 0 to ensure each component has two digits
		if (hrs < 10) {
			returnString = returnString + "0" + hrs;
		}
		else returnString = returnString + hrs;
		returnString = returnString + ":";

		if (mins < 10) {
			returnString = returnString + "0" + mins;		
		}
		else returnString = returnString + mins;
		returnString = returnString + ":";

		if (secs < 10) {
			returnString = returnString + "0" + secs;		
		}
		else returnString = returnString + secs;

		returnString = returnString + "." + msecs;

		return returnString;

	}

	/* These methods will be used by the action listeners attached
	   to the two menu items. */

	public synchronized void writeDataFile(File f) throws IOException, FileNotFoundException {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			out.writeObject(display.getText());
		} 
		catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "File Not Found Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "I/O Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		finally {
			out.close();
		}
	}

	public synchronized String readDataFile(File f) throws IOException, ClassNotFoundException {
		ObjectInputStream in = null;
		String result = new String();
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			result = (String)in.readObject();
		}
		catch (ClassCastException ex) {
			JOptionPane.showMessageDialog(this, "Class Cast Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "I/O Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Exception\n " + ex.toString(), "Error Message", JOptionPane.ERROR_MESSAGE);
		}
		finally {
			in.close();
		}   
		return result;
	}

	public static void main(String[] args) {

		LapTimer timer = new LapTimer();
		timer.setVisible(true);

	}


}
