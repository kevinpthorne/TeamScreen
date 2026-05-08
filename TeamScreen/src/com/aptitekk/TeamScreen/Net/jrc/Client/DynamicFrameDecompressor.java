package com.aptitekk.TeamScreen.Net.jrc.Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import com.aptitekk.TeamScreen.TeamScreenDaemon;

public class DynamicFrameDecompressor {
	private static final int ALPHA = 0xFF000000;

	public class FramePacket {
		private FramePacket(InputStream iStream, int previousSize) {
			this.iStream = iStream;
			this.previousData = new int[previousSize];
		}

		private void nextFrame() {
			if (newData != null) {
				previousData = newData;
				// newData = null;
			}
		}

		private InputStream iStream;
		private int[] previousData;
		private int resultedSize;
		private long frameTimeStamp;
		private byte[] packed;
		private int[] newData;
		private int[] frameDim = new int[2];

		public int[] getData() {
			return newData;
		}

		public int getResultSize() {
			return resultedSize;
		}

		public long getTimeStamp() {
			return frameTimeStamp;
		}

		public int[] getFrameDim() {
			return frameDim;
		}
	}

	public FramePacket frame;

	public DynamicFrameDecompressor(InputStream iStream, int frameSize) {
		frame = new FramePacket(iStream, frameSize);
	}

	/**
	 * Pulls from the stream and then calls
	 * 
	 * @return
	 * @throws IOException
	 */
	public FramePacket unpack() throws IOException {
		frame.nextFrame();

		// try{
		int i = frame.iStream.read();
		int time = i;
		time = time << 8;
		i = frame.iStream.read();
		time += i;
		time = time << 8;
		i = frame.iStream.read();
		time += i;
		time = time << 8;
		i = frame.iStream.read();
		time += i;

		frame.frameTimeStamp = (long) time;
		//System.out.println("ft:"+time);

		byte type = (byte) frame.iStream.read();
		//System.out.println("Packed Code:"+type);

		if (type <= 0) {
			// frame.newData = frame.previousData;
			frame.resultedSize = type;
			return frame;
		}
		/*
		 * } catch(Exception e) { e.printStackTrace(); }
		 */

		// GET SIZE
		int width = frame.iStream.read();
		width = width << 8;
		width += frame.iStream.read();
		frame.getFrameDim()[0] = width;
		int height = frame.iStream.read();
		height = height << 8;
		height += frame.iStream.read();
		frame.getFrameDim()[1] = height;
		//System.out.println("Frame size: " + width + "x" + height);

		ByteArrayOutputStream bO = new ByteArrayOutputStream();
		try {
			i = frame.iStream.read();
			int zSize = i;
			zSize = zSize << 8;
			i = frame.iStream.read();
			zSize += i;
			zSize = zSize << 8;
			i = frame.iStream.read();
			zSize += i;
			zSize = zSize << 8;
			i = frame.iStream.read();
			zSize += i;

			if (TeamScreenDaemon.getVerbose())
				System.out.println("Zipped Frame size: " + zSize);

			byte[] zData = new byte[zSize];
			int readCursor = 0;
			int sizeRead = 0;

			while (sizeRead > -1) {
				readCursor += sizeRead;
				if (readCursor >= zSize) {
					break;
				}

				sizeRead = frame.iStream.read(zData, readCursor, zSize
						- readCursor);
			}

			ByteArrayInputStream bI = new ByteArrayInputStream(zData);

			// ZipInputStream zI = new ZipInputStream(bI);
			// zI.getNextEntry();

			GZIPInputStream zI = new GZIPInputStream(bI);

			byte[] buffer = new byte[1000];
			sizeRead = zI.read(buffer);

			while (sizeRead > -1) {
				bO.write(buffer, 0, sizeRead);
				bO.flush();

				sizeRead = zI.read(buffer);
			}
			bO.flush();
			bO.close();
		} catch (Exception e) {
			e.printStackTrace();
			frame.resultedSize = 0;
			return frame;
		}

		frame.packed = bO.toByteArray();
		// System.out.println("UnZipped To: "+packed.length);

		runLengthDecode();

		return frame;
	}

	/**
	 * Actually decompresses frames
	 * 
	 */
	private void runLengthDecode() {
		frame.newData = new int[frame.frameDim[0] * frame.frameDim[1]];

		int inCursor = 0;
		int outCursor = 0;

		int blockSize = 0;

		int rgb = 0xFF000000;
		int red = 0xFFFF0000;
		int green = 0xFF00FF00;
		int blue = 0xFF0000FF;

		//System.out.println("Combining old:" + frame.previousData + " with new:"
		//		+ frame.newData);

		while (inCursor < frame.packed.length && outCursor < (frame.frameDim[0] * frame.frameDim[1])) {
			if (outCursor > frame.previousData.length) {
				//fill with black
				frame.newData[outCursor] = ((0 & 0xFF) << 16)
											| ((0 & 0xFF) << 8)
											| (0 & 0xFF);
				outCursor++;
				if (outCursor == frame.newData.length) {
					break;
				}
			}
			if (frame.packed[inCursor] == -1) {
				inCursor++;

				int count = (frame.packed[inCursor] & 0xFF);
				inCursor++;

				int size = count * 126;
				if (size > frame.newData.length) {
					size = frame.newData.length;
				}
				// System.arraycopy(frame.previousData,0,frame.newData,0,size);
				// outCursor+=size;

				for (int loop = 0; loop < (126 * count); loop++) {
					// frame.newData[outCursor]=blue;//frame.previousData[outCursor];
					frame.newData[outCursor] = frame.previousData[outCursor];
					// newRawData[outCursor]=blue;
					outCursor++;
					if (outCursor == frame.newData.length) {
						break;
					}
				}

			} else if (frame.packed[inCursor] < 0) // uncomp
			{
				blockSize = frame.packed[inCursor] & 0x7F;// (128+packed[inCursor]);
				inCursor++;

				for (int loop = 0; loop < blockSize; loop++) {
					rgb = ((frame.packed[inCursor] & 0xFF) << 16)
							| ((frame.packed[inCursor + 1] & 0xFF) << 8)
							| (frame.packed[inCursor + 2] & 0xFF) | ALPHA;
					if (rgb == ALPHA) {
						rgb = frame.previousData[outCursor];
					}
					// rgb = green;
					inCursor += 3;
					frame.newData[outCursor] = rgb;
					outCursor++;
					if (outCursor == frame.newData.length) {
						break;
					}
				}
			} else {
				blockSize = frame.packed[inCursor];
				inCursor++;
				rgb = ((frame.packed[inCursor] & 0xFF) << 16)
						| ((frame.packed[inCursor + 1] & 0xFF) << 8)
						| (frame.packed[inCursor + 2] & 0xFF) | ALPHA;

				boolean transparent = false;
				if (rgb == ALPHA) {
					transparent = true;
				}
				// rgb = red;
				inCursor += 3;

				for (int loop = 0; loop < blockSize; loop++) {
					if (transparent) {
						frame.newData[outCursor] = frame.previousData[outCursor];
					} else {
						frame.newData[outCursor] = rgb;
					}
					outCursor++;
					if (outCursor == frame.newData.length) {
						break;
					}
				}
			}
		}

		frame.resultedSize = outCursor;
	}

}
