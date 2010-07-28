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
package com.arykow.applications.ugabe.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.arykow.applications.ugabe.client.VideoController;

public class ColorSelector extends JFrame implements MouseListener, ChangeListener, ActionListener {
	public static final long serialVersionUID = 1;

	private VideoController vc;
	private swinggui gui;

	JColorChooser colorchooser;
	JPanel colorabletypes;
	JPanel colorsBackground;
	JPanel colorsSprites1;
	JPanel colorsSprites2;

	JPanel[] colorPanes;
	int currentColorPane = 0;

	public void updateColorPanes() {
		for (int i = 0; i < 12; ++i) {
			colorPanes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		colorPanes[currentColorPane].setBorder(BorderFactory.createLineBorder(Color.RED, 3));
	}

	public ColorSelector(swinggui gui) {
		this.vc = gui.VC;
		this.gui = gui;

		setLayout(new FlowLayout());
		setResizable(false);
		addMouseListener(this);
		setTitle("Color Editor");

		colorchooser = new JColorChooser();
		colorchooser.getSelectionModel().addChangeListener(this);

		AbstractColorChooserPanel[] panels = colorchooser.getChooserPanels();
		AbstractColorChooserPanel[] panels2 = new AbstractColorChooserPanel[panels.length + 1];
		panels2[0] = new ColorSelectorComponent();

		for (int i = 0; i < panels.length; ++i) {
			colorchooser.removeChooserPanel(panels[i]);
			panels2[i + 1] = panels[i];
		}

		colorchooser.setChooserPanels(panels2);

		colorabletypes = new JPanel();
		colorsBackground = new JPanel();
		colorsBackground.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Background:"));
		colorsSprites1 = new JPanel();
		colorsSprites1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sprites 1:"));
		colorsSprites2 = new JPanel();
		colorsSprites2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sprites 2:"));
		colorsBackground.setLayout(new GridLayout(0, 4));
		colorsSprites1.setLayout(new GridLayout(0, 4));
		colorsSprites2.setLayout(new GridLayout(0, 4));
		colorPanes = new JPanel[12];
		for (int i = 0; i < 12; ++i) {
			colorPanes[i] = new JPanel();
			colorPanes[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			colorPanes[i].addMouseListener(this);
			colorPanes[i].setPreferredSize(new Dimension(48, 48));
		}
		colorsBackground.setLayout(new FlowLayout());
		colorsSprites1.setLayout(new FlowLayout());
		colorsSprites2.setLayout(new FlowLayout());
		colorabletypes.setLayout(new FlowLayout());
		for (int i = 0; i < 4; ++i)
			colorsBackground.add(colorPanes[i]);
		for (int i = 4; i < 8; ++i)
			colorsSprites1.add(colorPanes[i]);
		for (int i = 8; i < 12; ++i)
			colorsSprites2.add(colorPanes[i]);
		colorabletypes.setLayout(new GridLayout(3, 1));
		colorabletypes.add(colorsBackground);
		colorabletypes.add(colorsSprites1);
		colorabletypes.add(colorsSprites2);

		JPanel dividerthing = new JPanel();
		dividerthing.setLayout(new BorderLayout());
		JPanel presets = new JPanel();
		JPanel buttons = new JPanel();
		presets.setLayout(new GridLayout(2, 0));
		buttons.add(new JButton("New"));
		buttons.add(new JButton("Delete"));
		presets.add(buttons);
		presets.add(new JComboBox());
		presets.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Presets:"));
		dividerthing.add(presets, BorderLayout.NORTH);
		dividerthing.add(colorabletypes, BorderLayout.CENTER);
		add(colorchooser);
		add(dividerthing);

		initColorPanels();
		pack();
	}

	private void initColorPanels() {
		{
			int[][] c = vc.getGrayShade(0);
			for (int i = 0; i < 4; ++i)
				colorPanes[i].setBackground(new Color(c[i][0], c[i][1], c[i][2]));
		}
		{
			int[][] c = vc.getGrayShade(1);
			for (int i = 4; i < 8; ++i)
				colorPanes[i].setBackground(new Color(c[i % 4][0], c[i % 4][1], c[i % 4][2]));
		}
		{
			int[][] c = vc.getGrayShade(2);
			for (int i = 8; i < 12; ++i)
				colorPanes[i].setBackground(new Color(c[i % 4][0], c[i % 4][1], c[i % 4][2]));
		}
		currentColorPane = 0;
		updateColorPanes();
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(colorchooser.getSelectionModel())) {
			Color c = colorchooser.getColor();
			colorPanes[currentColorPane].setBackground(c);
			vc.setGrayShade(currentColorPane / 4, currentColorPane % 4, new int[] { c.getRed(), c.getGreen(), c.getBlue() });
		}
		gui.saveConfig();
	}

	public void mousePressed(MouseEvent event) {
		for (int i = 0; i < 12; ++i) {
			if (event.getSource().equals(colorPanes[i])) {
				currentColorPane = i;
				if (event.getButton() == MouseEvent.BUTTON1) {
					colorchooser.setColor(colorPanes[currentColorPane].getBackground());
				} else {
					stateChanged(new ChangeEvent(colorchooser.getSelectionModel()));
				}
				updateColorPanes();
			}
		}
	}

	public void setVisible(boolean b) {
		initColorPanels();
		super.setVisible(b);
	}

	public void actionPerformed(ActionEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}
}
