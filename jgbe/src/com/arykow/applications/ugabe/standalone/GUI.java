/* ==========================================================================
 * GNU GENERAL PUBLIC LICENSE
 * Version 2, June 1991
 * 
 * Copyright (C) 1989, 1991 Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * 
 * $LastChangeDate$
 * $Rev$
 * $LastChangedBy$
 * $URL$
 * $Id$
 * ========================================================================== */
package com.arykow.applications.ugabe.standalone;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import com.arykow.applications.ugabe.client.CPU;
import com.arykow.applications.ugabe.client.CPURunner;
import com.arykow.applications.ugabe.client.CPUServer;
import com.arykow.applications.ugabe.client.Cartridge;
import com.arykow.applications.ugabe.client.CartridgeController;
import com.arykow.applications.ugabe.client.CartridgeCreateHandler;
import com.arykow.applications.ugabe.client.ImageRenderer;
import com.arykow.applications.ugabe.client.IntVector;
import com.arykow.applications.ugabe.client.UGABEService;
import com.arykow.applications.ugabe.client.UGABEServiceAsync;
import com.arykow.applications.ugabe.client.Version;
import com.arykow.applications.ugabe.client.VideoController;
import com.arykow.applications.ugabe.server.UGABEServiceController;
import com.arykow.applications.ugabe.standalone.Debugger.DebugRunner;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class GUI extends JApplet implements ActionListener, ItemListener, KeyListener, ComponentListener, WindowListener, MouseMotionListener, FocusListener {
	private static final long serialVersionUID = -5280334034698204592L;

	private IntVector saveStateOrder = new IntVector();
	private int lastmousex;
	int lastmousey;
	int lastmousecnt;
	int mousehidden = 0;
	protected VideoController VC;
	protected transient CPUServer server = new CPUServerImpl();
	ImageRendererGUI imageRenderer = new ImageRendererGUI(this);
	protected CPU cpu = new CPU(server, imageRenderer);
	protected AudioDriver audioDriver;
	int fps;
	private boolean isApplet;
	private int selectedState = 0;
	private int[] keyStates = new int[KeyConfigurationDialog.KEY_STATES_LENGTH];
	static ArrayList<String> osdLines = new ArrayList<String>();
	ArrayList<String> rcFiles = new ArrayList<String>();
	static int osdTimer = 1;
	private static int titleUpdateCountDown = 0;
	public String curcartname;
	public String biosfilename;
	public DataOutputStream speedRunPlayWithOutputVideoStream;

	JMenuItem menuitemExit;
	MenuItemArrayGroup scaleRadioGroup;
	MenuItemArrayGroup interpolationRadioGroup;
	JCheckBoxMenuItem keepAspectRatio;
	JCheckBoxMenuItem enableFullScreen;
	JMenuItem menuitemOpenROM;
	JMenuItem menuitemSaveState;
	JMenuItem menuitemLoadState;
	JMenuItem menuitemSaveOldestState;
	JMenuItem menuitemIncFrameSkip;
	JMenuItem menuitemDecFrameSkip;
	JCheckBoxMenuItem menuitemEnableSound;
	MenuItemArrayGroup savestateRadioGroup;
	JMenuItem[] menuItems;
	JMenuItem menuitemSeparator;
	JMenuItem menuitemPause;
	JMenuItem menuitemReset;
	JMenu menuColorScheme;
	JCheckBoxMenuItem[] menuitemColorSchemes;
	JCheckBoxMenuItem menuitemMixFrame;
	JCheckBoxMenuItem menuitemIgnoreSTAT;

	JMenu menuSoundChannels;
	MenuItemArray soundChannelGroup;
	JMenu menuOther;
	JMenuItem menuitemCheats;
	JCheckBoxMenuItem menuitemEnableCheats;
	JCheckBoxMenuItem menuitemUseBIOS;
	JMenuItem menuitemSetBIOS;
	JMenu menuEmuSpeed;
	JMenuItem menuitemEmuSpeedPlus;
	JMenuItem menuitemEmuSpeedMinus;
	JMenuItem menuitemEmuSpeedMul;
	JMenuItem menuitemEmuSpeedDiv;
	JMenuItem menuitemEmuSpeedNormal;
	JMenu menuLink;
	JMenuItem menuitemLinkServe;
	JMenuItem menuitemLinkClient;
	JMenuItem menuitemLinkSever;
	JMenuItem menuitemConfigKeys;
	JCheckBoxMenuItem menuitemToggleRemoteKeys;
	JCheckBoxMenuItem menuitemSpeedRunRecord;
	JCheckBoxMenuItem menuitemSpeedRunPlay;
	JCheckBoxMenuItem menuitemSpeedRunPlayWithOutput;
	JMenuItem menuitemShowAdvancedAudioPropertiesDialog;

	CheatCodeEditor cheatcodes;
	AdvancedAudioPropertiesDialog advancedAudioPropertiesDialog;
	CPURunner cpuRunner;

	protected ColorSelector cs;

	public void updateCartName(String fname) {
		int slashPos = fname.lastIndexOf(File.separator);
		int dotPos = fname.lastIndexOf(".");
		if (dotPos == -1)
			dotPos = fname.length();
		curcartname = fname.substring(slashPos + 1, dotPos);

		rcFiles.remove(fname);
		rcFiles.add(0, fname);
		while (rcFiles.size() > 10)
			rcFiles.remove(rcFiles.size() - 1);
		saveRCFiles();
	}

	class GlobalExceptionCatcher implements UncaughtExceptionHandler {
		public void uncaughtException(Thread t, Throwable ee) {
			final Throwable e = ee;
			if ((cpuRunner != null) && hasThread(cpuRunner, t)) {
				cpuRunner = null;
			} else {
				pauseEmulation(false);
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JFrame errMsg = new JFrame("Internal Error!");
					errMsg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					JPanel iconAndText = new JPanel();
					JPanel iconAndTextAndTextField = new JPanel();
					BoxLayout bl;
					bl = new BoxLayout(iconAndText, BoxLayout.X_AXIS);
					iconAndText.setLayout(bl);
					iconAndTextAndTextField.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
					bl = new BoxLayout(iconAndTextAndTextField, BoxLayout.PAGE_AXIS);
					iconAndTextAndTextField.setLayout(bl);
					iconAndText.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
					JPanel spacer = new JPanel();
					iconAndText.add(spacer);
					iconAndText.add(new JLabel("<HTML><BODY>" + "There was an error within the emulator itself, this should not happen.<BR>" + "Please go to `http://code.google.com/p/jgbe/issues/' and submit a bug report with<BR>" + "the full text of the error message (as shown in the textfield below), and<BR>" + "(if possible) the steps neccesary to reproduce the error. Doing so will help<BR>" + "us fix this bug and improve JGBE.<BR>" + "</BODY></HTML>"));
					JTextArea tf = new JTextArea();
					tf.setEditable(false);
					iconAndTextAndTextField.add(iconAndText);
					spacer = new JPanel();
					bl = new BoxLayout(spacer, BoxLayout.X_AXIS);
					spacer.setLayout(bl);
					spacer.add(new JLabel("Error message:"));
					spacer.add(new JPanel());
					iconAndTextAndTextField.add(spacer);
					tf.setBorder(BorderFactory.createLoweredBevelBorder());
					iconAndTextAndTextField.add(new JScrollPane(tf));
					String s = "Type of error: \"" + e.toString() + "\"\n" + "Stacktrace:\n";
					StackTraceElement[] ste = e.getStackTrace();
					for (int i = 0; i < ste.length; ++i) {
						s += ste[i] + "\n";
					}
					tf.setText(s);
					errMsg.getContentPane().add(iconAndTextAndTextField);
					errMsg.pack();
					errMsg.setSize(new Dimension(640, 480));
					Point p = new Point();
					Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
					p.setLocation((d.getWidth() / 2) - (errMsg.getWidth() / 2), (d.getHeight() / 2) - (errMsg.getHeight() / 2));
					errMsg.setLocation(p);
					errMsg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					errMsg.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent we) {
							System.exit(-1);
						}
					});
					errMsg.setVisible(true);
				}
			});
		}

		private boolean hasThread(CPURunner cpuRunner, Thread t) {
			boolean result = false;
			if (cpuRunner instanceof SimpleCPURunner) {
				result = ((SimpleCPURunner) cpuRunner).hasThread(t);
			} else if (cpuRunner instanceof DebugRunner) {
				result = ((DebugRunner) cpuRunner).hasThread(t);
			}
			return result;
		}
	}

	public static void addOSDLine(String line) {
		osdLines.add(line);
		osdTimer = 4 * 2;
	}

	public GUI() {
		isApplet = false;
	}

	private JMenu menuFile;

	JMenuBar mainMenuBar;

	private JMenuBar createJMenuBar() {
		JMenuBar mainMenuBar;
		JMenu menuVideo;
		JMenu menuScaling;
		JMenu menuFrameSkip;
		JMenu menuState;
		JMenu menuSound;
		mainMenuBar = new JMenuBar();

		menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		mainMenuBar.add(menuFile);
		menuVideo = new JMenu("Video");
		menuVideo.setMnemonic(KeyEvent.VK_V);
		mainMenuBar.add(menuVideo);

		menuitemOpenROM = new JMenuItem("Open ROM", KeyEvent.VK_O);
		menuitemOpenROM.addActionListener(this);
		menuFile.add(menuitemOpenROM);
		menuitemPause = new JMenuItem("Pause", KeyEvent.VK_P);
		menuitemPause.addActionListener(this);
		menuFile.add(menuitemPause);
		menuitemReset = new JMenuItem("Reset", KeyEvent.VK_R);
		menuitemReset.addActionListener(this);
		menuFile.add(menuitemReset);

		menuitemExit = new JMenuItem("Exit", KeyEvent.VK_X);
		menuitemExit.addActionListener(this);
		menuFile.add(menuitemExit);

		enableFullScreen = new JCheckBoxMenuItem("Full Screen");
		enableFullScreen.setMnemonic(KeyEvent.VK_F);
		enableFullScreen.addActionListener(this);
		menuVideo.add(enableFullScreen);

		menuitemMixFrame = new JCheckBoxMenuItem("Mix Frame");
		menuitemMixFrame.setMnemonic(KeyEvent.VK_M);
		menuitemMixFrame.addActionListener(this);
		menuVideo.add(menuitemMixFrame);

		menuVideo.addSeparator();

		menuScaling = new JMenu("Scaling");
		scaleRadioGroup = new MenuItemArrayGroup();
		scaleRadioGroup.add(new JRadioButtonMenuItem("Scale 1x"), KeyEvent.VK_1);
		scaleRadioGroup.add(new JRadioButtonMenuItem("Scale 2x"), KeyEvent.VK_2);
		scaleRadioGroup.add(new JRadioButtonMenuItem("Scale 3x"), KeyEvent.VK_3);
		scaleRadioGroup.add(new JRadioButtonMenuItem("Scale 4x"), KeyEvent.VK_4);
		scaleRadioGroup.addActionListener(this);
		scaleRadioGroup.addToMenu(menuScaling);
		menuScaling.addSeparator();

		interpolationRadioGroup = new MenuItemArrayGroup();
		interpolationRadioGroup.add(new JRadioButtonMenuItem("No Interpolation (fixed window size)"));
		interpolationRadioGroup.add(new JRadioButtonMenuItem("Nearest Neighbour Interpolation"));
		interpolationRadioGroup.add(new JRadioButtonMenuItem("BiLinear Interpolation"));
		interpolationRadioGroup.add(new JRadioButtonMenuItem("BiCubic Interpolation"));

		interpolationRadioGroup.addActionListener(this);
		interpolationRadioGroup.addToMenu(menuScaling);

		menuScaling.addSeparator();
		keepAspectRatio = new JCheckBoxMenuItem("Keep ascpect ratio");
		keepAspectRatio.addActionListener(this);
		menuScaling.add(keepAspectRatio);

		menuVideo.add(menuScaling);

		menuFrameSkip = new JMenu("Frame Skipping");
		menuitemIncFrameSkip = new JMenuItem("Increase Frame Skip", KeyEvent.VK_I);
		menuitemDecFrameSkip = new JMenuItem("Decrease Frame Skip", KeyEvent.VK_D);
		menuitemIncFrameSkip.addActionListener(this);
		menuitemDecFrameSkip.addActionListener(this);
		menuFrameSkip.add(menuitemIncFrameSkip);
		menuFrameSkip.add(menuitemDecFrameSkip);
		menuVideo.add(menuFrameSkip);

		menuColorScheme = new JMenu("Color scheme");
		menuitemColorSchemes = new JCheckBoxMenuItem[5];
		menuitemColorSchemes[0] = new JCheckBoxMenuItem("GameBoy LCD", false);
		menuitemColorSchemes[0].setMnemonic(KeyEvent.VK_L);
		menuitemColorSchemes[1] = new JCheckBoxMenuItem("GameBoy Pocket/Light", false);
		menuitemColorSchemes[1].setMnemonic(KeyEvent.VK_P);
		menuitemColorSchemes[2] = new JCheckBoxMenuItem("Black and White", false);
		menuitemColorSchemes[2].setMnemonic(KeyEvent.VK_B);
		menuitemColorSchemes[3] = new JCheckBoxMenuItem("Graytones", false);
		menuitemColorSchemes[3].setMnemonic(KeyEvent.VK_G);
		menuitemColorSchemes[4] = new JCheckBoxMenuItem("Custom", false);
		menuitemColorSchemes[4].setMnemonic(KeyEvent.VK_C);
		for (int i = 0; i < menuitemColorSchemes.length; ++i) {
			menuColorScheme.add(menuitemColorSchemes[i]);
			menuitemColorSchemes[i].addActionListener(this);
		}
		menuVideo.add(menuColorScheme);

		menuState = new JMenu("State");
		menuState.setMnemonic(KeyEvent.VK_S);
		menuitemSaveState = new JMenuItem("Save State", KeyEvent.VK_S);
		menuitemLoadState = new JMenuItem("Load State", KeyEvent.VK_L);
		menuitemSaveOldestState = new JMenuItem("Save To Oldest State", KeyEvent.VK_O);

		menuitemSaveState.addActionListener(this);
		menuitemLoadState.addActionListener(this);
		menuitemSaveOldestState.addActionListener(this);

		menuState.add(menuitemSaveState);
		menuState.add(menuitemLoadState);
		menuState.add(menuitemSaveOldestState);
		menuState.addSeparator();
		savestateRadioGroup = new MenuItemArrayGroup();
		for (int i = 0; i < 10; ++i) {
			savestateRadioGroup.add(new JRadioButtonMenuItem("Select State " + i), KeyEvent.VK_0 + i);
			savestateRadioGroup.get(i).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0));
		}
		savestateRadioGroup.addActionListener(this);
		savestateRadioGroup.addToMenu(menuState);
		savestateRadioGroup.setSelectedIndex(0);

		mainMenuBar.add(menuState);

		menuSound = new JMenu("Sound");
		menuSound.setMnemonic(KeyEvent.VK_U);
		menuitemEnableSound = new JCheckBoxMenuItem("Enable sound", true);
		menuitemEnableSound.setMnemonic(KeyEvent.VK_T);
		menuitemEnableSound.addActionListener(this);
		menuSound.add(menuitemEnableSound);

		menuSoundChannels = new JMenu("Channels");
		soundChannelGroup = new MenuItemArray();
		soundChannelGroup.add(new JCheckBoxMenuItem("Enable channel 1"), KeyEvent.VK_1);
		soundChannelGroup.add(new JCheckBoxMenuItem("Enable channel 2"), KeyEvent.VK_2);
		soundChannelGroup.add(new JCheckBoxMenuItem("Enable channel 3"), KeyEvent.VK_3);
		soundChannelGroup.add(new JCheckBoxMenuItem("Enable channel 4"), KeyEvent.VK_4);
		soundChannelGroup.addActionListener(this);
		soundChannelGroup.addToMenu(menuSoundChannels);

		menuSound.add(menuSoundChannels);
		menuitemShowAdvancedAudioPropertiesDialog = new JMenuItem("Advanced...", KeyEvent.VK_A);
		menuitemShowAdvancedAudioPropertiesDialog.addActionListener(this);
		menuSound.add(menuitemShowAdvancedAudioPropertiesDialog);
		mainMenuBar.add(menuSound);
		menuOther = new JMenu("Other");
		menuOther.setMnemonic(KeyEvent.VK_O);
		menuitemCheats = new JMenuItem("Edit cheats", KeyEvent.VK_C);
		menuitemCheats.addActionListener(this);
		menuitemEnableCheats = new JCheckBoxMenuItem("Enable cheat codes", false);
		menuitemEnableCheats.setMnemonic(KeyEvent.VK_T);
		menuitemEnableCheats.addActionListener(this);
		menuitemUseBIOS = new JCheckBoxMenuItem("Run BIOS on reset", false);
		menuitemUseBIOS.setMnemonic(KeyEvent.VK_B);
		menuitemUseBIOS.addActionListener(this);
		menuitemSetBIOS = new JMenuItem("Select BIOS ROM", KeyEvent.VK_B);
		menuitemSetBIOS.addActionListener(this);

		menuitemToggleRemoteKeys = new JCheckBoxMenuItem("Use Remote Keys", false);
		menuitemToggleRemoteKeys.setMnemonic(KeyEvent.VK_R);
		menuitemToggleRemoteKeys.addActionListener(this);

		menuLink = new JMenu("Link");
		menuLink.setMnemonic(KeyEvent.VK_L);
		menuitemLinkServe = new JMenuItem("Start Link Server", KeyEvent.VK_S);
		menuitemLinkClient = new JMenuItem("Start Link Client", KeyEvent.VK_C);
		menuitemLinkSever = new JMenuItem("Stop Link", KeyEvent.VK_L);
		menuitemLinkServe.addActionListener(this);
		menuitemLinkClient.addActionListener(this);
		menuitemLinkSever.addActionListener(this);
		menuLink.add(menuitemLinkServe);
		menuLink.add(menuitemLinkClient);
		menuLink.add(menuitemLinkSever);
		mainMenuBar.add(menuLink);

		menuitemConfigKeys = new JMenuItem("Config keys", KeyEvent.VK_1);
		menuitemConfigKeys.addActionListener(this);

		menuitemSpeedRunRecord = new JCheckBoxMenuItem("Record speedrun", false);
		menuitemSpeedRunPlay = new JCheckBoxMenuItem("Play speedrun", false);
		menuitemSpeedRunPlayWithOutput = new JCheckBoxMenuItem("Record speedrun to file", false);
		menuitemSpeedRunRecord.setMnemonic(KeyEvent.VK_E);
		menuitemSpeedRunPlay.setMnemonic(KeyEvent.VK_P);
		menuitemSpeedRunPlayWithOutput.setMnemonic(KeyEvent.VK_F);
		menuitemSpeedRunRecord.addActionListener(this);
		menuitemSpeedRunPlay.addActionListener(this);
		menuitemSpeedRunPlayWithOutput.addActionListener(this);

		menuOther.add(menuitemEnableCheats);
		menuOther.add(menuitemUseBIOS);
		menuOther.add(menuitemToggleRemoteKeys);
		menuOther.add(menuitemCheats);
		menuOther.add(menuitemSetBIOS);
		menuOther.add(menuitemConfigKeys);

		menuEmuSpeed = new JMenu("Emulation Speed");
		menuitemEmuSpeedPlus = new JMenuItem("+25%", KeyEvent.VK_PLUS);
		menuitemEmuSpeedMinus = new JMenuItem("-25%", KeyEvent.VK_MINUS);
		menuitemEmuSpeedMul = new JMenuItem("*125%", KeyEvent.VK_OPEN_BRACKET);
		menuitemEmuSpeedDiv = new JMenuItem("*75%", KeyEvent.VK_CLOSE_BRACKET);
		menuitemEmuSpeedNormal = new JMenuItem("100%", KeyEvent.VK_EQUALS);
		menuitemEmuSpeedPlus.addActionListener(this);
		menuitemEmuSpeedMinus.addActionListener(this);
		menuitemEmuSpeedMul.addActionListener(this);
		menuitemEmuSpeedDiv.addActionListener(this);
		menuitemEmuSpeedNormal.addActionListener(this);

		menuEmuSpeed.add(menuitemEmuSpeedPlus);
		menuEmuSpeed.add(menuitemEmuSpeedMinus);
		menuEmuSpeed.add(menuitemEmuSpeedMul);
		menuEmuSpeed.add(menuitemEmuSpeedDiv);
		menuEmuSpeed.add(menuitemEmuSpeedNormal);
		menuOther.add(menuEmuSpeed);
		menuOther.addSeparator();
		menuOther.add(menuitemSpeedRunRecord);
		menuOther.add(menuitemSpeedRunPlay);
		menuOther.add(menuitemSpeedRunPlayWithOutput);

		menuitemIgnoreSTAT = new JCheckBoxMenuItem("Ignore mode2/3 writelock", false);
		menuitemIgnoreSTAT.setMnemonic(KeyEvent.VK_I);
		menuitemIgnoreSTAT.addActionListener(this);
		menuitemIgnoreSTAT.setAccelerator(KeyStroke.getKeyStroke(73, 2));
		menuOther.add(menuitemIgnoreSTAT);

		mainMenuBar.add(menuOther);

		return mainMenuBar;
	}

	private void applyAccelerators() {
		int keyMapIndex = 16;

		menuitemConfigKeys.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemOpenROM.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemPause.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemReset.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemExit.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		enableFullScreen.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemMixFrame.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		for (int i = 0; i < 4; ++i)
			scaleRadioGroup.get(i).setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		keepAspectRatio.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		for (int i = 0; i < 4; ++i)
			interpolationRadioGroup.get(i).setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemIncFrameSkip.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemDecFrameSkip.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemColorSchemes[0].setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemColorSchemes[1].setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemColorSchemes[2].setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemColorSchemes[3].setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemColorSchemes[4].setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemSaveState.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemLoadState.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemSaveOldestState.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemEnableSound.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		for (int i = 0; i < 4; ++i)
			soundChannelGroup.get(i).setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemCheats.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemEnableCheats.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemEmuSpeedPlus.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemEmuSpeedMinus.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemEmuSpeedMul.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemEmuSpeedDiv.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		menuitemEmuSpeedNormal.setAccelerator(KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8));
		KeyStroke.getKeyStroke(keyStates[keyMapIndex] & 0xff, keyStates[keyMapIndex++] >> 8);
	}

	public void addComponentsToPane(Container contentPane) {
		imageRenderer.setFocusable(true);

		if (System.getProperty("os.name").equals("Linux"))
			imageRenderer.addKeyListener(new TimedKeyListener(this));
		else
			imageRenderer.addKeyListener(this);

		imageRenderer.setDoubleBuffered(false);
		contentPane.add(imageRenderer);
	}

	static JFrame frame;
	static JFrame fsframe;

	private void createGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			System.out.println(ex);
		}

		frame = new JFrame("JGameBoy Emulator V" + Version.str);
		fsframe = new JFrame("JGameBoy Emulator V" + Version.str);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		fsframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.getContentPane().setLayout(null);
		fsframe.getContentPane().setLayout(null);

		fsframe.getContentPane().setBackground(Color.BLACK);

		fsframe.setUndecorated(true);

		mainMenuBar = createJMenuBar();
		frame.setJMenuBar(mainMenuBar);
		addComponentsToPane(frame.getContentPane());
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		URL imgurl = getClass().getClassLoader().getResource("icon.gif");
		if (imgurl != null) {
			Image image = toolkit.getImage(imgurl);
			frame.setIconImage(image);
		} else {
			System.out.println("Can't load JGBE icon!");
		}
		frame.addComponentListener(this);
		frame.addWindowListener(this);
		fsframe.addWindowListener(this);
		mainMenuBar.addMouseMotionListener(this);
		imageRenderer.addFocusListener(this);
		fsframe.addMouseMotionListener(this);
		for (int i = 0; i < mainMenuBar.getMenuCount(); ++i)
			mainMenuBar.getMenu(i).addMouseMotionListener(this);

		frame.setResizable(false);
		frame.pack();

		JavaCrossplatformnessIsAMyth transferhandler = new JavaCrossplatformnessIsAMyth();
		imageRenderer.setTransferHandler(transferhandler);
	}

	public void showGUI() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Point p = new Point();
		p.setLocation((((double)d.getWidth()) / 2) - (((double)frame.getWidth()) / 2), (((double)d.getHeight()) / 2) - (((double)frame.getHeight()) / 2));
		frame.setLocation(p);
		frame.setVisible(true);
	}

	public class JavaCrossplatformnessIsAMyth extends TransferHandler {
		private static final long serialVersionUID = 793656254418511854L;

		private static final String URI_LIST = "uri-list";

		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			boolean result = false;
			for (int i = 0; !result && i < transferFlavors.length; ++i) {
				DataFlavor flavor = transferFlavors[i];
				result = flavor.equals(DataFlavor.javaFileListFlavor) || URI_LIST.equals(flavor.getSubType());
			}
			return result;
		}

		public boolean importData(JComponent comp, Transferable t) {
			ArrayList<File> files = null;
			try {
				for (int i = 0; i < t.getTransferDataFlavors().length; ++i) {
					DataFlavor flavor = t.getTransferDataFlavors()[i];
					Object obj = t.getTransferData(flavor);

					if (DataFlavor.javaFileListFlavor.equals(flavor)) {
						files = new ArrayList<File>();
						for(Object object : Collection.class.cast(t.getTransferData(flavor))) {
							files.add(File.class.cast(object));
						}

					} else if (URI_LIST.equals(flavor.getSubType()) && obj instanceof String) {
						String urilist = (String) obj;
						Scanner scanner = new Scanner(urilist.trim());
						files = new ArrayList<File>();
						while (scanner.hasNextLine()) {
							files.add(new File(new URI(scanner.nextLine())));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			if (files != null)
				if (files.size() == 1)
					tryToLoadROM(files.get(0).toString());

			return true;
		}
	}

	class SimpleCPURunner implements CPURunner, Runnable {
		private volatile int threadStatus = 0;
		private Thread cpurunthread;

		public boolean hasThread(Thread t) {
			return cpurunthread.equals(t);
		}

		synchronized public void suspend() {
			while (threadStatus != 0) {
				cpu.keeprunning = false;
				threadStatus = 3;
				while (threadStatus == 3) {
					{
						try {
							Thread.sleep(100);
						} catch (Exception e) {
						}
					}
					;
				}
				;
			}
		}

		synchronized public void resume() {
			if (!cpu.canRun())
				return;
			if (threadStatus != 2) {
				threadStatus = 1;
				while (threadStatus == 1) {
					{
						try {
							Thread.sleep(100);
						} catch (Exception e) {
						}
					}
					;
				}
				;
			}
		}

		public boolean isRunning() {
			return (threadStatus != 0);
		}

		SimpleCPURunner() {
			cpurunthread = new Thread(this);
			cpurunthread.start();
			while (!cpurunthread.isAlive()) {
				{
					try {
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				;
			}
			;
		}

		public void run() {
			while (true) {

				while (threadStatus == 0) {
					{
						try {
							Thread.sleep(100);
						} catch (Exception e) {
						}
					}
					;
				}
				;

				if (threadStatus == 1)
					threadStatus = 2;

				cpu.runloop();

				if (threadStatus == 2) {
					threadStatus = 3;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(null, "Encountered an invalid instruction, perhaps ROM is broken?", "Invalid Instruction", JOptionPane.INFORMATION_MESSAGE);
						}
					});
				}

				if (threadStatus == 3)
					threadStatus = 0;

			}
		}
	}

	boolean focus_pause = false;

	public void focusGained(FocusEvent e) {
		if (!debug)
			if (focus_pause)
				resumeEmulation(false);
	}

	public void focusLost(FocusEvent e) {
		if (!debug) {
			focus_pause = cpuRunner.isRunning();
			if (!cs.isVisible()) {
				pauseEmulation(false);
			}
		}
	}

	boolean fulls = false;

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		if (fulls != enableFullScreen.getState()) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			if (enableFullScreen.getState()) {
				frame.setVisible(false);

				fsframe.setJMenuBar(null);
				fsframe.getContentPane().add(mainMenuBar);
				fsframe.getContentPane().add(imageRenderer);

				gd.setFullScreenWindow(fsframe);
			} else {
				gd.setFullScreenWindow(null);
				fsframe.setVisible(false);
				frame.setResizable(imageRenderer.interpolation != 0);
				frame.getContentPane().add(imageRenderer);
				frame.setJMenuBar(mainMenuBar);
				frame.setVisible(true);
			}
			fulls = enableFullScreen.getState();
		}

		Dimension psize;
		Dimension csize;
		csize = (fulls ? fsframe : frame.getContentPane()).getSize();
		if (imageRenderer.interpolation == 0) {
			psize = new Dimension(imageRenderer.nscale * ImageRenderer.SCREEN_WIDTH, imageRenderer.nscale * ImageRenderer.SCREEN_HEIGHT);
			if (!fulls) {
				Dimension fsize = frame.getSize();
				Dimension bsize = new Dimension();
				bsize.width = ((psize.width + (fsize.width - csize.width)) < (frame.getPreferredSize().width) ? (frame.getPreferredSize().width) : (psize.width + (fsize.width - csize.width)));
				bsize.height = ((psize.height + (fsize.height - csize.height)) < (frame.getPreferredSize().height) ? (frame.getPreferredSize().height) : (psize.height + (fsize.height - csize.height)));

				frame.setSize(bsize);
			}
		} else {
			psize = new Dimension(csize);
			if (keepAspectRatio.getState()) {
				double ratio = psize.getWidth() / psize.getHeight();
				double target = ((double) ImageRenderer.SCREEN_WIDTH) / ((double) ImageRenderer.SCREEN_HEIGHT);
				if (ratio < target)
					psize.height = (int) Math.round(psize.getWidth() / target);
				else
					psize.width = (int) Math.round(psize.getHeight() * target);
			}
		}
		Rectangle trect = new Rectangle(psize);
		if (psize.width < csize.width)
			trect.translate((csize.width - psize.width) / 2, 0);
		if (psize.height < csize.height)
			trect.translate(0, (csize.height - psize.height) / 2);
		imageRenderer.setBounds(trect);
		if (fulls)
			mainMenuBar.setBounds(new Rectangle(mainMenuBar.getPreferredSize()));
		imageRenderer.repaint();
		saveConfig();
	}

	public void pauseEmulation(boolean verbose) {
		if (cpuRunner != null) {
			cpuRunner.suspend();
			if (verbose)
				addOSDLine("Paused gameboy");
		}
	}

	public void resumeEmulation(boolean verbose) {
		if ((cpuRunner != null) && (cart != null)) {
			cpuRunner.resume();
			if (verbose)
				addOSDLine("Unpaused gameboy");
		}
	}

	public void actionPerformed(ActionEvent e) {

		boolean configStateChanged = false;
		if (e.getSource().equals(menuitemIgnoreSTAT)) {
			VC.allow_writes_in_mode_2_3 = menuitemIgnoreSTAT.getState();
			configStateChanged = true;
		} else if (e.getSource().equals(menuitemExit)) {
			windowClosed(null);
		} else if (scaleRadioGroup.contains(e.getSource())) {
			int idx = scaleRadioGroup.getSelectedIndex();
			imageRenderer.nscale = idx + 1;
			addOSDLine("Set scaling: Scale" + imageRenderer.nscale + "x");
			componentResized(null);
			configStateChanged = true;
		} else if (interpolationRadioGroup.contains(e.getSource())) {
			int idx = interpolationRadioGroup.getSelectedIndex();
			frame.setResizable(idx != 0);
			keepAspectRatio.setEnabled(idx != 0);
			imageRenderer.interpolation = idx;
			componentResized(null);
			configStateChanged = true;
		} else if (e.getSource().equals(keepAspectRatio)) {
			componentResized(null);
			configStateChanged = true;
		} else if (e.getSource().equals(enableFullScreen)) {
			componentResized(null);
			configStateChanged = true;
		} else if (e.getSource().equals(menuitemIncFrameSkip)) {
			++VC.fskip;
			addOSDLine("Increased frameskip: " + VC.fskip);
			configStateChanged = true;
		} else if (e.getSource().equals(menuitemDecFrameSkip)) {
			if (VC.fskip > 1)
				--VC.fskip;
			addOSDLine("Decreased frameskip: " + VC.fskip);
			configStateChanged = true;
		} else if (e.getSource().equals(menuitemEnableSound)) {
			cpu.audioController.isMuted = !menuitemEnableSound.getState();
			addOSDLine(cpu.audioController.isMuted ? "Disabled sound" : "Enabled sound");
			configStateChanged = true;
		} else if (soundChannelGroup.contains(e.getSource())) {
			int channel = soundChannelGroup.indexOf(e.getSource()) + 1;
			boolean enabled = cpu.audioController.toggleChannelOnOff(channel);
			addOSDLine((enabled ? "Enabled" : "Disabled") + " soundchannel " + channel);
			configStateChanged = true;
		} else if (e.getSource().equals(menuitemEnableCheats)) {
			cheatcodes.toggleCheats(cart);
			addOSDLine("Cheats codes are now " + (cheatcodes.useCheats ? "enabled" : "disabled"));
			configStateChanged = true;
		} else if (e.getSource().equals(menuitemConfigKeys)) {
			KeyConfigurationDialog k = new KeyConfigurationDialog(frame, keyStates);
			pauseEmulation(false);
			k.setVisible(true);
			saveKeyBinds();
			applyAccelerators();
			resumeEmulation(false);
		}

		else if (e.getSource().equals(menuitemCheats)) {
			pauseEmulation(false);
			cheatcodes.editCodes();
			cheatcodes.applyCheatCodes(cart);
			resumeEmulation(false);
			addOSDLine("ZOMG CHEATER!!!!");
		} else if (e.getSource().equals(menuitemUseBIOS)) {
			if (menuitemUseBIOS.getState())
				cart.loadBios(biosfilename, new BiosLoadingFunctionLocalImpl());
			else
				cart.loadBios("", new BiosLoadingFunctionLocalImpl());
			configStateChanged = true;
		} else if (e.getSource().equals(menuitemSetBIOS)) {
			JFileChooser fc = new JFileChooser(".");
			fc.showOpenDialog(frame);
			File selFile = fc.getSelectedFile();
			if (selFile != null) {
				if (selFile.length() != 256)
					JOptionPane.showMessageDialog(null, "This file's size is not equal to 256 bytes!", "Invalid BIOS Rom", JOptionPane.INFORMATION_MESSAGE);
				else {
					biosfilename = selFile.getAbsolutePath();
					if (menuitemUseBIOS.getState())
						cart.loadBios(biosfilename, new BiosLoadingFunctionLocalImpl());
					else
						cart.loadBios("", new BiosLoadingFunctionLocalImpl());
					configStateChanged = true;
				}
			}
		} else if (e.getSource().equals(menuitemOpenROM)) {
			JFileChooser fc;
			if (rcFiles.size() > 0) {
				String lastpath = (String) rcFiles.get(0);
				int slashPos = lastpath.lastIndexOf(File.separator);
				lastpath = lastpath.substring(0, slashPos + 1);
				fc = new JFileChooser(lastpath);
			} else
				fc = new JFileChooser(".");

			fc.showOpenDialog(frame);
			File selFile = fc.getSelectedFile();
			if (selFile != null) {
				tryToLoadROM(selFile.getAbsolutePath());
			}
		} else if (e.getSource().equals(menuitemLinkServe)) {
			try {
				server.serveLink(cpu);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(frame, "Failed: " + ioe.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource().equals(menuitemLinkSever)) {
			server.severLink(cpu);
		} else if (e.getSource().equals(menuitemLinkClient)) {
			try {
				server.clientLink(cpu, (String) JOptionPane.showInputDialog(frame, (Object) "Enter host address", "Link setup", JOptionPane.QUESTION_MESSAGE, null, null, "localhost"));
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(frame, "Failed: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource().equals(menuitemSaveState)) {
			if (!cpu.canRun())
				return;
			pauseEmulation(false);
			addOSDLine("Saving State " + selectedState);
			try {
				String stname = FHandler.JGBEDir("savestates");
				stname += curcartname + ".st" + selectedState;
				DataOutputStream dostream = FHandler.getDataOutputStream(stname);
				new CPUSaveState().saveState(dostream, cpu);
				dostream.close();
			} catch (java.io.IOException ioe) {
				System.out.println("Error saving state! ");
				System.out.println(ioe.getMessage());
			}
			;
			menuitemSpeedRunRecord.setState(cpu.keyHistoryEnabled);
			menuitemSpeedRunPlay.setState(cpu.playbackHistoryIndex != -1);
			resumeEmulation(false);
		} else if (e.getSource().equals(menuitemLoadState)) {
			if (!cpu.canRun())
				return;
			pauseEmulation(false);
			try {
				String stname = FHandler.JGBEDir("savestates") + curcartname + ".st" + selectedState;
				DataInputStream distream = FHandler.getDataInputStream(stname);
				new CPULoadState().loadState(distream, cpu);
				distream.close();
				addOSDLine("Loaded state " + selectedState);
			} catch (java.io.IOException ioe) {
				addOSDLine("Failed to load state " + selectedState);
			}
			;
			resumeEmulation(false);
			if (cpu.keyHistoryEnabled)
				addOSDLine("Note: Speedrun in progress");
			menuitemSpeedRunRecord.setState(cpu.keyHistoryEnabled);
			menuitemSpeedRunPlay.setState(cpu.playbackHistoryIndex != -1);
		} else if (e.getSource().equals(menuitemSaveOldestState)) {
			if (!cpu.canRun())
				return;
			pauseEmulation(false);
			selectedState = saveStateOrder.remove(0);
			saveStateOrder.add(selectedState);
			addOSDLine("Saving To Oldest State " + selectedState);
			try {
				String stname = FHandler.JGBEDir("savestates");
				stname += curcartname + ".st" + selectedState;
				DataOutputStream dostream = FHandler.getDataOutputStream(stname);
				new CPUSaveState().saveState(dostream, cpu);
				dostream.close();
			} catch (java.io.IOException ioe) {
				System.out.println("Error saving state! ");
				System.out.println(ioe.getMessage());
			}
			;
			menuitemSpeedRunRecord.setState(cpu.keyHistoryEnabled);
			menuitemSpeedRunPlay.setState(cpu.playbackHistoryIndex != -1);
			resumeEmulation(false);
		} else if (e.getSource().equals(menuitemPause)) {
			if (cpuRunner.isRunning())
				pauseEmulation(true);
			else
				resumeEmulation(true);
		} else if (e.getSource().equals(menuitemReset)) {
			pauseEmulation(false);
			cpu.reset(menuitemUseBIOS.getState());
			resumeEmulation(false);
			addOSDLine("Reset gameboy");
		} else if (e.getSource().equals(menuitemSpeedRunRecord)) {
			if (cpu.keyHistoryEnabled) {
				addOSDLine("Stopped recording of your speedrun");
				pauseEmulation(false);
				cpu.keyHistoryEnabled = false;
				resumeEmulation(false);
			} else {
				menuitemSpeedRunPlay.setState(false);
				addOSDLine("Now recording speedrun");
				pauseEmulation(false);
				cpu.reset(false);
				cpu.keyHistoryEnabled = true;
				resumeEmulation(false);
			}
			menuitemSpeedRunRecord.setState(cpu.keyHistoryEnabled);
			menuitemSpeedRunPlay.setState(cpu.playbackHistoryIndex != -1);
		} else if (e.getSource().equals(menuitemSpeedRunPlay) || e.getSource().equals(menuitemSpeedRunPlayWithOutput)) {
			menuitemSpeedRunRecord.setState(false);
			pauseEmulation(false);
			IntVector hist = cpu.keyHistory;
			cpu.keyHistory = new IntVector();
			cpu.reset(false);
			cpu.keyHistory = hist;
			cpu.playbackHistoryIndex = 0;
			cpu.keyHistoryEnabled = false;
			if (e.getSource().equals(menuitemSpeedRunPlayWithOutput)) {
				if (menuitemSpeedRunPlayWithOutput.getState()) {
					JFileChooser fcv = new JFileChooser(".");
					fcv.setDialogTitle("Save video dump");
					File vidFile;
					fcv.showSaveDialog(frame);
					vidFile = fcv.getSelectedFile();

					JFileChooser fca = new JFileChooser(".");
					fca.setDialogTitle("Save audio dump");
					File audFile;
					fca.showSaveDialog(frame);
					audFile = fca.getSelectedFile();

					if (vidFile != null && audFile != null) {
						try {
							speedRunPlayWithOutputVideoStream = FHandler.getDataOutputStream(vidFile.getAbsolutePath());
							DataOutputStream audstr = FHandler.getDataOutputStream(audFile.getAbsolutePath());

							int SampleRate = 44100;
							int Channels = 2;
							int BitsPerSample = 8;
							int ByteRate = SampleRate * Channels * BitsPerSample / 8;
							int BlockAlign = Channels * BitsPerSample / 8;
							int hdr[] = { 'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'A', 'V', 'E', 'f', 'm', 't', ' ', 16, 0, 0, 0, 1, 0, (Channels & 0xff), ((Channels >> 8) & 0xff), (SampleRate & 0xff), ((SampleRate >> 8) & 0xff), ((SampleRate >> 16) & 0xff), ((SampleRate >> 24) & 0xff), (ByteRate & 0xff), ((ByteRate >> 8) & 0xff), ((ByteRate >> 16) & 0xff), ((ByteRate >> 24) & 0xff), (BlockAlign & 0xff), ((BlockAlign >> 8) & 0xff), (BitsPerSample & 0xff), ((BitsPerSample >> 8) & 0xff), 'd', 'a', 't', 'a', 0, 0, 0, 0, 0, 0, 0, 0, };

							System.out.println(hdr.length);
							for (int i = 0; i < hdr.length; ++i)
								audstr.writeByte(hdr[i]);

							audioDriver.reset();
							audioDriver.setDumpStream(audstr);

						} catch (IOException ee) {
							speedRunPlayWithOutputVideoStream = null;
							JOptionPane.showMessageDialog(frame, "Error while opening file, recording will fail:\n" + ee.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
						}
					}
				} else {
					speedRunPlayWithOutputVideoStream = null;
					audioDriver.setDumpStream(null);
				}
			}
			resumeEmulation(false);
			menuitemSpeedRunPlay.setState(cpu.playbackHistoryIndex != -1);
		} else if (e.getSource().equals(menuitemEmuSpeedPlus)) {
			double cs = cpu.audioController.currentEmulationSpeed;
			double ns = 0.25 + cs;
			cpu.audioController.setSpeed(ns);
			addOSDLine("EmuSpeed: " + (int) (ns * 100.0) + "." + (int) ((ns * 100.0 - (int) (ns * 100.0))) + "%");
		} else if (e.getSource().equals(menuitemEmuSpeedMinus)) {
			double cs = cpu.audioController.currentEmulationSpeed;
			double ns = Math.max(cs - 0.25, 0);
			cpu.audioController.setSpeed(ns);
			addOSDLine("EmuSpeed: " + (int) (ns * 100.0) + "." + (int) ((ns * 100.0 - (int) (ns * 100.0))) + "%");
		} else if (e.getSource().equals(menuitemEmuSpeedMul)) {
			double cs = cpu.audioController.currentEmulationSpeed;
			double ns = 1.25 * cs;
			cpu.audioController.setSpeed(ns);
			addOSDLine("EmuSpeed: " + (int) (ns * 100.0) + "." + (int) ((ns * 100.0 - (int) (ns * 100.0))) + "%");
		} else if (e.getSource().equals(menuitemEmuSpeedDiv)) {
			double cs = cpu.audioController.currentEmulationSpeed;
			double ns = 0.75 * cs;
			cpu.audioController.setSpeed(ns);
			addOSDLine("EmuSpeed: " + (int) (ns * 100.0) + "." + (int) ((ns * 100.0 - (int) (ns * 100.0))) + "%");
		} else if (e.getSource().equals(menuitemEmuSpeedNormal)) {
			cpu.audioController.setSpeed(1.0);
			addOSDLine("EmuSpeed: 100%");
		} else if (e.getSource().equals(menuitemMixFrame)) {
			imageRenderer.mixFrames = !imageRenderer.mixFrames;
			addOSDLine((imageRenderer.mixFrames ? "Enabled" : "Disabled") + " Mix Frame mode");
			configStateChanged = true;
		}

		else if (e.getSource().equals(menuitemColorSchemes[0])) {
			int[][] c = { { 0xa0, 0xe0, 0x20 }, { 0x70, 0xb0, 0x40 }, { 0x40, 0x70, 0x32 }, { 0x10, 0x50, 0x26 } };
			cpu.videoController.setGrayShades(c);
			cs.setVisible(false);
			configStateChanged = true;
			for (int i = 0; i < menuitemColorSchemes.length; ++i)
				menuitemColorSchemes[i].setState(false);
			menuitemColorSchemes[0].setState(true);
		} else if (e.getSource().equals(menuitemColorSchemes[1])) {
			int[][] c = { { 0xC4, 0xCF, 0xA1 }, { 0x8B, 0x95, 0x6D }, { 0x6B, 0x73, 0x53 }, { 0x41, 0x41, 0x41 } };
			cpu.videoController.setGrayShades(c);
			cs.setVisible(false);
			configStateChanged = true;
			for (int i = 0; i < menuitemColorSchemes.length; ++i)
				menuitemColorSchemes[i].setState(false);
			menuitemColorSchemes[1].setState(true);
		} else if (e.getSource().equals(menuitemColorSchemes[2])) {
			int[][] c = { { 0xf8, 0xf8, 0xf8 }, { 0xa8, 0xa8, 0xa8 }, { 0x60, 0x60, 0x60 }, { 0x00, 0x00, 0x00 } };
			cpu.videoController.setGrayShades(c);
			cs.setVisible(false);
			configStateChanged = true;
			for (int i = 0; i < menuitemColorSchemes.length; ++i)
				menuitemColorSchemes[i].setState(false);
			menuitemColorSchemes[2].setState(true);
		} else if (e.getSource().equals(menuitemColorSchemes[3])) {
			int[][] c = { { 0xc5, 0xc5, 0xc5 }, { 0x9a, 0x9a, 0x9a }, { 0x61, 0x61, 0x61 }, { 0x3f, 0x3f, 0x3f } };
			cpu.videoController.setGrayShades(c);
			cs.setVisible(false);
			configStateChanged = true;
			for (int i = 0; i < menuitemColorSchemes.length; ++i)
				menuitemColorSchemes[i].setState(false);
			menuitemColorSchemes[3].setState(true);
		} else if (e.getSource().equals(menuitemColorSchemes[4])) {
			if (!cpu.isCGB()) {

				cs.setVisible(true);
				configStateChanged = true;
				for (int i = 0; i < menuitemColorSchemes.length; ++i)
					menuitemColorSchemes[i].setState(false);
				menuitemColorSchemes[4].setState(true);
			} else {
				JOptionPane.showMessageDialog(frame, "You are not allowed to change colors", "color error", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource().equals(menuitemToggleRemoteKeys)) {
			cpu.useRemoteKeys = !cpu.useRemoteKeys;
			addOSDLine("Remote Keys: " + (cpu.useRemoteKeys ? "Enabled" : "Disabled"));
		} else if (e.getSource().equals(menuitemShowAdvancedAudioPropertiesDialog)) {
			pauseEmulation(false);
			advancedAudioPropertiesDialog.showWindow();
			resumeEmulation(false);
		} else if (savestateRadioGroup.contains(e.getSource())) {
			int idx = savestateRadioGroup.getSelectedIndex();
			selectedState = idx;
			addOSDLine("Selected State " + selectedState);
		} else {
			if (menuItems != null)
				for (int i = 0; i < menuItems.length; ++i) {
					if (e.getSource().equals(menuItems[i]))
						tryToLoadROM((String) rcFiles.get(i));
				}

			if ((--titleUpdateCountDown) <= 0) {
				long ctime = System.nanoTime();
				long timeLapse = ctime - FPSTimeMillis;
				FPSTimeMillis = ctime;

				double afps = fps / (timeLapse / 1000000000.0);
				long ncycles = cpu.totalCycleCount - FPSCPUCycles;
				FPSCPUCycles = cpu.totalCycleCount;
				double emuspeed = ((int) ((((float) ncycles) / ((4194304 * timeLapse * (cpu.doublespeed ? 2 : 1)) / 1000000000.0f)) * 10000.0f)) / 100.0;
				String titlestr = (cart != null) ? " - " + cart.getTitle() : "";
				frame.setTitle(Format142.strformat("JGBE V" + Version.str + " - %05.02f fps / %06.02f%%" + titlestr, new double[] { (((int) (afps * 100)) / ((double) 100)), (double) emuspeed }));
				fps = 0;
				titleUpdateCountDown = 4;
			}

			if (deactcount > 0 && --deactcount == 0 && enableFullScreen.getState()) {
				--deactcount;
				enableFullScreen.setState(false);
				componentResized(null);
			}
			if ((osdTimer == 0) && (osdLines.size() > 0)) {
				osdLines.remove(0);
			} else
				--osdTimer;
		}
		if (configStateChanged)
			saveConfig();
	}

	long FPSTimeMillis = System.nanoTime();
	long FPSCPUCycles = cpu.totalCycleCount;
	long deactcount = 0;

	public void itemStateChanged(ItemEvent e) {
		throw new Error("Assertion failed: " + "false");
	}

	public void keyTyped(KeyEvent e) {
	}

	static final int[] keyMasks = { (1 << 2), (1 << 3), (1 << 1), (1 << 0), (1 << 4), (1 << 5), (1 << 7), (1 << 6), };

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int keyModifiers = e.getModifiers();
		int keyState = keyCode | (keyModifiers << 8);

		for (int i = 0; i < 8; ++i) {
			if ((keyStates[i] & 0xff) == keyCode) {
				cpu.releaseButton(keyMasks[i]);
				return;
			}
		}
		for (int i = 8; i < 16; ++i) {
			if (keyStates[i] == keyState) {
				cpu.releaseRemoteButton(keyMasks[i & 7]);
				return;
			}
		}
		if (keyCode == keyStates[39 + 16 - 1]) {
			cpu.audioController.speedHax = false;
		}
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int keyModifiers = e.getModifiers();
		int keyState = keyCode | (keyModifiers << 8);

		for (int i = 0; i < 8; ++i) {
			if (keyStates[i] == keyState) {
				cpu.pressButton(keyMasks[i]);
				return;
			}
		}
		for (int i = 8; i < 16; ++i) {
			if (keyStates[i] == keyState) {
				cpu.pressRemoteButton(keyMasks[i & 7]);
				return;
			}
		}
		if (keyCode == keyStates[39 + 16 - 1]) {
			cpu.audioController.speedHax = true;
		}
	}

	public void makeRcFilesMenuItems() {
		int staticMenuItems = 4;
		if (!(menuFile != null))
			throw new Error("Assertion failed: " + "menuFile != null");
		if (menuFile != null) {
			while (menuFile.getItemCount() > staticMenuItems) {
				menuFile.remove(staticMenuItems);
			}
			menuItems = new JMenuItem[rcFiles.size()];

			menuFile.addSeparator();
			for (int i = 0; i < rcFiles.size(); ++i) {
				String lname = (String) rcFiles.get(i);
				String name = lname.substring(lname.lastIndexOf(File.separator) + 1);
				menuItems[i] = new JMenuItem(name);
				menuFile.add(menuItems[i]);
				menuItems[i].addActionListener(this);
			}
		}
	}

	protected static String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf(".");
		return classString.substring(dotIndex + 1);
	}

	public void loadRCFiles() {
		try {
			String rcfilename = FHandler.JGBEDir("") + "rcfiles.log";
			BufferedReader in = new BufferedReader(new FileReader(rcfilename));
			String str;
			rcFiles.clear();
			while ((str = in.readLine()) != null) {
				rcFiles.add(str);
			}
			in.close();
		} catch (IOException e) {
			System.out.println("error reading rcfiles.log");
		}
	}

	public void saveRCFiles() {
		try {
			String rcfilename = FHandler.JGBEDir("") + "rcfiles.log";
			BufferedWriter out = new BufferedWriter(new FileWriter(rcfilename));
			String str;
			int num = ((10) > (rcFiles.size()) ? (rcFiles.size()) : (10));
			for (int i = 0; i < num; ++i) {
				str = (String) rcFiles.get(i);
				out.write(str, 0, str.length());
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.println("error writing rcfiles.log");
		}
		makeRcFilesMenuItems();
	}

	private boolean allowSaves = false;

	public void saveConfig() {
		if (!allowSaves)
			return;
		try {
			String filename = FHandler.JGBEDir("") + "jgbe.conf";
			DataOutputStream dostream = FHandler.getDataOutputStream(filename);
			int saveversion = (0);
			dostream.writeInt((0x53475549));
			dostream.writeInt((0));
			int compressionmethod = 1;
			DeflaterOutputStream zostream = null;
			dostream.writeInt(compressionmethod);
			switch (compressionmethod) {

			case 0:
				break;

			case 1: {
				zostream = new GZIPOutputStream(dostream);
				dostream = new DataOutputStream(zostream);
			}
				;
				break;
			default:
				if (!(false))
					throw new Error("Assertion failed: " + "false");
			}
			stateSaveLoad(true, saveversion, dostream, null);
			if (zostream != null) {
				dostream.flush();
				zostream.finish();
			}
			dostream.close();
		} catch (IOException e) {
			System.out.println("Warning: failed to save GUI config: " + e.toString());
		}
	}

	public void loadConfig() {
		setGUIDefaults();
		try {
			String filename = FHandler.JGBEDir("") + "jgbe.conf";
			DataInputStream distream = FHandler.getDataInputStream(filename);
			int loadversion;
			int magix = distream.readInt();
			if (magix == (0x53475549)) {
				loadversion = distream.readInt();
				if ((loadversion < (0)) || (loadversion > ((23))))
					return;
				if (loadversion != (0))
					System.out.println("loading config with old version:" + loadversion);

				int compressionmethod = distream.readInt();
				switch (compressionmethod) {

				case 0:
					break;

				case 1:
					distream = new DataInputStream(new GZIPInputStream(distream));
					break;
				default:
					System.out.println("GUI config: unknown compression method:" + compressionmethod);
				}
				stateSaveLoad(false, loadversion, null, distream);
			}
		} catch (IOException e) {
			System.out.println("Warning: failed to load GUI config: " + e.toString());
		}
		allowSaves = true;
	}

	void setGUIDefaults() {
		boolean runbiosonreset = false;
		boolean[] soundchannelactive = new boolean[4];
		for (int i = 0; i < 4; ++i)
			soundchannelactive[i] = true;
		boolean usecheats = true;
		int colorType = 0;
		biosfilename = "";
		imageRenderer.nscale = 2;
		int adChannels = 2;
		String adName = "None";
		String adVendor = "None";
		String adDescription = "None";
		String adVersion = "None";
		int adSampleRate = 44100;
		int adOutputInterval = 735;
		int adBufferSize = 2048;
		int interpolationRadioGroupsetSelectedIndex = 0;
		boolean keepAspectRatiosetEnabled = false;
		boolean keepAspectRatiosetState = true;
		boolean enableFullScreensetEnabled = true;
		boolean enableFullScreensetState = false;
		boolean menuitemMixFramesetState = false;
		Dimension fsize = frame.getSize();
		int fsizewidth = fsize.width;
		int fsizeheight = fsize.height;
		imageRenderer.setPreferredSize(new Dimension(ImageRenderer.SCREEN_WIDTH * imageRenderer.nscale, ImageRenderer.SCREEN_HEIGHT * imageRenderer.nscale));
		imageRenderer.setSize(new Dimension(ImageRenderer.SCREEN_WIDTH * imageRenderer.nscale, ImageRenderer.SCREEN_HEIGHT * imageRenderer.nscale));
		menuitemIgnoreSTAT.setState(cpu.videoController.allow_writes_in_mode_2_3);
		frame.pack();
		menuitemUseBIOS.setState(runbiosonreset);
		menuitemEnableSound.setState(!cpu.audioController.isMuted);
		for (int i = 0; i < 4; ++i) {
			cpu.audioController.setChannelActive(i + 1, soundchannelactive[i]);
			((JCheckBoxMenuItem) soundChannelGroup.get(i)).setState(soundchannelactive[i]);
		}
		actionPerformed(new ActionEvent(menuitemColorSchemes[colorType], 0, ""));
		scaleRadioGroup.setSelectedIndex(imageRenderer.nscale - 1);
		if (cheatcodes == null)
			cheatcodes = new CheatCodeEditor(frame, curcartname);
		cheatcodes.useCheats(usecheats);
		menuitemEnableCheats.setState(usecheats);

		if (audioDriver != null) {
			audioDriver.setMixerInfo(adName, adVendor, adDescription, adVersion);
			audioDriver.setChannels(adChannels);
			audioDriver.setBufferSize(adBufferSize);
			audioDriver.setOutputInterval(adOutputInterval);
			audioDriver.setSampleRate(adSampleRate);
		}

		interpolationRadioGroup.setSelectedIndex(interpolationRadioGroupsetSelectedIndex);
		keepAspectRatio.setEnabled(keepAspectRatiosetEnabled);
		keepAspectRatio.setState(keepAspectRatiosetState);
		enableFullScreen.setEnabled(enableFullScreensetEnabled);
		enableFullScreen.setState(enableFullScreensetState);
		menuitemMixFrame.setState(menuitemMixFramesetState);
		imageRenderer.mixFrames = menuitemMixFramesetState;
		fsize = new Dimension(fsizewidth, fsizeheight);
		frame.setPreferredSize(fsize);
		frame.pack();
		frame.setPreferredSize(new Dimension(0, 0));
		actionPerformed(new ActionEvent(interpolationRadioGroup.get(interpolationRadioGroupsetSelectedIndex), 0, ""));
	}

	protected void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream) throws IOException {
		{
			if ((save))
				dostream.writeBoolean(cpu.audioController.isMuted);
			else
				cpu.audioController.isMuted = distream.readBoolean();
		}
		;
		{
			if ((save))
				dostream.writeUTF(biosfilename);
			else
				biosfilename = distream.readUTF();
		}
		;
		{
			if ((save)) {
				boolean sl_v = menuitemUseBIOS.getState();
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
			} else {
				boolean sl_v = false;
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
				menuitemUseBIOS.setState(sl_v);
			}
		}
		;
		{
			if ((save))
				dostream.writeInt((int) imageRenderer.nscale);
			else
				imageRenderer.nscale = distream.readInt();
		}
		;
		{
			if ((save)) {
				int sl_v = scaleRadioGroup.getSelectedIndex();
				{
					if ((save))
						dostream.writeInt((int) sl_v);
					else
						sl_v = distream.readInt();
				}
				;
			} else {
				int sl_v = 0;
				{
					if ((save))
						dostream.writeInt((int) sl_v);
					else
						sl_v = distream.readInt();
				}
				;
				scaleRadioGroup.setSelectedIndex(sl_v);
			}
		}
		;
		{
			if ((save))
				dostream.writeInt((int) VC.fskip);
			else
				VC.fskip = distream.readInt();
		}
		;

		{
			if ((save)) {
				int sl_v = interpolationRadioGroup.getSelectedIndex();
				{
					if ((save))
						dostream.writeInt((int) sl_v);
					else
						sl_v = distream.readInt();
				}
				;
			} else {
				int sl_v = 0;
				{
					if ((save))
						dostream.writeInt((int) sl_v);
					else
						sl_v = distream.readInt();
				}
				;
				interpolationRadioGroup.setSelectedIndex(sl_v);
			}
		}
		;
		if ((!save))
			actionPerformed(new ActionEvent(interpolationRadioGroup.get(interpolationRadioGroup.getSelectedIndex()), 0, ""));
		{
			if ((save)) {
				boolean sl_v = keepAspectRatio.isEnabled();
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
			} else {
				boolean sl_v = false;
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
				keepAspectRatio.setEnabled(sl_v);
			}
		}
		;
		{
			if ((save)) {
				boolean sl_v = keepAspectRatio.getState();
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
			} else {
				boolean sl_v = false;
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
				keepAspectRatio.setState(sl_v);
			}
		}
		;
		{
			if ((save)) {
				boolean sl_v = enableFullScreen.isEnabled();
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
			} else {
				boolean sl_v = false;
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
				enableFullScreen.setEnabled(sl_v);
			}
		}
		;
		{
			if ((save)) {
				boolean sl_v = enableFullScreen.getState();
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
			} else {
				boolean sl_v = false;
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
				enableFullScreen.setState(sl_v);
			}
		}
		;
		{
			if ((save)) {
				boolean sl_v = menuitemMixFrame.getState();
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
			} else {
				boolean sl_v = false;
				{
					if ((save))
						dostream.writeBoolean(sl_v);
					else
						sl_v = distream.readBoolean();
				}
				;
				menuitemMixFrame.setState(sl_v);
			}
		}
		;
		imageRenderer.mixFrames = menuitemMixFrame.getState();

		{
			int[][][] gs = VC.getGrayShades();
			{
				for (int sl_i = 0; sl_i < (3); ++sl_i)
					for (int sl_j = 0; sl_j < (4); ++sl_j)
						for (int sl_k = 0; sl_k < (3); ++sl_k) {
							if ((save))
								dostream.writeInt((int) gs[sl_i][sl_j][sl_k]);
							else
								gs[sl_i][sl_j][sl_k] = distream.readInt();
						}
				;
			}
			;
			if ((!save))
				VC.setGrayShades(gs);
			for (int i = 0; i < menuitemColorSchemes.length; ++i) {
				{
					if ((save)) {
						boolean sl_v = menuitemColorSchemes[i].getState();
						{
							if ((save))
								dostream.writeBoolean(sl_v);
							else
								sl_v = distream.readBoolean();
						}
						;
					} else {
						boolean sl_v = false;
						{
							if ((save))
								dostream.writeBoolean(sl_v);
							else
								sl_v = distream.readBoolean();
						}
						;
						menuitemColorSchemes[i].setState(sl_v);
					}
				}
				;
				if ((!save) && menuitemColorSchemes[i].getState())
					actionPerformed(new ActionEvent(menuitemColorSchemes[i], 0, ""));
			}

			{
				if ((save)) {
					boolean sl_v = menuitemIgnoreSTAT.getState();
					{
						if ((save))
							dostream.writeBoolean(sl_v);
						else
							sl_v = distream.readBoolean();
					}
					;
				} else {
					boolean sl_v = false;
					{
						if ((save))
							dostream.writeBoolean(sl_v);
						else
							sl_v = distream.readBoolean();
					}
					;
					menuitemIgnoreSTAT.setState(sl_v);
				}
			}
			;
			VC.allow_writes_in_mode_2_3 = menuitemIgnoreSTAT.getState();
		}

		{
			{
				if ((save))
					dostream.writeBoolean(cheatcodes.useCheats);
				else
					cheatcodes.useCheats = distream.readBoolean();
			}
			;
			menuitemEnableCheats.setState(cheatcodes.useCheats);
		}

		{
			boolean[] soundchannelactive = new boolean[4];
			if ((save))
				for (int i = 0; i < 4; ++i)
					soundchannelactive[i] = cpu.audioController.channelActive(i + 1);
			{
				for (int sl_i = 0; sl_i < (4); ++sl_i) {
					if ((save))
						dostream.writeBoolean(soundchannelactive[sl_i]);
					else
						soundchannelactive[sl_i] = distream.readBoolean();
				}
				;
			}
			menuitemEnableSound.setState(!cpu.audioController.isMuted);
			if ((!save))
				for (int i = 0; i < 4; ++i) {
					cpu.audioController.setChannelActive(i + 1, soundchannelactive[i]);
					((JCheckBoxMenuItem) soundChannelGroup.get(i)).setState(soundchannelactive[i]);
				}
		}

		{
			String adName = "None", adVendor = "None", adDescription = "None", adVersion = "None";
			int adChannels = 2, adSampleRate = 44100, adOutputInterval = 735, adBufferSize = 2048;
			if ((save) && audioDriver != null) {
				adName = audioDriver.getMixerInfo().getName();
				adVendor = audioDriver.getMixerInfo().getVendor();
				adDescription = audioDriver.getMixerInfo().getDescription();
				adVersion = audioDriver.getMixerInfo().getVersion();
				adChannels = audioDriver.getChannels();
				adBufferSize = audioDriver.getBufferSize();
				adOutputInterval = audioDriver.getOutputInterval();
				adSampleRate = audioDriver.getSampleRate();
			}

			{
				if ((save))
					dostream.writeUTF(adName);
				else
					adName = distream.readUTF();
			}
			;
			{
				if ((save))
					dostream.writeUTF(adVendor);
				else
					adVendor = distream.readUTF();
			}
			;
			{
				if ((save))
					dostream.writeUTF(adDescription);
				else
					adDescription = distream.readUTF();
			}
			;
			{
				if ((save))
					dostream.writeUTF(adVersion);
				else
					adVersion = distream.readUTF();
			}
			;
			{
				if ((save))
					dostream.writeInt((int) adChannels);
				else
					adChannels = distream.readInt();
			}
			;
			{
				if ((save))
					dostream.writeInt((int) adBufferSize);
				else
					adBufferSize = distream.readInt();
			}
			;
			{
				if ((save))
					dostream.writeInt((int) adOutputInterval);
				else
					adOutputInterval = distream.readInt();
			}
			;
			{
				if ((save))
					dostream.writeInt((int) adSampleRate);
				else
					adSampleRate = distream.readInt();
			}
			;
			if ((!save) && audioDriver != null) {
				audioDriver.setMixerInfo(adName, adVendor, adDescription, adVersion);
				audioDriver.setChannels(adChannels);
				audioDriver.setBufferSize(adBufferSize);
				audioDriver.setOutputInterval(adOutputInterval);
				audioDriver.setSampleRate(adSampleRate);
			}
		}

		{
			int fsizewidth = 0;
			int fsizeheight = 0;
			if ((save)) {
				Dimension fsize = frame.getSize();
				fsizewidth = fsize.width;
				fsizeheight = fsize.height;
			}
			{
				if ((save))
					dostream.writeInt((int) fsizewidth);
				else
					fsizewidth = distream.readInt();
			}
			;
			{
				if ((save))
					dostream.writeInt((int) fsizeheight);
				else
					fsizeheight = distream.readInt();
			}
			;
			if ((!save)) {
				Dimension fsize = new Dimension(fsizewidth, fsizeheight);
				frame.setPreferredSize(fsize);
				frame.pack();
				frame.setPreferredSize(new Dimension(0, 0));
			}
		}
	}

	private void updateSaveStateOrder() {
		saveStateOrder.clear();
		long[] modTimes = new long[10];
		try {
			for (int i = 0; i < 10; ++i) {
				String stname = FHandler.JGBEDir("savestates");
				stname += curcartname + ".st" + i;
				File sf = new File(stname);
				long modTime = sf.lastModified();

				if (!(modTime >= 0))
					throw new Error("Assertion failed: " + "modTime >= 0");
				modTime = modTime & 0xFFFFFFF0;

				modTimes[i] = modTime + i;
			}
		} catch (Exception e) {
			saveStateOrder = null;
		}
		Arrays.sort(modTimes);
		for (int i = 0; i < 10; ++i) {
			saveStateOrder.add((int) (modTimes[i] & 0xF));

		}
	}

	private void tryToLoadROM(final String filename) {
		logo = null;
		pauseEmulation(false);

		UGABEServiceAsync service = new UGABEServiceAsync() {
			public void loadCartridge(String fileName, AsyncCallback<List<Integer>> callback) {
				UGABEService service = new UGABEServiceController();
				try {
					callback.onSuccess(service.loadCartridge(fileName));
				} catch (Exception e) {
					callback.onFailure(e);
				}
			}
		};
		CartridgeController cartridgeController = new CartridgeController(service);
		cartridgeController.createCartridge(filename, new CartridgeCreateHandler() {
			public void onCreateCartridge(Cartridge tcart) {
				if (menuitemUseBIOS.getState())
					tcart.loadBios(biosfilename, new BiosLoadingFunctionLocalImpl());
				else
					tcart.loadBios("", new BiosLoadingFunctionLocalImpl());
				String[] messages = { "[Missing an error message here]" };
				switch (tcart.getStatus(messages)) {
				case Cartridge.STATUS_NONFATAL_ERROR: {
					JOptionPane.showMessageDialog(frame, "WARNING:\n" + messages[0], "Warning!", JOptionPane.WARNING_MESSAGE);
				}
				case Cartridge.STATUS_OK: {
					cart = tcart;
					cpu.loadCartridge(cart);
					updateCartName(filename);
					boolean b = (cheatcodes == null) ? false : cheatcodes.useCheats;
					cheatcodes = new CheatCodeEditor(frame, curcartname);
					cheatcodes.useCheats(b);
					cheatcodes.applyCheatCodes(cart);
					updateSaveStateOrder();
					addOSDLine("loaded Rom: " + curcartname);
				}
					break;
				default: {
					JOptionPane.showMessageDialog(frame, "There was an error loading this ROM!\n(" + messages[0] + ")", "Error!", JOptionPane.ERROR_MESSAGE);
				}
					break;
				}
				resumeEmulation(false);
			}
		});

	}

	private void saveKeyBinds() {
		DataOutputStream distream = null;
		try {
			String path = FHandler.JGBEDir("") + "keys.conf";
			FileOutputStream fistream = new FileOutputStream(path);
			distream = new DataOutputStream(fistream);
			for (int i = 0; i < 16 + 39; ++i) {
				distream.writeInt(keyStates[i]);

			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "Could not save keybinds!", "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				distream.close();
			} catch (Exception e) {
			}
		}
	}

	private void loadKeyBinds() {
		DataInputStream distream = null;
		try {
			String path = FHandler.JGBEDir("") + "keys.conf";
			FileInputStream fistream = new FileInputStream(path);
			distream = new DataInputStream(fistream);
			for (int i = 0; i < 16 + 39; ++i) {
				keyStates[i] = distream.readInt();
			}
		} catch (IOException e) {
			pauseEmulation(false);
			JOptionPane.showMessageDialog(frame, "Keys need to be configured", "Warning", JOptionPane.WARNING_MESSAGE);
			generateDefaultKeyBinds();
			KeyConfigurationDialog k = new KeyConfigurationDialog(frame, keyStates);
			k.setVisible(true);
			saveKeyBinds();
			resumeEmulation(false);
		} finally {
			try {
				applyAccelerators();
				distream.close();
			} catch (Exception e) {
			}
		}
	}

	private void generateDefaultKeyBinds() {
		int Magix = 0;
		keyStates[Magix++] = 38;
		keyStates[Magix++] = 40;
		keyStates[Magix++] = 37;
		keyStates[Magix++] = 39;
		keyStates[Magix++] = 90;
		keyStates[Magix++] = 88;
		keyStates[Magix++] = 61;
		keyStates[Magix++] = 45;
		keyStates[Magix++] = 73;
		keyStates[Magix++] = 75;
		keyStates[Magix++] = 74;
		keyStates[Magix++] = 76;
		keyStates[Magix++] = 91;
		keyStates[Magix++] = 93;
		keyStates[Magix++] = 222;
		keyStates[Magix++] = 59;
		keyStates[Magix++] = 556;
		keyStates[Magix++] = 591;
		keyStates[Magix++] = 19;
		keyStates[Magix++] = 594;
		keyStates[Magix++] = 593;
		keyStates[Magix++] = 2058;
		keyStates[Magix++] = 589;
		keyStates[Magix++] = 561;
		keyStates[Magix++] = 562;
		keyStates[Magix++] = 563;
		keyStates[Magix++] = 564;
		keyStates[Magix++] = 2625;
		keyStates[Magix++] = 305;
		keyStates[Magix++] = 306;
		keyStates[Magix++] = 307;
		keyStates[Magix++] = 308;
		keyStates[Magix++] = 605;
		keyStates[Magix++] = 603;
		keyStates[Magix++] = 560;
		keyStates[Magix++] = 569;
		keyStates[Magix++] = 568;
		keyStates[Magix++] = 567;
		keyStates[Magix++] = 566;
		keyStates[Magix++] = 595;
		keyStates[Magix++] = 588;
		keyStates[Magix++] = 851;
		keyStates[Magix++] = 577;
		keyStates[Magix++] = 2097;
		keyStates[Magix++] = 2098;
		keyStates[Magix++] = 2099;
		keyStates[Magix++] = 2100;
		keyStates[Magix++] = 704;
		keyStates[Magix++] = 604;
		keyStates[Magix++] = 829;
		keyStates[Magix++] = 557;
		keyStates[Magix++] = 861;
		keyStates[Magix++] = 859;
		keyStates[Magix++] = 573;
		keyStates[Magix++] = 32;
	}

	static Cartridge cart = null;

	public void init() {
		JLabel label = new JLabel("The emulator will be opened in a new window.");
		getContentPane().add(label);
	}

	boolean debug = false;
	Image logo = null;

	public void starter(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionCatcher());

		boolean sound = true, debug = false, lastcart = false;
		String romfile = "", logfile = "", remotedebugoffset = "0";
		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) != '-')
				romfile = args[i];
			if (args[i].equals("-log"))
				logfile = args[++i];
			if (args[i].equals("-nosound"))
				sound = false;
			if (args[i].equals("-sound"))
				sound = true;
			if (args[i].equals("-debug"))
				debug = true;
			if (args[i].equals("-rdo"))
				remotedebugoffset = args[++i];
			if ((args[i].equals("-lastcart")))
				lastcart = true;
		}

		this.debug = debug;

		if (!lastcart) {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			URL imgurl = this.getClass().getClassLoader().getResource("jgbe_logo.png");
			this.logo = null;
			if (imgurl != null) {
				logo = toolkit.getImage(imgurl);
				while ((this.logo.getWidth(null) == -1) || (this.logo.getHeight(null) == -1)) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}
				;
			} else {
				System.out.println("Can't load JGBE logo!");
			}
		}

		this.loadRCFiles();
		if (lastcart && (this.rcFiles.size() > 0))
			romfile = (String) this.rcFiles.get(0);

		this.VC = this.cpu.videoController;
		this.cs = new ColorSelector(this);
		this.createGUI();

		if (sound) {
			this.audioDriver = new AudioDriver(this.cpu.audioController);
			cpu.audioController.addIAudioListener(this.audioDriver);
			advancedAudioPropertiesDialog = new AdvancedAudioPropertiesDialog(frame, this.audioDriver, this);
		}

		if (!romfile.equals(""))
			this.tryToLoadROM(romfile);

		cheatcodes = new CheatCodeEditor(frame, curcartname);

		cheatcodes.applyCheatCodes(cart);

		this.loadConfig();

		if (this.menuitemUseBIOS.getState() && cart != null)
			cart.loadBios(biosfilename, new BiosLoadingFunctionLocalImpl());
		this.loadKeyBinds();

		if (!sound)
			this.cpu.audioController.isMuted = true;

		Timer timer = new Timer(250, this);
		timer.setInitialDelay(250);
		timer.start();

		if (cart != null)
			this.cpu.loadCartridge(cart);

		this.makeRcFilesMenuItems();

		if (debug) {
			Debugger dbgr = new Debugger(this, logfile);
			System.out.println("Parsing long" + remotedebugoffset);
			long l = Long.parseLong(remotedebugoffset);
			System.out.println("Setting");
			dbgr.setRemoteDebugOffset(l);
			System.out.println("Running");
			this.cpuRunner = dbgr.runner;
		} else {
			this.cpuRunner = new SimpleCPURunner();
			this.resumeEmulation(false);
		}
		this.showGUI();
		;
	}

	public void start() {
		try {
			isApplet = true;
			String[] args = getParameter("params").split(" ");
			starter(args);
		} catch (Exception e) {

		}
	}

	public static void main(String[] args) {
		final GUI gui = new GUI();
		gui.starter(args);
	}

	public void windowActivated(WindowEvent e) {
		if (deactcount == -1) {
			enableFullScreen.setState(true);
			componentResized(null);
		}

		deactcount = 0;
	}

	public void windowClosing(WindowEvent e) {
		windowClosed(e);
	}

	public void windowDeactivated(WindowEvent e) {
		deactcount = 2;

	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
		if (isApplet) {
			try {
				getAppletContext().showDocument(new URL("http://code.google.com/p/jgbe/"));
			} catch (MalformedURLException ex) {

			}
		} else {
			System.exit(0);
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (mousehidden == 1) {
			mousehidden = 2;
			return;
		}
		if (mousehidden == 2) {
			addOSDLine("Unhiding mouse");
			try {
				new Robot().mouseMove(lastmousex, lastmousey);
			} catch (AWTException ex) {
			}
			mousehidden = 0;
			return;
		}
		lastmousecnt = 60 * 3;

		lastmousex = e.getX();
		lastmousey = e.getY();
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
}
