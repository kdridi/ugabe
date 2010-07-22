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