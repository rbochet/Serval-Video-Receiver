package org.servalproject.receiver;

import java.io.IOException;
import java.io.InputStream;

public class DemuxerThread extends Thread {
	/** Path to fps */
	public static final String FPS_BIN = "lib/fps";

	/** Canonical name of hte video */
	private String name;
	
	private int instanceNumber;

	/**
	 * Set up the name of the video for further treatment in dumps/videos
	 * 
	 * @param instanceNumber
	 *            The number of the writer thread
	 */
	public DemuxerThread(int instanceNumber) {
		this.instanceNumber = instanceNumber;
		this.name = new String("video-" + instanceNumber);
		System.out.println("[Muxer #"+instanceNumber+"] Init.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		System.out.println("[Muxer #"+instanceNumber+"] Start.");
		try {
			ProcessBuilder demux = new ProcessBuilder(FPS_BIN, "dumps/" + name
					+ ".dump", "videos/" + name + ".mp4");
			InputStream is = demux.start().getErrorStream();
			dumpStream(is);

		} catch (IOException e) {
			System.err.println("Problem with the demuxer.");
			e.printStackTrace();
		}
	}

	/**
	 * Dump the stream on the error output
	 * 
	 * @param in
	 *            The stream
	 * @throws IOException
	 */
	private void dumpStream(InputStream in) throws IOException {
		byte[] arr = new byte[20];
		int n;
		do {
			n = in.read(arr);
			System.err.write(arr, 0, n);
		} while (n != -1);
	}

}
