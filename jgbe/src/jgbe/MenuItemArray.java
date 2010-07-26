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
package jgbe;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class MenuItemArray extends ArrayList<JMenuItem> {
	private static final long serialVersionUID = -8800398233194489440L;

	public boolean add(JMenuItem b) {
		return super.add(b);
	}

	public void add(JMenuItem b, int mnemonic) {
		super.add(b);
		b.setMnemonic(mnemonic);
	}

	public void remove(JMenuItem b) {
		super.remove(b);
	}

	public void addActionListener(ActionListener l) {
		for (JMenuItem ab : this)
			ab.addActionListener(l);
	}

	public void addToMenu(JMenu m) {
		for (JMenuItem ab : this)
			m.add(ab);
	}

	public void addToMenu(JPopupMenu m) {
		for (JMenuItem ab : this)
			m.add(ab);
	}

	public boolean contains(Object b) {
		return (b instanceof JMenuItem) && super.contains(b);
	}

	public JMenuItem get(int index) {
		return (JMenuItem) super.get(index);
	}
}