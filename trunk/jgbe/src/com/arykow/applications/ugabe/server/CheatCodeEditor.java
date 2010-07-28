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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.arykow.applications.ugabe.client.Cartridge;
import com.arykow.applications.ugabe.client.ShTablesRRC;

public class CheatCodeEditor implements ComponentListener, ActionListener {
	private Font MonoFont = new Font("Bitstream Vera Sans Mono", 0, 12);
	String cartname;

	class DbTable extends JTable {
		private static final long serialVersionUID = 1L;

		public DbTable(DefaultTableModel m) {
			super(m);
		}

		public void setPreferredColumnWidths(double[] percentages) {
			Dimension tableDim = this.getPreferredSize();
			double total = 0;
			for (int i = 0; i < getColumnModel().getColumnCount(); i++)
				total += percentages[i];
			for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
				TableColumn column = getColumnModel().getColumn(i);
				column.setPreferredWidth((int) (tableDim.width * (percentages[i] / total)));
			}
		}

		public void setPreferredColumnWidths(int[] widths) {
			for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
				TableColumn column = getColumnModel().getColumn(i);
				column.setPreferredWidth(widths[i]);
			}
		}
	}

	public class CheatCodeEditorTableModelListener implements TableModelListener {
		public void tableChanged(TableModelEvent evt) {
			if (evt.getType() == TableModelEvent.UPDATE) {
				int row = evt.getFirstRow();
				CheatCode c = (CheatCode) codes.get(row);
				c.enabled = ((Boolean) tableCodes.getValueAt(row, 0)).booleanValue();
				c.setCode((String) tableCodes.getValueAt(row, 1));
				c.Description = (String) tableCodes.getValueAt(row, 2);
			}
		}
	}

	public class MyCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public MyCellRenderer() {
			super();
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setText(String.valueOf(value));
			CheatCode c = (CheatCode) codes.get(row);
			if (c.address < 0)
				setBackground(new Color(255, 220, 220));
			else
				setBackground(Color.WHITE);
			setFont(MonoFont);
			return this;
		}
	}

	public class TableCodesRightClick extends MouseAdapter {
		private void checkRightClick(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JTable source = (JTable) e.getSource();
				int row = source.rowAtPoint(e.getPoint());
				int column = source.columnAtPoint(e.getPoint());
				source.changeSelection(row, column, false, false);
				JPopupMenu popup = new JPopupMenu();
				popup.add(menuitemAddNewCode);
				popup.add(menuitemRemoveCode);
				popup.add(menuitemToggleActive);
				menuitemRemoveCode.setEnabled(source.getRowCount() > 1);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mouseReleased(MouseEvent e) {
			checkRightClick(e);
		}

		public void mousePressed(MouseEvent e) {
			checkRightClick(e);
		}
	}

	class CheatCode {
		public boolean enabled = false;
		public String Description = "";
		public String strCode;
		private int address;
		private int patch;
		private int oldvalue;

		class undoCheat {
			int MM_ROM_index_a = 0;
			int MM_ROM_index_b = 0;

			public undoCheat(int a, int b) {
				MM_ROM_index_a = a;
				MM_ROM_index_b = b;
			}
		}

		ArrayList<undoCheat> undos;

		public void addUndo(int a, int b) {
			undoCheat u = new undoCheat(a, b);
			undos.add(u);
		}

		public void undo(Cartridge cart) {
			for (undoCheat u : undos) {
				cart.MM_ROM[u.MM_ROM_index_a][u.MM_ROM_index_b] = oldvalue;
			}
		}

		public CheatCode(boolean e, String s, String d) {
			enabled = e;
			Description = d;
			setCode(s);
			undos = new ArrayList<undoCheat>();
		}

		private boolean isHex(String s) {
			String hex = "1234567890ABCDEF";
			return (hex.indexOf(s) > -1);
		}

		public void setCode(String s) {
			s = s.toUpperCase();
			address = -1;
			strCode = s;
			String c = "";

			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < s.length(); ++i) {
				buffer.append(isHex(s.substring(i, i + 1)) ? s.substring(i, i + 1) : "");
			}
			c= buffer.toString();
			if (c.length() > 9)
				c = c.substring(0, 8);
			if (c.length() == 9) {

				String addr = "$" + c.substring(5, 6) + c.substring(2, 3) + c.substring(3, 4) + c.substring(4, 5);
				String NewData = "$" + c.substring(0, 2);
				String OldData = "$" + c.substring(6, 7) + c.substring(8, 9);
				address = parser.StrToInt(addr) ^ 0xf000;
				patch = parser.StrToInt(NewData);
				oldvalue = parser.StrToInt(OldData) & 0xff;
				oldvalue = ShTablesRRC.val[0][oldvalue];
				oldvalue = ShTablesRRC.val[0][oldvalue];
				oldvalue ^= 0xba;
				if (address > 0x7fff)
					address = -1;

				strCode = c.substring(0, 3) + "-" + c.substring(3, 6) + "-" + c.substring(6, 9);
			}
		}
	}

	ArrayList<CheatCode> codes = new ArrayList<CheatCode>();
	JDialog dialog;
	JFrame owner;
	DbTable tableCodes;
	DefaultTableModel tablemodelcodes;
	JButton buttonDone;
	JMenuItem menuitemAddNewCode;
	JMenuItem menuitemRemoveCode;
	JMenuItem menuitemToggleActive;
	RDParser parser = new RDParser();

	public CheatCodeEditor(JFrame o, String cartname) {
		owner = o;
		JScrollPane scroll;
		dialog = new JDialog(owner, "Cheat Code Editor", true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {

				saveCheatCodes();
				dialog.setVisible(false);
			}
		});
		tablemodelcodes = new DefaultTableModel(1, 3);
		tablemodelcodes.addTableModelListener(new CheatCodeEditorTableModelListener());
		tableCodes = new DbTable(tablemodelcodes) {
			private static final long serialVersionUID = 7594112661094368719L;

			public Class<? extends Object> getColumnClass(int column) {
				return getValueAt(0, column).getClass();
			}
		};
		tableCodes.addMouseListener(new TableCodesRightClick());

		tableCodes.getColumnModel().getColumn(tableCodes.convertColumnIndexToView(0)).setHeaderValue("Active");
		tableCodes.getColumnModel().getColumn(tableCodes.convertColumnIndexToView(1)).setHeaderValue("Code");
		tableCodes.getColumnModel().getColumn(tableCodes.convertColumnIndexToView(2)).setHeaderValue("Effect");
		tableCodes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableCodes.setPreferredColumnWidths(new int[] { 38, 90, 480 - 38 - 90 - 3 });
		tableCodes.doLayout();

		scroll = new JScrollPane(tableCodes);

		scroll.setPreferredSize(new Dimension(480, 240));
		dialog.add(scroll, BorderLayout.CENTER);

		buttonDone = new JButton("Done");
		buttonDone.addActionListener(this);
		dialog.add(buttonDone, BorderLayout.SOUTH);

		dialog.addComponentListener(this);
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(false);
		dialog.pack();

		Dimension d = owner.getSize();
		Point p = new Point();
		p.setLocation((owner.getLocation().getX() + (d.getWidth() / 2)) - (dialog.getWidth() / 2), (owner.getLocation().getY() + d.getHeight() / 2) - (dialog.getHeight() / 2));
		dialog.setLocation(p);

		menuitemAddNewCode = new JMenuItem("Add new code here");
		menuitemAddNewCode.addActionListener(this);
		menuitemRemoveCode = new JMenuItem("Remove this code");
		menuitemRemoveCode.addActionListener(this);
		menuitemToggleActive = new JMenuItem("Toggle Active");
		menuitemToggleActive.addActionListener(this);

		TableColumnModel m = tableCodes.getColumnModel();
		MyCellRenderer r = new MyCellRenderer();
		TableColumn c = m.getColumn(1);
		c.setCellRenderer(r);
		this.cartname = cartname;
		loadCheatCodes();
		if (codes.size() == 0) {
			codes.add(new CheatCode(false, "", ""));
		}
		listCodes();
	}

	public void loadCheatCodes() {
		String name = null;
		try {
			name = FHandler.JGBEDir("cheats");
			name += cartname + ".cht";
			BufferedReader in = new BufferedReader(new FileReader(name));
			String str;
			codes.clear();
			while ((str = in.readLine()) != null) {
				int i = str.indexOf(",");
				int j = str.indexOf(",", i + 1);
				codes.add(new CheatCode(str.substring(0, i).equals("on") ? true : false, str.substring(i + 2, j), str.substring(j + 2)));
			}
			in.close();
		} catch (IOException e) {

		}
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
	}

	public void saveCheatCodes() {
		String name = null;
		try {
			name = FHandler.JGBEDir("cheats");
			name += cartname + ".cht";
			BufferedWriter out = new BufferedWriter(new FileWriter(name));
			String str;
			for (CheatCode c : codes) {
				str = c.enabled ? "on" : "off";
				str += ", " + c.strCode;
				str += ", " + c.Description;
				out.write(str, 0, str.length());
				out.newLine();
			}
			out.close();
		} catch (IOException e) {
			System.out.println("error writing to '" + name + "'");
			System.out.println(e.getMessage());
		}
	}

	private void listCodes() {
		Object[] o;
		tablemodelcodes.setRowCount(0);
		for (CheatCode ggc : codes) {
			o = new Object[3];
			o[0] = Boolean.valueOf(ggc.enabled);
			o[1] = ggc.strCode;
			o[2] = ggc.Description;
			tablemodelcodes.addRow(o);
		}
	}

	private void showWindow() {
		dialog.setVisible(true);
	}

	private void printFailedCode(CheatCode c) {
		System.out.println("Code failed to apply: " + c.strCode + " - " + c.Description);
		System.out.printf("  Address=$%04x oldvalue=$%02x patch=$%02x\n", c.address, c.oldvalue, c.patch);
	}

	protected boolean useCheats = true;

	public void useCheats(boolean b) {
		useCheats = b;
	}

	public void toggleCheats(Cartridge cart) {
		useCheats = !useCheats;
		applyCheatCodes(cart);
	}

	public void applyCheatCodes(Cartridge cart) {
		for (CheatCode c : codes) {
			if (c.enabled && useCheats) {
				if (c.address >= 0) {
					if (c.address < 0x4000) {
						if (cart.MM_ROM[c.address >> 12][c.address & 0x0fff] == c.oldvalue) {
							c.addUndo(c.address >> 12, c.address & 0x0fff);
							cart.MM_ROM[c.address >> 12][c.address & 0x0fff] = c.patch;
						} else
							printFailedCode(c);
					} else {
						boolean success = false;
						for (int i = 4; i < cart.rom_mm_size; i += 4) {
							if (cart.MM_ROM[i + ((c.address >> 12) & 3)][c.address & 0x0fff] == c.oldvalue) {
								c.addUndo(i + ((c.address >> 12) & 3), c.address & 0x0fff);
								cart.MM_ROM[i + ((c.address >> 12) & 3)][c.address & 0x0fff] = c.patch;
								success = true;
							}
						}
						if (!success)
							printFailedCode(c);
					}
				}
			} else {
				c.undo(cart);
			}
		}
	}

	public void editCodes() {
		showWindow();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(menuitemAddNewCode)) {
			int row = tableCodes.getSelectedRow();
			codes.add(row, new CheatCode(false, "", ""));
			Object[] o = new Object[3];
			o[0] = Boolean.FALSE;
			o[1] = new String();
			o[2] = new String();
			tablemodelcodes.insertRow(row, o);
			tableCodes.changeSelection(row, 0, false, false);
		} else if (e.getSource().equals(menuitemRemoveCode)) {
			int row = tableCodes.getSelectedRow();
			codes.remove(row);
			tablemodelcodes.removeRow(row);
		} else if (e.getSource().equals(menuitemToggleActive)) {
			int row = tableCodes.getSelectedRow();
			CheatCode c = (CheatCode) codes.get(row);
			c.enabled = !c.enabled;
			codes.set(row, c);
			tablemodelcodes.removeRow(row);
			Object[] o = new Object[3];
			o[0] = Boolean.valueOf(c.enabled);
			o[1] = c.strCode;
			o[2] = c.Description;
			tablemodelcodes.insertRow(row, o);
			tableCodes.changeSelection(row, 0, false, false);
		} else if (e.getSource().equals(buttonDone)) {

			saveCheatCodes();
			dialog.setVisible(false);
		} else {
			System.out.println("Unhandled event");
		}
	}
}
