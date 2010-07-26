package jgbe.testing;

import jgbe.CPU;
import jgbe.CPURunner;
import jgbe.CPUServer;
import jgbe.Cartridge;
import jgbe.CartridgeController;
import jgbe.CartridgeCreateHandler;
import jgbe.VideoScreenImpl;

public class Application {
	public static void main(String[] args) {
		CartridgeController cartridgeController = new CartridgeController();
		cartridgeController.createCartridge("/home/kdridi/sml.gb", new CartridgeCreateHandler() {
			public void onCreateCartridge(Cartridge cartridge) {
				CPUServer server = null;
				final CPU cpu = new CPU(server, new VideoScreenImpl());
				cpu.loadCartridge(cartridge);

				CPURunner cpuRunner = new CPURunner() {
					public void suspend() {
					}

					public void resume() {
						while (true) {
							cpu.runloop();
						}
					}

					public boolean isRunning() {
						return true;
					}

					public boolean hasThread(Thread t) {
						return false;
					}
				};
				if ((cpuRunner != null) && (cartridge != null)) {
					cpuRunner.resume();
				}
			}
		});
	}
}
