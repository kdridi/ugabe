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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Timer;

public class TimedKeyListener implements KeyListener, ActionListener {
	private final KeyListener kl;
	private final Timer timer;

	private KeyEvent releaseEvent;

	public TimedKeyListener(KeyListener akl) {
		kl = akl;
		timer = new Timer(0, this);
	}

	public void keyReleased(KeyEvent e) {
		if (timer.isRunning())
			kl.keyReleased(releaseEvent);
		releaseEvent = e;
		timer.restart();
	}

	public void keyPressed(KeyEvent e) {
		if (timer.isRunning()) {
			timer.stop();
			if (e.getKeyCode() != releaseEvent.getKeyCode())
				kl.keyReleased(releaseEvent);
		}
		kl.keyPressed(e);
	}

	public void keyTyped(KeyEvent e) {
		kl.keyTyped(e);
	}

	public void actionPerformed(ActionEvent e) {
		timer.stop();
		kl.keyReleased(releaseEvent);
	}
}
