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

class Format142 {
	public static final String strformat(String s, int[] iarr) {
		Object oarr[] = new Object[iarr.length];
		for (int i = 0; i < iarr.length; ++i)
			oarr[i] = Integer.valueOf(iarr[i]);
		return String.format(s, oarr);

	}

	public static final String strformat(String s, double[] iarr) {

		Object oarr[] = new Object[iarr.length];
		for (int i = 0; i < iarr.length; ++i)
			oarr[i] = new Double(iarr[i]);
		return String.format(s, oarr);

	}

	public static final void strprintf(String s, int[] iarr) {
		s = strformat(s, iarr);
		System.out.print(s);
	}
}
