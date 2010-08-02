package com.arykow.applications.ugabe.server.testing;

import java.util.List;

import com.arykow.applications.ugabe.client.CPU;
import com.arykow.applications.ugabe.client.CPURunner;
import com.arykow.applications.ugabe.client.CPUServer;
import com.arykow.applications.ugabe.client.Cartridge;
import com.arykow.applications.ugabe.client.CartridgeController;
import com.arykow.applications.ugabe.client.CartridgeCreateHandler;
import com.arykow.applications.ugabe.client.UGABEService;
import com.arykow.applications.ugabe.client.UGABEServiceAsync;
import com.arykow.applications.ugabe.server.UGABEServiceController;
import com.arykow.applications.ugabe.standalone.ImageRendererGUI;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class Application {
	public static void main(String[] args) {
		UGABEServiceAsync service = new UGABEServiceAsync() {
			public void loadCartridge(String fileName, AsyncCallback<List<Integer>> callback) {
				UGABEService service = new UGABEServiceController();
				try {
					callback.onSuccess(service.loadCartridge(fileName));
				} catch (Exception e) {
					callback.onFailure(e);
				}
			}
		};
		CartridgeController cartridgeController = new CartridgeController(service);
		cartridgeController.createCartridge("/home/kdridi/sml.gb", new CartridgeCreateHandler() {
			public void onCreateCartridge(Cartridge cartridge) {
				CPUServer server = null;
				final CPU cpu = new CPU(server, new ImageRendererGUI(null));
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

				};
				if ((cpuRunner != null) && (cartridge != null)) {
					cpuRunner.resume();
				}
			}
		});
	}
}
