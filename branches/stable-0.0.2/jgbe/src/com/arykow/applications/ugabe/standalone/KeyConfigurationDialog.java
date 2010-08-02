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

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

class KeyConfigurationDialog extends JDialog {
	private static final long serialVersionUID = -8382206565267992307L;

	private static final List<String> PLAYER_KEYS = Arrays.asList("Up", "Down", "Left", "Right", "A", "B", "Start", "Select");
	private static final List<String> SHORTCUT_KEYS = Arrays.asList("Configure Keybinds", "Open ROM", "Pause", "Reset Gameboy", "Exit Emulator", "Fullscreen", "Mix Frame", "Scale 1x", "Scale 2x", "Scale 3x", "Scale 4x", "Scale with Aspect", "Interpolate None", "Interpolate Nearest", "Interpolate BiLinear", "Interpolate BiCubic", "Increase FrameSkip", "Decrease FrameSkip", "Original Colors", "GB Pocket/Light Colors", "Black and White Colors", "Greyscale Colors", "Custom Colors", "Save state", "Load state", "Save To Oldest State", "Toggle Sound on/off", "Toggle Soundchannel 1 on/off", "Toggle Soundchannel 2 on/off", "Toggle Soundchannel 3 on/off", "Toggle Soundchannel 4 on/off", "Open Cheat Code Editor", "Toggle Cheats on/off", "Emulation speed +25%", "Emulation speed -25%", "Emulation speed *125%", "Emulation speed *75%", "Emulation speed 100%", "Emulation speed Infinite%");
	public static final int KEY_STATES_LENGTH = PLAYER_KEYS.size() * 2 + SHORTCUT_KEYS.size();

	private static class KeyTextFactory implements ActionListener, KeyListener {
		private static final long serialVersionUID = -2671050133411183856L;

		private final List<KeyTextField> fields = new ArrayList<KeyTextField>();
		private boolean[] keysUp = new boolean[256];
		private int[] keyStates;
		private boolean canChooseNextKey = true;

		public void setKeyStates(int[] keyStates) {
			this.keyStates = keyStates;
			updateKeyStates();
		}

		private void updateKeyStates() {
			for (int index = 0; index < keyStates.length; index++) {
				int keyCode = keyStates[index] & 0xff;
				int keyModifiers = keyStates[index] >> 8;
				fields.get(index).updateText(keyCode, keyModifiers);
			}
			for (int i = 0; i < 256; ++i) {
				keysUp[i] = true;
			}
		}

		public void setKeyState(int index, int keyState) {
			keyStates[index] = keyState;
		}

		public void setKeyUp(int index, boolean keyState) {
			keysUp[index] = keyState;
		}

		public boolean getKeyUpStatus(int index) {
			return keysUp[index];
		}

		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub

		}

		public void keyReleased(KeyEvent event) {
			int keyCode = event.getKeyCode();
			int keyModifiers = event.getModifiers();
			int keyState = keyCode | (keyModifiers << 8);

			if (canChooseNextKey) {
				int index = 0;
				for (KeyTextField textField : fields) {
					if (textField.hasFocus()) {
						textField.updateText(keyCode, keyModifiers);
						fields.get(Math.min(index + 1, fields.size() - 1)).requestFocus();
						setKeyState(index, keyState);
					}
					++index;
				}
			}
			setKeyUp(keyCode, true);
			canChooseNextKey = true;
			for (int index = 0; canChooseNextKey && index < 256; ++index) {
				canChooseNextKey = getKeyUpStatus(index) && canChooseNextKey;
			}
		}

		public void keyPressed(KeyEvent event) {
			setKeyUp(event.getKeyCode(), false);
		}

		public void keyTyped(KeyEvent e) {
		}

		public KeyTextField createKeyTextField() {
			KeyTextField result = new KeyTextField();
			result.addKeyListener(this);
			result.addActionListener(this);
			fields.add(result);
			return result;
		}

		private static class KeyTextField extends JTextField {
			private static final long serialVersionUID = 1626717517814320866L;

			public void updateText(int keyCode, int keyModifiers) {
				setText((keyModifiers > 0 ? KeyEvent.getKeyModifiersText(keyModifiers) + "+" : "") + KeyEvent.getKeyText(keyCode));
			}

		}
	}

	private KeyTextFactory keyTextFactory = new KeyTextFactory();

	public KeyConfigurationDialog(final JFrame frame, int[] keyStates) {
		super(frame, "Key Bindings", true);
		initializeWidget(createLocationPoint(frame));
		keyTextFactory.setKeyStates(keyStates);
	}

	private Point createLocationPoint(JFrame owner) {
		Point result = new Point();
		result.setLocation((owner.getLocation().getX() + (owner.getSize().getWidth() / 2)) - (getWidth() / 2), (owner.getLocation().getY() + owner.getSize().getHeight() / 2) - (getHeight() / 2));
		return result;
	}

	private void initializeWidget(Point point) {
		JScrollPane scrollPanel = new JScrollPane(createTabbedPane());
		scrollPanel.setPreferredSize(new Dimension(480, 320));
		add(scrollPanel);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		});
		setLocationRelativeTo(null);
		setResizable(true);
		pack();
		// addWindowListener(new WindowAdapter() {
		// public void windowOpened(WindowEvent e) {
		// (JTextField.class.cast(textFields.get(0))).requestFocus();
		// }
		// });
		addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentHidden(ComponentEvent e) {
			}
		});
		setLocation(point);
	}

	class PlayerPanel extends JPanel {
		private static final long serialVersionUID = -4690985101922756263L;

		public PlayerPanel(int size) {
			super(new GridLayout(size, 2));
		}

		public PlayerPanel(List<String> labels) {
			this(labels.size());
			for (String label : labels) {
				add(new JLabel(label));
				add(keyTextFactory.createKeyTextField());
			}
		}
	}

	private JTabbedPane createTabbedPane() {
		return new JTabbedPane() {
			private static final long serialVersionUID = 9170258848982894211L;
			{
				add("Player 1", PLAYER_KEYS, false);
				add("Player 2", PLAYER_KEYS, false);
				add("Shortcuts", SHORTCUT_KEYS, true);
			}

			private void add(String label, final List<String> values, boolean scrolled) {
				Component component = scrolled ? new JScrollPane(new PlayerPanel(values)) {
					private static final long serialVersionUID = 2192992776982137248L;
					{
						setPreferredSize(new Dimension(10, 10));
					}
				} : new JPanel() {
					private static final long serialVersionUID = -3520930470928571857L;
					{
						setLayout(new BorderLayout());
						add(new PlayerPanel(values), BorderLayout.NORTH);
						add(new JPanel(), BorderLayout.SOUTH);
					}
				};
				add(label, component);
			}
		};
	}

}