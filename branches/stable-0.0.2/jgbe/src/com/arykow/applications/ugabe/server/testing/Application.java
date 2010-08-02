package com.arykow.applications.ugabe.server.testing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import com.arykow.applications.ugabe.client.CPU;
import com.arykow.applications.ugabe.client.CPURunner;
import com.arykow.applications.ugabe.client.CPUServer;
import com.arykow.applications.ugabe.client.Cartridge;
import com.arykow.applications.ugabe.client.CartridgeController;
import com.arykow.applications.ugabe.client.CartridgeCreateHandler;
import com.arykow.applications.ugabe.client.UGABEService;
import com.arykow.applications.ugabe.client.UGABEServiceAsync;
import com.arykow.applications.ugabe.client.VideoScreen;
import com.arykow.applications.ugabe.server.UGABEServiceController;
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
				final CPU cpu = new CPU(server, new VideoScreen() {
					private int size = 0;
					private int index = 0;
					private BufferedImage[] images = new BufferedImage[2];

					public void swapImage() {
						String filename = String.format("%08d.png", size++);
						try {
							ImageIO.write(images[index], "png", new File(filename));
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println("file : " + filename);
						index ^= 1;
					}

					public void scaleImage(int scale) {
						int width = scale * SCREEN_WIDTH;
						int height = scale * SCREEN_HEIGHT;
						images[0] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						images[1] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					}

					public int[] getPixels() {
						return DataBufferInt.class.cast(images[index].getRaster().getDataBuffer()).getData();
					}
				});
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
