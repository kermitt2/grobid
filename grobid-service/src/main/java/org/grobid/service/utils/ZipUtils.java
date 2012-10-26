package org.grobid.service.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {

	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	public static final void main(String[] args) {
		Enumeration entries;
		ZipFile zipFile;

		if (args.length != 1) {
			System.err.println("Usage: Unzip zipfile");
			return;
		}

		String pPath = args[0];
		try {
			zipFile = new ZipFile(pPath);

			entries = zipFile.entries();

			File tempDir = GrobidRestUtils.newTempFile("GROBID",
					Long.toString(System.nanoTime()));
			if (!(tempDir.delete())) {
				throw new IOException("Could not delete temp file: "
						+ tempDir.getAbsolutePath());
			}
			if (!(tempDir.mkdir())) {
				throw new IOException("Could not create temp directory: "
						+ tempDir.getAbsolutePath());
			}

			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					// Assume directories are stored parents first then
					// children.
					System.err.println("Extracting directory: "
							+ entry.getName());
					// This is not robust, just for demonstration purposes.

					(new File(tempDir.getAbsolutePath() + File.separator
							+ entry.getName())).mkdir();
					continue;
				}

				System.err.println("Extracting file: " + entry.getName());

				copyInputStream(
						zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(tempDir
								.getAbsolutePath()
								+ File.separator
								+ entry.getName())));
			}

			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
	}

}