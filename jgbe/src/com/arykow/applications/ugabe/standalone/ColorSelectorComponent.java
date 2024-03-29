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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.MaskFormatter;

public class ColorSelectorComponent extends AbstractColorChooserPanel {
	private static final long serialVersionUID = 1682610171942555978L;

	public static final int MIN_RED = 0;
	public static final int MAX_RED = 255;
	public static final int MIN_GREEN = 0;
	public static final int MAX_GREEN = 255;
	public static final int MIN_BLUE = 0;
	public static final int MAX_BLUE = 255;

	public static final int MIN_HUE = 0;
	public static final int MAX_HUE = 360;
	public static final int MIN_SATURATION = 0;
	public static final int MAX_SATURATION = 100;
	public static final int MIN_VALUE = 0;
	public static final int MAX_VALUE = 100;

	public static final float WHEEL_WIDTH = 0.2f;

	private JPanel pnlHSVRGBCS = new JPanel();

	private RGBColorSelectorComponent RGBCSC = new RGBColorSelectorComponent(this);
	private HSVColorSelectorComponent HSVCSC = new HSVColorSelectorComponent(this);
	private HexRGBColorSelectorComponent HRGBCSC = new HexRGBColorSelectorComponent(this);
	private ExtendedHSVColorSelectorComponent EHSVSC = new ExtendedHSVColorSelectorComponent(this);

	private boolean updating = false;

	public ColorSelectorComponent() {
		buildChooser();
	}

	public String getDisplayName() {
		return "HSV Wheel";
	}

	public Icon getLargeDisplayIcon() {
		return new ImageIcon();
	}

	public Icon getSmallDisplayIcon() {
		return new ImageIcon();
	}

	public void updateChooser() {
		if (!updating)
			updateColors(this, getColorFromModel().getRed(), getColorFromModel().getGreen(), getColorFromModel().getBlue());
	}

	protected void buildChooser() {
		add(EHSVSC, BorderLayout.CENTER);

		pnlHSVRGBCS.setLayout(new GridLayout(3, 1));
		pnlHSVRGBCS.add(RGBCSC);
		pnlHSVRGBCS.add(HSVCSC);
		pnlHSVRGBCS.add(HRGBCSC);
		add(pnlHSVRGBCS, BorderLayout.EAST);
	}

	int hackhack = 0;

	public void updateColors(JPanel caller, int r, int g, int b) {
		updating = true;

		getColorSelectionModel().setSelectedColor(new Color(r, g, b));

		RGBCSC.updateColors(r, g, b);

		HSVCSC.updateColors(r, g, b);

		EHSVSC.updateColors(r, g, b);

		HRGBCSC.updateColors(r, g, b);

		updating = false;
	}

	public void updateColors(JPanel caller, double h, double s, double v) {
		updating = true;

		Color col = ColorConverter.hsvToColor(h, s, v);

		getColorSelectionModel().setSelectedColor(col);

		RGBCSC.updateColors(h, s, v);

		HSVCSC.updateColors(h, s, v);

		EHSVSC.updateColors(h, s, v);

		HRGBCSC.updateColors(h, s, v);

		updating = false;
	}
}

class RGBColorSelectorComponent extends JPanel implements ChangeListener {
	private static final long serialVersionUID = -503370610040330258L;

	private JColoredSlider[] rgbSliders = { new JColoredSlider(ColorSelectorComponent.MIN_RED, ColorSelectorComponent.MAX_RED, ColorSelectorComponent.MIN_RED), new JColoredSlider(ColorSelectorComponent.MIN_GREEN, ColorSelectorComponent.MAX_GREEN, ColorSelectorComponent.MIN_GREEN), new JColoredSlider(ColorSelectorComponent.MIN_BLUE, ColorSelectorComponent.MAX_BLUE, ColorSelectorComponent.MIN_BLUE) };

	private SpinnerNumberModel[] snms = { new SpinnerNumberModel(ColorSelectorComponent.MIN_RED, ColorSelectorComponent.MIN_RED, ColorSelectorComponent.MAX_RED, 1), new SpinnerNumberModel(ColorSelectorComponent.MIN_RED, ColorSelectorComponent.MIN_RED, ColorSelectorComponent.MAX_RED, 1), new SpinnerNumberModel(ColorSelectorComponent.MIN_RED, ColorSelectorComponent.MIN_RED, ColorSelectorComponent.MAX_RED, 1) };

	private JSpinner[] rgbSpinners = { new JSpinner(snms[0]), new JSpinner(snms[1]), new JSpinner(snms[2]) };
	ColorSelectorComponent creator;
	boolean changing = false;

	public RGBColorSelectorComponent(ColorSelectorComponent creator) {
		this.creator = creator;

		setLayout(new BorderLayout(3, 0));

		JPanel pnlCenter = new JPanel();
		pnlCenter.setLayout(new GridLayout(3, 1));
		for (int i = 0; i < rgbSliders.length; ++i) {
			JSlider s = rgbSliders[i];
			s.setMajorTickSpacing(s.getMaximum());
			s.addChangeListener(this);
			pnlCenter.add(s);
		}

		JPanel pnlEast = new JPanel();
		pnlEast.setLayout(new GridLayout(3, 1));
		for (int i = 0; i < rgbSpinners.length; ++i) {
			JSpinner s = rgbSpinners[i];
			pnlEast.add(s);
			s.addChangeListener(this);
		}

		JPanel pnlWest = new JPanel();
		pnlWest.setLayout(new GridLayout(3, 1));
		pnlWest.add(new JLabel("R"));
		pnlWest.add(new JLabel("G"));
		pnlWest.add(new JLabel("B"));

		add(pnlCenter, BorderLayout.CENTER);
		add(pnlEast, BorderLayout.EAST);
		add(pnlWest, BorderLayout.WEST);
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "RGB Colors"));
	}

	public void updateSliders() {
		int r = rgbSliders[0].getValue();
		int g = rgbSliders[1].getValue();
		int b = rgbSliders[2].getValue();

		rgbSliders[0].setMinColor(new Color(0, g, b));
		rgbSliders[0].setMaxColor(new Color(255, g, b));

		rgbSliders[1].setMinColor(new Color(r, 0, b));
		rgbSliders[1].setMaxColor(new Color(r, 255, b));

		rgbSliders[2].setMinColor(new Color(r, g, 0));
		rgbSliders[2].setMaxColor(new Color(r, g, 255));

		repaint();
	}

	public void updateSpinners() {
		int r = rgbSliders[0].getValue();
		int g = rgbSliders[1].getValue();
		int b = rgbSliders[2].getValue();

		rgbSpinners[0].setValue(Integer.valueOf(r));
		rgbSpinners[1].setValue(Integer.valueOf(g));
		rgbSpinners[2].setValue(Integer.valueOf(b));
	}

	public void stateChanged(ChangeEvent e) {
		if (!changing) {
			if (e.getSource() instanceof JSlider)
				creator.updateColors(this, rgbSliders[0].getValue(), rgbSliders[1].getValue(), rgbSliders[2].getValue());
			else
				creator.updateColors(this, ((Integer) rgbSpinners[0].getValue()).intValue(), ((Integer) rgbSpinners[1].getValue()).intValue(), ((Integer) rgbSpinners[2].getValue()).intValue());
		}
	}

	public void updateColors(int r, int g, int b) {
		changing = true;
		rgbSliders[0].setValue(r);
		rgbSliders[1].setValue(g);
		rgbSliders[2].setValue(b);
		updateSliders();
		updateSpinners();
		changing = false;
	}

	public void updateColors(double h, double s, double v) {
		changing = true;

		int[] rgb = ColorConverter.hsvToRgb(h, s, v);

		rgbSliders[0].setValue(rgb[0]);
		rgbSliders[1].setValue(rgb[1]);
		rgbSliders[2].setValue(rgb[2]);
		updateSliders();
		updateSpinners();
		changing = false;
	}
}

class HSVColorSelectorComponent extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 1431931683556996784L;

	private JColoredSlider[] hsvSliders = { new JHueSlider(JSlider.HORIZONTAL, ColorSelectorComponent.MIN_HUE, ColorSelectorComponent.MAX_HUE, ColorSelectorComponent.MIN_HUE), new JColoredSlider(JSlider.HORIZONTAL, ColorSelectorComponent.MIN_SATURATION, ColorSelectorComponent.MAX_SATURATION, ColorSelectorComponent.MIN_SATURATION), new JColoredSlider(JSlider.HORIZONTAL, ColorSelectorComponent.MIN_VALUE, ColorSelectorComponent.MAX_VALUE, ColorSelectorComponent.MIN_VALUE) };

	private SpinnerNumberModel[] hsvms = { new SpinnerNumberModel(ColorSelectorComponent.MIN_HUE, ColorSelectorComponent.MIN_HUE, ColorSelectorComponent.MAX_HUE, 1), new SpinnerNumberModel(ColorSelectorComponent.MIN_SATURATION, ColorSelectorComponent.MIN_SATURATION, ColorSelectorComponent.MAX_SATURATION, 1), new SpinnerNumberModel(ColorSelectorComponent.MAX_VALUE, ColorSelectorComponent.MIN_VALUE, ColorSelectorComponent.MAX_VALUE, 1) };

	private JSpinner[] hsvSpinners = { new JSpinner(hsvms[0]), new JSpinner(hsvms[1]), new JSpinner(hsvms[2]) };
	ColorSelectorComponent creator;
	boolean changing = false;

	public HSVColorSelectorComponent(ColorSelectorComponent creator) {
		this.creator = creator;

		setLayout(new BorderLayout(3, 0));

		JPanel pnlCenter = new JPanel();
		pnlCenter.setLayout(new GridLayout(3, 1));

		for (int i = 0; i < hsvSliders.length; ++i) {

			JSlider s = hsvSliders[i];
			s.addChangeListener(this);
			pnlCenter.add(s);
		}
		JPanel pnlEast = new JPanel();
		pnlEast.setLayout(new GridLayout(3, 1));
		for (int i = 0; i < hsvSpinners.length; ++i) {
			JSpinner s = hsvSpinners[i];
			pnlEast.add(s);
			s.addChangeListener(this);
		}

		JPanel pnlWest = new JPanel();
		pnlWest.setLayout(new GridLayout(3, 1));
		pnlWest.add(new JLabel("H"));
		pnlWest.add(new JLabel("S"));
		pnlWest.add(new JLabel("V"));

		add(pnlCenter, BorderLayout.CENTER);
		add(pnlEast, BorderLayout.EAST);
		add(pnlWest, BorderLayout.WEST);
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "HSV Colors"));
	}

	private void updateSliders() {
		float h = (float) hsvSliders[0].getValue();
		float s = (float) hsvSliders[1].getValue();
		float v = (float) hsvSliders[2].getValue();

		((JHueSlider) hsvSliders[0]).setSaturationAndValue(s, v);

		hsvSliders[1].setMinColor(ColorConverter.hsvToColor(h, 0f, v));
		hsvSliders[1].setMaxColor(ColorConverter.hsvToColor(h, 100f, v));

		hsvSliders[2].setMinColor(ColorConverter.hsvToColor(h, s, 0f));
		hsvSliders[2].setMaxColor(ColorConverter.hsvToColor(h, s, 100f));
		repaint();
	}

	public void updateSpinners() {
		int h = hsvSliders[0].getValue();
		int s = hsvSliders[1].getValue();
		int v = hsvSliders[2].getValue();

		hsvSpinners[0].setValue(Integer.valueOf(h));
		hsvSpinners[1].setValue(Integer.valueOf(s));
		hsvSpinners[2].setValue(Integer.valueOf(v));
	}

	public void stateChanged(ChangeEvent e) {
		if (!changing) {

			if (e.getSource() instanceof JSlider) {
				creator.updateColors(this, (double) hsvSliders[0].getValue(), (double) hsvSliders[1].getValue(), (double) hsvSliders[2].getValue());

			} else {
				creator.updateColors(this, (double) ((Integer) hsvSpinners[0].getValue()).intValue(), (double) ((Integer) hsvSpinners[1].getValue()).intValue(), (double) ((Integer) hsvSpinners[2].getValue()).intValue());
			}
		}
	}

	public void updateColors(int r, int g, int b) {
		changing = true;
		double[] rgb = ColorConverter.rgbToHsv(r, g, b);
		hsvSliders[0].setValue((int) rgb[0]);
		hsvSliders[1].setValue((int) rgb[1]);
		hsvSliders[2].setValue((int) rgb[2]);
		updateSliders();
		updateSpinners();
		changing = false;
	}

	public void updateColors(double h, double s, double v) {

		changing = true;
		hsvSliders[0].setValue((int) h);
		hsvSliders[1].setValue((int) s);
		hsvSliders[2].setValue((int) v);
		updateSliders();
		updateSpinners();
		changing = false;
	}
}

class HexRGBColorSelectorComponent extends JPanel implements ActionListener {
	private static final long serialVersionUID = -5329299425365436690L;

	private boolean changing = false;
	private ColorSelectorComponent creator;
	private MaskFormatter formatter;
	private JFormattedTextField rgbHex;

	public HexRGBColorSelectorComponent(ColorSelectorComponent creator) {
		try {
			formatter = new MaskFormatter("HHHHHH");
			rgbHex = new JFormattedTextField(formatter);
			rgbHex.addActionListener(this);
		} catch (Exception pe) {
			pe.printStackTrace();
		}

		this.creator = creator;
		setLayout(new BorderLayout(3, 0));
		add(rgbHex, BorderLayout.NORTH);
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "RGB Hex Colors"));
	}

	public void actionPerformed(ActionEvent e) {
		if (!changing) {
			String hexStr = rgbHex.getText();

			try {
				int hex = Integer.parseInt(hexStr, 16);
				int r = (hex >> 16) & 0xFF;
				int g = (hex >> 8) & 0xFF;
				int b = hex & 0xFF;
				creator.updateColors(this, r, g, b);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void updateTextField(int r, int g, int b) {
		String text = String.format("%02x%02x%02x", r, g, b);
		rgbHex.setText(text);
	}

	public void updateColors(int r, int g, int b) {
		changing = true;
		updateTextField(r, g, b);
		changing = false;
	}

	public void updateColors(double h, double s, double v) {
		changing = true;

		int[] rgb = ColorConverter.hsvToRgb(h, s, v);
		updateTextField(rgb[0], rgb[1], rgb[2]);
		changing = false;
	}
}

class ExtendedHSVColorSelectorComponent extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -7624968751986631294L;

	private float cAngle = 0f;
	private double cHue = 0f;
	private double cValue = 0f;
	private double cSat = 0f;
	private boolean circleDragging = false;
	private boolean triangleDragging = false;
	ColorSelectorComponent creator;

	public ExtendedHSVColorSelectorComponent(ColorSelectorComponent creator) {
		this.creator = creator;
		setPreferredSize(new Dimension(300, 300));
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "Extended HSV Colors"));
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void updateColors(int r, int g, int b) {
		double[] hsv = ColorConverter.rgbToHsv(r, g, b);
		cHue = hsv[0];
		cAngle = (float) hsv[0];
		cSat = hsv[1];
		cValue = hsv[2];
		repaint();
	}

	public void updateColors(double h, double s, double v) {
		cAngle = (float) h;
		cHue = h;
		cSat = s;
		cValue = v;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		circleDragging = isInCircle(e.getX(), e.getY());
		triangleDragging = isInTriangle(e.getX(), e.getY());
		mouseDragged(e);
	}

	public void mouseReleased(MouseEvent e) {
		circleDragging = false;
		triangleDragging = false;
	}

	private Point getPointClosestToLine(int x1, int y1, int x2, int y2, int x3, int y3) {
		double u = ((x3 - x1) * (x2 - x1)) + ((y3 - y1) * (y2 - y1));
		u /= Point2D.distanceSq(x1, y1, x2, y2);
		if (u <= 0)
			return new Point(x1, y1);
		if (u >= 1)
			return new Point(x2, y2);
		return new Point((int) Math.round(x1 + u * (x2 - x1)), (int) Math.round(y1 + u * (y2 - y1)));
	}

	public void mouseDragged(MouseEvent e) {
		if (circleDragging) {
			int x = getCenterX() - e.getX();
			int y = getCenterY() - e.getY();
			cAngle = getAngle(x, y);
			cHue = cAngle;
			creator.updateColors(this, cHue, cSat, cValue);
			repaint();
		}

		if (triangleDragging) {
			int x = e.getX();
			int y = e.getY();

			int[] xs = new int[3];
			int[] ys = new int[3];

			xs[0] = getCenterX() + getX(Math.IEEEremainder(cAngle + 0f, 360f), getRadius());
			ys[0] = getCenterY() + getY(Math.IEEEremainder(cAngle + 0f, 360f), getRadius());

			xs[1] = getCenterX() + getX(Math.IEEEremainder(cAngle + 120f, 360f), getRadius());
			ys[1] = getCenterY() + getY(Math.IEEEremainder(cAngle + 120f, 360f), getRadius());

			xs[2] = getCenterX() + getX(Math.IEEEremainder(cAngle + 240f, 360f), getRadius());
			ys[2] = getCenterY() + getY(Math.IEEEremainder(cAngle + 240f, 360f), getRadius());

			int haxx = 0;
			if (!isInTriangle(x, y)) {
				Point[] p = new Point[3];
				double[] d = new double[3];
				for (int i = 0; i < 3; ++i) {
					p[i] = getPointClosestToLine(xs[i], ys[i], xs[(i + 1) % 3], ys[(i + 1) % 3], x, y);
					d[i] = p[i].distance(x, y);
				}

				for (int i = 0; i < 3; ++i) {
					if (d[i] <= d[(i + 1) % 3] && d[i] <= d[(i + 2) % 3]) {
						x = p[i].x;
						y = p[i].y;
						haxx |= 1 << i;
					}
				}
			}

			double max_v_dist = Line2D.ptLineDist(xs[0], ys[0], xs[1], ys[1], xs[2], ys[2]);
			double tr_v = Line2D.ptLineDist(xs[0], ys[0], xs[1], ys[1], x, y);
			tr_v /= max_v_dist;

			double max_s_dist = Point2D.distance(xs[0], ys[0], xs[1], ys[1]);
			double tr_s = Line2D.ptLineDist(xs[1], ys[1], xs[1] - (ys[0] - ys[1]), ys[1] + (xs[0] - xs[1]), x, y);
			tr_s -= tr_v * max_s_dist / 2;
			tr_v = 1 - tr_v;
			tr_s /= tr_v * max_s_dist;

			cSat = tr_s * ColorSelectorComponent.MAX_SATURATION;
			cValue = tr_v * ColorSelectorComponent.MAX_VALUE;

			if (0 != (haxx & 1))
				cValue = 100;
			if (0 != (haxx & 2))
				cSat = 0;
			if (0 != (haxx & 4))
				cSat = 100;

			cHue = cAngle;
			creator.updateColors(this, cHue, cSat, cValue);
			repaint();
		}
	}

	private int getCenterX() {
		return (int) (getSize().getWidth() / 2);
	}

	private int getCenterY() {
		return (int) (getSize().getHeight() / 2);
	}

	private int getMin() {
		Insets bi = getInsets();
		int bw = bi.left + bi.right;
		int bh = bi.top + bi.bottom;
		int width = (int) getSize().getWidth() - bw;
		int height = (int) getSize().getHeight() - bh;
		return (width < height) ? width : height;
	}

	private int getRadius() {
		return (int) (getMin() * (1 - ColorSelectorComponent.WHEEL_WIDTH)) / 2;
	}

	private boolean isInCircle(int x, int y) {

		int dist = (int) Point2D.distance(getCenterX(), getCenterY(), x, y);
		return ((int) (getMin() * ColorSelectorComponent.WHEEL_WIDTH * 2) <= dist) && (dist <= getMin() / 2);
	}

	private boolean isInTriangle(int x, int y) {
		int[] xs = new int[3];
		int[] ys = new int[3];

		xs[0] = getCenterX() + getX(Math.IEEEremainder(cAngle + 0f, 360f), getRadius());
		ys[0] = getCenterY() + getY(Math.IEEEremainder(cAngle + 0f, 360f), getRadius());

		xs[1] = getCenterX() + getX(Math.IEEEremainder(cAngle + 120f, 360f), getRadius());
		ys[1] = getCenterY() + getY(Math.IEEEremainder(cAngle + 120f, 360f), getRadius());

		xs[2] = getCenterX() + getX(Math.IEEEremainder(cAngle + 240f, 360f), getRadius());
		ys[2] = getCenterY() + getY(Math.IEEEremainder(cAngle + 240f, 360f), getRadius());

		Polygon p = new Polygon(xs, ys, 3);

		return p.contains(x, y);
	}

	public int getMaxRadius() {
		return getMin() / 2;
	}

	public float getAngle(int x, int y) {
		float angle = (float) Math.toDegrees(Math.atan2(y, x));
		if (angle < 0)
			angle += 360;
		angle -= 180;
		if (angle < 0)
			angle += 360;
		return angle;
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void paintComponent(Graphics g2) {
		Graphics2D g = (Graphics2D) g2;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(getBackground());
		g.fillRect(0, 0, (int) getSize().getWidth(), (int) getSize().getHeight());
		Insets bi = getInsets();
		int bw = bi.left + bi.right;
		int bh = bi.top + bi.bottom;
		int width = (int) getSize().getWidth() - bw;
		int height = (int) getSize().getHeight() - bh;
		int min = (width < height) ? width : height;
		int x = ((int) (getSize().getWidth() / 2) - (min / 2));
		int y = ((int) (getSize().getHeight() / 2) - (min / 2));

		int r1 = min / 2;
		int r2 = (int) (min * (1 - ColorSelectorComponent.WHEEL_WIDTH)) / 2;
		x = x + min / 2;
		y = y + min / 2;

		int precision = 256 * 3;

		for (int i = 0; i < precision; ++i) {
			g.setColor(ColorConverter.hsvToColor(i * (float) ColorSelectorComponent.MAX_HUE / ((float) precision), (float) ColorSelectorComponent.MAX_SATURATION, (float) ColorSelectorComponent.MAX_VALUE));

			int[] xs = new int[4];
			int[] ys = new int[4];

			xs[0] = x + getX(i * 360f / (precision), r1);
			xs[1] = x + getX(i * 360f / (precision) + 1, r1);
			xs[2] = x + getX(i * 360f / (precision) + 1, r2);
			xs[3] = x + getX(i * 360f / (precision), r2);

			ys[0] = y + getY(i * 360f / (precision), r1);
			ys[1] = y + getY(i * 360f / (precision) + 1, r1);
			ys[2] = y + getY(i * 360f / (precision) + 1, r2);
			ys[3] = y + getY(i * 360f / (precision), r2);

			g.fillPolygon(xs, ys, 4);
		}

		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2.0f));
		g.drawLine(x + getX(cAngle, r1), y + getY(cAngle, r1), x + getX(cAngle, r2), y + getY(cAngle, r2));

		drawTriangle(g, x, y, r2, cAngle);

		int[] xs = new int[4];
		int[] ys = new int[4];

		xs[0] = getCenterX() + getX(Math.IEEEremainder(cAngle + 0f, 360f), getRadius());
		ys[0] = getCenterY() + getY(Math.IEEEremainder(cAngle + 0f, 360f), getRadius());

		xs[1] = getCenterX() + getX(Math.IEEEremainder(cAngle + 120f, 360f), getRadius());
		ys[1] = getCenterY() + getY(Math.IEEEremainder(cAngle + 120f, 360f), getRadius());

		xs[2] = getCenterX() + getX(Math.IEEEremainder(cAngle + 240f, 360f), getRadius());
		ys[2] = getCenterY() + getY(Math.IEEEremainder(cAngle + 240f, 360f), getRadius());

		xs[3] = (xs[0] + xs[1]) >>> 1;
		ys[3] = (ys[0] + ys[1]) >>> 1;

		int cw = 2;

		double max_s_dist = (cSat - 50) * 2;

		max_s_dist *= cValue;
		max_s_dist /= -100;

		x = xs[2];
		y = ys[2];
		x += (int) Math.round(cValue * (xs[3] - xs[2]) / 100);
		y += (int) Math.round(cValue * (ys[3] - ys[2]) / 100);
		x += (int) Math.round(max_s_dist * (xs[3] - xs[0]) / 100);
		y += (int) Math.round(max_s_dist * (ys[3] - ys[0]) / 100);
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(2.0f));

		g.drawOval(x - cw, y - cw, cw * 2, cw * 2);
	}

	public void drawTriangle(Graphics2D g, int x, int y, int radius, float angle) {
		int[] xs = new int[3];
		int[] ys = new int[3];

		xs[0] = x + getX(Math.IEEEremainder(angle + 0f, 360f), radius);
		ys[0] = y + getY(Math.IEEEremainder(angle + 0f, 360f), radius);

		xs[1] = x + getX(Math.IEEEremainder(angle + 120f, 360f), radius);
		ys[1] = y + getY(Math.IEEEremainder(angle + 120f, 360f), radius);

		xs[2] = x + getX(Math.IEEEremainder(angle + 240f, 360f), radius);
		ys[2] = y + getY(Math.IEEEremainder(angle + 240f, 360f), radius);

		Point[] p = new Point[3];
		p[0] = new Point(xs[0], ys[0]);
		p[1] = new Point(xs[1], ys[1]);
		p[2] = new Point(xs[2], ys[2]);

		Color[] c = new Color[3];
		c[0] = ColorConverter.hsvToColor(angle, 100f, 100f);
		c[1] = Color.WHITE;
		c[2] = Color.BLACK;

		g.setPaint(new TriGradientPaint(p, c));
		g.fillPolygon(xs, ys, 3);
	}

	private int getX(float degree, int radius) {
		return (int) (radius * (Math.cos(Math.toRadians(degree))));
	}

	private int getY(float degree, int radius) {
		return (int) (radius * (Math.sin(Math.toRadians(degree))));
	}

	private int getX(double degree, int radius) {
		return (int) (radius * (Math.cos(Math.toRadians(degree))));
	}

	private int getY(double degree, int radius) {
		return (int) (radius * (Math.sin(Math.toRadians(degree))));
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}
}

class ColorConverter {

	public static Color hsvToColor(float h, float s, float v) {
		return Color.getHSBColor(h / ColorSelectorComponent.MAX_HUE, s / ColorSelectorComponent.MAX_SATURATION, v / ColorSelectorComponent.MAX_VALUE);
	}

	public static Color hsvToColor(double h, double s, double v) {
		return Color.getHSBColor((float) (h / ColorSelectorComponent.MAX_HUE), (float) (s / ColorSelectorComponent.MAX_SATURATION), (float) (v / ColorSelectorComponent.MAX_VALUE));
	}

	public static double[] rgbToHsv(int r, int g, int b) {
		double[] result = new double[3];
		float[] temp = new float[3];
		Color.RGBtoHSB(r, g, b, temp);
		result[0] = temp[0] * ColorSelectorComponent.MAX_HUE;
		result[1] = temp[1] * ColorSelectorComponent.MAX_SATURATION;
		result[2] = temp[2] * ColorSelectorComponent.MAX_VALUE;
		return result;
	}

	public static int[] hsvToRgb(float h, float s, float v) {
		int[] result = new int[3];
		int color = Color.HSBtoRGB(h / ColorSelectorComponent.MAX_HUE, s / ColorSelectorComponent.MAX_SATURATION, v / ColorSelectorComponent.MAX_VALUE);

		result[2] = color & 0xff;
		result[1] = (color >> 8) & 0xff;
		result[0] = (color >> 16) & 0xff;
		return result;
	}

	public static int[] hsvToRgb(double h, double s, double v) {
		int[] result = new int[3];
		int color = Color.HSBtoRGB((float) (h / ColorSelectorComponent.MAX_HUE), (float) (s / ColorSelectorComponent.MAX_SATURATION), (float) (v / ColorSelectorComponent.MAX_VALUE));

		result[2] = color & 0xff;
		result[1] = (color >> 8) & 0xff;
		result[0] = (color >> 16) & 0xff;
		return result;
	}
}
