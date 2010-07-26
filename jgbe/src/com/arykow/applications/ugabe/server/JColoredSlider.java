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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class JColoredSlider extends JSlider {
	private static final long serialVersionUID = 9120958536299578958L;

	private Color minColor;
	private Color maxColor;

	public JColoredSlider() {
		super();
	}

	public JColoredSlider(BoundedRangeModel brm) {
		super(brm);
	}

	public JColoredSlider(int orientation) {
		super(orientation);
	}

	public JColoredSlider(int min, int max) {
		super(min, max);
	}

	public JColoredSlider(int min, int max, int value) {
		super(min, max, value);
	}

	public JColoredSlider(int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
	}

	public void setMinColor(Color color) {
		minColor = color;
	}

	public void setMaxColor(Color color) {
		maxColor = color;
	}

	public void paintComponent(Graphics gr) {
		float width = (float) getSize().getWidth();
		float height = (float) getSize().getHeight();

		Graphics2D g = (Graphics2D) gr;
		g.setColor(getBackground());
		g.fillRect(0, 0, (int) width, (int) height);
		g.setPaint(new GradientPaint(0, 0, minColor, width, height, maxColor));
		g.fillRect(getX(), (int) height / 2, (int) width, (int) height);

		((BasicSliderUI) getUI()).paintLabels(g);
		((BasicSliderUI) getUI()).paintThumb(g);
	}
}

class JHueSlider extends JColoredSlider {
	private static final long serialVersionUID = 5975980866729723326L;

	protected float saturation = 0f;
	protected float value = 100f;

	public JHueSlider() {
		super();
	}

	public JHueSlider(BoundedRangeModel brm) {
		super(brm);
	}

	public JHueSlider(int orientation) {
		super(orientation);
	}

	public JHueSlider(int min, int max) {
		super(min, max);
	}

	public JHueSlider(int min, int max, int value) {
		super(min, max, value);
	}

	public JHueSlider(int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
	}

	public void setSaturationAndValue(float saturation, float value) {
		this.saturation = saturation;
		this.value = value;
	}

	public void paintComponent(Graphics gr) {
		float width = getWidth();
		float height = getHeight();

		Graphics2D g = (Graphics2D) gr;
		g.setColor(getBackground());
		g.fillRect(0, 0, (int) width, (int) height);
		int precision = (int) width;

		for (int i = 0; i < precision; ++i) {
			float hue = i * ((float) ColorSelectorComponent.MAX_HUE / (float) precision);
			g.setColor(ColorConverter.hsvToColor(hue, saturation, value));
			g.fillRect(getX() + (int) ((width / precision) * i), (int) height / 2, (int) ((width / precision) * (i + 1)), (int) height);
		}

		((BasicSliderUI) getUI()).paintLabels(g);
		((BasicSliderUI) getUI()).paintThumb(g);
	}
}
