package jgbe.testing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;

import javax.imageio.ImageIO;

import jgbe.CPU;
import jgbe.CPURunner;
import jgbe.CPUServer;
import jgbe.Cartridge;
import jgbe.CartridgeController;
import jgbe.CartridgeCreateHandler;
import jgbe.VideoScreen;

public class Application {
	public static void main(String[] args) {
		CartridgeController cartridgeController = new CartridgeController();
		cartridgeController.createCartridge("/home/kdridi/sml.gb", new CartridgeCreateHandler() {
			public void onCreateCartridge(Cartridge cartridge) {
				CPUServer server = null;
				final CPU cpu = new CPU(server, new VideoScreen() {
					private int size = 0;
					private int index = 0;
					private int[][] pixels;
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
						pixels = new int[2][160 * 144 * scale * scale];
						

						int width = scale * MIN_WIDTH;
						int height = scale * MIN_HEIGHT;
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
