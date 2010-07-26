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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface StateSaveLoad<T> {
	public abstract void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, T t) throws IOException;

	public static class Impl {
		@SuppressWarnings("unchecked")
		public static <T> void stateSaveLoad(boolean save, int version, DataOutputStream dostream, DataInputStream distream, T t) throws IOException {
			((StateSaveLoad<T>) createInstance(t)).stateSaveLoad(save, version, dostream, distream, t);
		}

		private static <T> StateSaveLoad<?> createInstance(T t) throws IOException {
			StateSaveLoad<?> result = null;
			if (t instanceof AudioController) {
				result = new AudioControllerStateSaveLoad();
			} else if (t instanceof AudioControllerSoundRegister) {
				result = new AudioControllerSoundRegisterStateSaveLoad();
			} else if (t instanceof Cartridge) {
				result = new CartridgeStateSaveLoad();
			} else if (t instanceof CPU) {
				result = new CPUStateSaveLoad();
			} else if (t instanceof VideoController) {
				result = new VideoControllerStateSaveLoad();
			} else if (t instanceof IntVector) {
				result = new IntVectorStateSaveLoad();
			}
			return result;
		}
	}

}
