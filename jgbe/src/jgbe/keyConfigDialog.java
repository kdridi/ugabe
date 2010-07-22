package jgbe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

class keyConfigDialog implements ComponentListener, ActionListener, KeyListener {
	private JDialog dialog;
	private JFrame owner;
	ArrayList<JTextField> editboxes = new ArrayList<JTextField>();
	JTextField tbox;
	JLabel tlabel;
	int nInputBoxen = -16;

	int[] keyMap;
	boolean[] keysup = new boolean[256];
	boolean canChooseNextKey = true;

	public keyConfigDialog(JFrame o, int[] km) {
		owner = o;
		keyMap = km;
		JScrollPane scroll;
		dialog = new JDialog(owner, "Key Bindings", true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				dialog.setVisible(false);
			}
		});
		JPanel ppp;
		JTabbedPane tabPane = new JTabbedPane();
		JPanel player1Keys = new JPanel();
		GridLayout player1KeysGL = new GridLayout(8, 2);
		player1Keys.setLayout(player1KeysGL);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Up");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Down");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Left");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Right");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("A");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("B");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Start");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Select");
		editboxes.add(tbox);
		player1Keys.add(tlabel);
		player1Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		ppp = new JPanel();
		ppp.setLayout(new BorderLayout());
		ppp.add(player1Keys, BorderLayout.NORTH);
		ppp.add(new JPanel(), BorderLayout.SOUTH);
		tabPane.add("Player 1", ppp);

		JPanel player2Keys = new JPanel();
		GridLayout player2KeysGL = new GridLayout(8, 2);
		player2Keys.setLayout(player2KeysGL);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Up");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Down");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Left");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Right");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("A");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("B");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Start");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Select");
		editboxes.add(tbox);
		player2Keys.add(tlabel);
		player2Keys.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		ppp = new JPanel();
		ppp.setLayout(new BorderLayout());
		ppp.add(player2Keys, BorderLayout.NORTH);
		ppp.add(new JPanel(), BorderLayout.SOUTH);
		tabPane.add("Player 2", ppp);

		JPanel shortCuts = new JPanel();
		GridLayout shortCutsGL = new GridLayout(39, 2);
		shortCuts.setLayout(shortCutsGL);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Configure Keybinds");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Open ROM");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Pause");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Reset Gameboy");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Exit Emulator");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Fullscreen");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Mix Frame");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Scale 1x");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Scale 2x");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Scale 3x");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Scale 4x");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Scale with Aspect");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Interpolate None");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Interpolate Nearest");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Interpolate BiLinear");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Interpolate BiCubic");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Increase FrameSkip");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Decrease FrameSkip");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Original Colors");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("GB Pocket/Light Colors");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Black and White Colors");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Greyscale Colors");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Custom Colors");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Save state");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Load state");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Save To Oldest State");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Toggle Sound on/off");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Toggle Soundchannel 1 on/off");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Toggle Soundchannel 2 on/off");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Toggle Soundchannel 3 on/off");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Toggle Soundchannel 4 on/off");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Open Cheat Code Editor");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Toggle Cheats on/off");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Emulation speed +25%");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Emulation speed -25%");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Emulation speed *125%");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Emulation speed *75%");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Emulation speed 100%");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		if (!(nInputBoxen++ < 39))
			throw new Error("Assertion failed: " + "nInputBoxen++ < nrOfShortCuts");
		tbox = new JTextField();
		tlabel = new JLabel("Emulation speed Infinite%");
		editboxes.add(tbox);
		shortCuts.add(tlabel);
		shortCuts.add(tbox);
		tbox.addKeyListener(this);
		tbox.addActionListener(this);
		scroll = new JScrollPane(shortCuts);
		scroll.setPreferredSize(new Dimension(10, 10));
		tabPane.add("Shortcuts", scroll);

		scroll = new JScrollPane(tabPane);
		scroll.setPreferredSize(new Dimension(480, 320));
		dialog.add(scroll);

		int j = 0;
		for (JTextField tf : editboxes) {
			int keyCode = keyMap[j] & 0xff;
			int keyModifiers = keyMap[j] >> 8;
			tf.setText((keyModifiers > 0 ? KeyEvent.getKeyModifiersText(keyModifiers) + "+" : "") + KeyEvent.getKeyText(keyCode));
			++j;
		}

		for (int i = 0; i < 256; ++i) {
			keysup[i] = true;
		}

		dialog.addComponentListener(this);
		dialog.addKeyListener(this);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(true);
		dialog.pack();
		Dimension d = owner.getSize();
		Point p = new Point();
		p.setLocation((owner.getLocation().getX() + (d.getWidth() / 2)) - (dialog.getWidth() / 2), (owner.getLocation().getY() + d.getHeight() / 2) - (dialog.getHeight() / 2));
		dialog.setLocation(p);

		dialog.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				((JTextField) editboxes.get(0)).requestFocus();
			}
		});
	}

	public int[] getKeyCodes() {
		dialog.setVisible(true);
		return null;
	}

	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int keyModifiers = e.getModifiers();
		int keyState = keyCode | (keyModifiers << 8);
		int j = 0;

		if (canChooseNextKey) {
			for (JTextField tf : editboxes) {
				if (tf.hasFocus()) {
					tf.setText((keyModifiers > 0 ? KeyEvent.getKeyModifiersText(keyModifiers) + "+" : "") + KeyEvent.getKeyText(keyCode));
					((JTextField) editboxes.get(Math.min(j + 1, editboxes.size() - 1))).requestFocus();
					keyMap[j] = keyState;
				}
				++j;
			}
		}
		keysup[keyCode] = true;
		canChooseNextKey = true;
		for (int i = 0; i < 256; ++i) {
			canChooseNextKey = keysup[i] && canChooseNextKey;
		}
	}

	public void keyPressed(KeyEvent e) {
		keysup[e.getKeyCode()] = false;
	}

	public void keyTyped(KeyEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
	}
}