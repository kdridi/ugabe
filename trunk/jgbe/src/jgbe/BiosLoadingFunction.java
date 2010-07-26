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

public interface BiosLoadingFunction {
	public void loadBios(String filename, BiosLoadingHandler handler);
}
