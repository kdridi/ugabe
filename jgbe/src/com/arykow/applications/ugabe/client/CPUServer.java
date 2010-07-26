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

import java.io.IOException;


public interface CPUServer {

	public abstract void severLink(CPU cpu);

	public abstract void serveLink(CPU cpu) throws IOException;

	public abstract void clientLink(CPU cpu, String target) throws IOException;

	public abstract int updateServer(int lstatus) throws IOException;

}