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

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;

class MenuItemArrayGroup extends MenuItemArray {
	private static final long serialVersionUID = -3105189447306925051L;
	private ButtonGroup grp = new ButtonGroup();

	public boolean add(JMenuItem b) {
		boolean bbb = super.add(b);
		grp.add(b);
		return bbb;
	}

	public void add(JMenuItem b, int mnemonic) {
		super.add(b);
		grp.add(b);
	}

	public void remove(JMenuItem b) {
		super.remove(b);
		grp.remove(b);
	}

	public int getSelectedIndex() {
		int i = -1;
		for (JMenuItem ab : this) {
			++i;
			if (ab.isSelected())
				return i;
		}
		return -1;
	}

	public void setSelectedIndex(int index) {
		int i = -1;
		for (JMenuItem ab : this) {
			++i;
			ab.setSelected(i == index);
		}
	}
}