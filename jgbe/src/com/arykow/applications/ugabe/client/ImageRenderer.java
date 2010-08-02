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
package com.arykow.applications.ugabe.client;

public interface ImageRenderer {

	public abstract void updateBLITFromPaletteColors(VideoController videoController, int srcPos, int dstPos);

	public abstract void updatePaletteColors(int index, int r, int g, int b);

	public abstract void blitImage(VideoController videoController);

}