package com.starbug1.android.newsapp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.starbug1.android.newsapp.AppException;

public class FileDownloader {
	public static String download(String uri) throws AppException {
		InputStream is = null;
		BufferedOutputStream out = null;
		try {
			int responseCode = 0;
			final int BUFFER_SIZE = 10240;

			final URI url = new URI(uri);

			final HttpClient client = new DefaultHttpClient();
			final HttpGet get = new HttpGet();

			client.getParams().setParameter("http.connection.timeout",
					new Integer(15000));
			get.setURI(url);
			final HttpResponse res = client.execute(get);

			responseCode = res.getStatusLine().getStatusCode();

			String content = "";
			
			if (responseCode == HttpStatus.SC_OK) {
				is = res.getEntity().getContent();
				final BufferedInputStream in = new BufferedInputStream(is, BUFFER_SIZE);
				final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(
						byteStream, BUFFER_SIZE);

				final byte buf[] = new byte[BUFFER_SIZE];
				int size = -1;
				try {
					while ((size = in.read(buf)) != -1) {
						out.write(buf, 0, size);
					}
					out.flush();
					content = byteStream.toString();
				} finally {
					out.close(); out = null;
					in.close();
					byteStream.close();
				}
			} else if (responseCode == HttpStatus.SC_NOT_FOUND) {
				throw new AppException("NOT FOUND");
			} else if (responseCode == HttpStatus.SC_REQUEST_TIMEOUT) {
				throw new AppException("TIMEOUT");
			}
			return content;
		} catch (Exception e) {
			Log.e("FileDownloader", "failed to download." + e.getMessage());
			throw new AppException("failed to download.", e);
		} finally {
			try {
				if (is != null) is.close();
			} catch (Exception e) {}
			try {
				if (out != null) out.close();
			} catch (Exception e) {}
		}
	}
}
