package com.aptitekk.TeamScreen.Net.jrc.Server;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.one.stone.soup.screen.recorder.FrameCompressor;
import org.one.stone.soup.screen.recorder.ScreenRecorderListener;
import org.one.stone.soup.util.Queue;

import com.aptitekk.TeamScreen.TeamScreenDaemon;
import com.aptitekk.TeamScreen.Util.WindowInfo;

/**
 * This special type of Screen recorder makes it so we can update the screen
 * area with a special server<br>
 * Modified originally from the <i>ScreenRecorder</i> class
 * 
 * @author kevint, Nicholas Cross
 */
public class DynamicWindowScreenRecorder implements Runnable {

	private Rectangle recordArea;

	private int frameSize;
	private int[] rawData;

	private OutputStream oStream;

	private boolean recording = false;
	private boolean running = false;

	private long startTime;
	private long frameTime;
	private boolean reset;

	private ScreenRecorderListener listener;
	private WindowInfo window;
	
	private Thread thread;

	private class DataPack {
		public DataPack(int[] newData, long frameTime, int width, int height) {
			this.newData = newData;
			this.frameTime = frameTime;
			this.width = width;
			this.height = height;
		}

		public long frameTime;
		public int[] newData;
		public int width;
		public int height;
	}

	private class StreamPacker implements Runnable {
		private Queue queue = new Queue();
		private DynamicFrameCompressor compressor;

		public StreamPacker(OutputStream oStream, int frameSize) {
			compressor = new DynamicFrameCompressor(oStream, frameSize);

			new Thread(this, "Stream Packer").start();
		}

		public void packToStream(DataPack pack) {
			while (queue.size() > 2) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
				}
			}
			queue.post(pack);
		}

		public void run() {
			while (recording) {
				while (queue.isEmpty() == false) {
					DataPack pack = (DataPack) queue.get();
					try {
						compressor.pack(pack.newData, pack.frameTime, pack.width, pack.height, reset);

						if (reset == true) {
							reset = false;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						resize();
						try {
							compressor.pack(pack.newData, pack.frameTime, pack.width, pack.height, reset);
							if (reset == true) {
								reset = false;
							}
						} catch (IOException exception) {
							exception.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
						try {
							oStream.close();
						} catch (Exception e2) {
						}
						return;
					}
				}
				while (queue.isEmpty() == true) {
					try {
						Thread.sleep(50);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	private StreamPacker streamPacker;

	public DynamicWindowScreenRecorder(OutputStream oStream,
			ScreenRecorderListener listener, WindowInfo window) {
		this.listener = listener;
		this.oStream = oStream;
		this.window = window;
	}

	public void triggerRecordingStop() {
		recording = false;
	}

	public synchronized void run() {
		startTime = System.currentTimeMillis();

		recording = true;
		running = true;
		long lastFrameTime = 0;
		long time = 0;

		BufferedImage initCapture = captureWindow();
		adjustRecordArea(initCapture);

		frameSize = recordArea.width * recordArea.height;

		streamPacker = new StreamPacker(oStream, frameSize);

		while (recording) {
			// System.out.println("recordArea: " + recordArea);
			time = System.currentTimeMillis();
			while (time - lastFrameTime < 200) {
				try {
					Thread.sleep(50);
				} catch (Exception e) {
				}
				time = System.currentTimeMillis();
			}
			lastFrameTime = time;

			try {
				recordFrame();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					oStream.close();
				} catch (Exception e2) {
				}
				break;
			}
		}

		running = false;
		recording = false;

		listener.recordingStopped();
	}

	private void adjustRecordArea(BufferedImage bImage) {
		if ((bImage.getHeight() * bImage.getWidth()) != frameSize) {
			resize();
			System.out.println("Window size changed");
		}
	}

	public void recordFrame() throws IOException {
		// long t1 = System.currentTimeMillis();
		BufferedImage bImage = captureWindow();
		frameTime = System.currentTimeMillis() - startTime;
		// long t2 = System.currentTimeMillis();

		adjustRecordArea(bImage);

		rawData = bImage.getRGB(0, 0, bImage.getWidth(), bImage.getHeight(), null,
					0, bImage.getWidth());
		// long t3 = System.currentTimeMillis();

		// packToStream(rawData,newRawData);
		streamPacker.packToStream(new DataPack(rawData, frameTime, recordArea.width, recordArea.height));
		// long t4 = System.currentTimeMillis();

		/*
		 * System.out.println("Times");
		 * System.out.println("  capture time:"+(t2-t1));
		 * System.out.println("  data grab time:"+(t3-t2));
		 * System.out.println("  pack time:"+(t4-t3));
		 */

		listener.frameRecorded(false);
	}

	/*
	 * public Rectangle initialiseScreenCapture() { Point topleft = new
	 * Point(this.window.getRect()[0], this.window.getRect()[1]); Point
	 * bottomright = new Point(this.window.getRect()[2],
	 * this.window.getRect()[3]);
	 * 
	 * Rectangle windowRect = getWindowRect();
	 * System.out.println("Created screen area: " +
	 * String.format("(%d,%d)-(%d,%d)", topleft.x,
	 * topleft.y,bottomright.x,bottomright.y)); return windowRect; }
	 */

	public BufferedImage captureWindow() {
		Point mousePosition = MouseInfo.getPointerInfo().getLocation();
		BufferedImage image = null;
		try {
			image = TeamScreenDaemon.getWindowManager().captureWindow(
					this.window);
			// if(TeamScreenDaemon.getVerbose())
			// System.out.println("image width: " + image.getWidth() +
			// " height: " + image.getHeight());
			// File outputfile = new File("saved.png");
			// ImageIO.write(image, "png", outputfile);
		} catch (AWTException e) {
			e.printStackTrace();
		}

		Polygon pointer = new Polygon(new int[] { 0, 16, 10, 8 }, new int[] {
				0, 8, 10, 16 }, 4);
		Polygon pointerShadow = new Polygon(new int[] { 6, 21, 16, 14 },
				new int[] { 1, 9, 11, 17 }, 4);

		Graphics2D grfx = image.createGraphics();
		grfx.translate(mousePosition.x - this.window.getRelativeRectangle().x,
				mousePosition.y - this.window.getRelativeRectangle().y);
		grfx.setColor(new Color(100, 100, 100, 100));
		grfx.fillPolygon(pointerShadow);
		grfx.setColor(new Color(100, 100, 255, 200));
		grfx.fillPolygon(pointer);
		grfx.setColor(Color.black);
		grfx.drawPolygon(pointer);
		grfx.dispose();

		return image;
	}

	public void resize() {
		recordArea = this.window.getAbsRectangle();
		frameSize = recordArea.width * recordArea.height;
	}

	public void startRecording() {
		recordArea = this.window.getAbsRectangle();
		frameSize = recordArea.width * recordArea.height;

		if (recordArea == null) {
			return;
		}
		try {
			oStream.write((recordArea.width & 0x0000FF00) >>> 8);
			oStream.write((recordArea.width & 0x000000FF));

			oStream.write((recordArea.height & 0x0000FF00) >>> 8);
			oStream.write((recordArea.height & 0x000000FF));
		} catch (Exception e) {
			e.printStackTrace();
		}

		thread = new Thread(this, "Screen Recorder");
		thread.start();
	}

	public void stopRecording() {
		triggerRecordingStop();

		int count = 0;
		while (running == true && count < 10) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
			count++;
		}

		try {
			oStream.flush();
			oStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Rectangle getRecordArea() {
		return recordArea;
	}

	public boolean isRecording() {
		return recording;
	}

	public void sendKeyFrame() {
		reset = true;
	}

	public void updateWindow(WindowInfo info) {
		this.window = info;
	}

	public int getFrameSize() {
		return frameSize;
	}

}
