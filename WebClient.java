package com.kogul.stockboard;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public abstract class WebClient {

	private static final String LOG_TAG = WebClient.class.getSimpleName();

	public static String get(URL url) throws IOException {
		for (int attempts = 0; attempts < 5; ++attempts) {
			String response;
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.connect();
				int responseCode = conn.getResponseCode();
				InputStreamReader reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
				StringBuilder sbuf = new StringBuilder();
				char[] rdbuf = new char[4096];
				while (true) {
					int n = reader.read(rdbuf);
					if (n < 0)
						break;
					sbuf.append(rdbuf, 0, n);
				}
				response = sbuf.toString();
				if (responseCode > 300 && responseCode < 400) {
					String location = conn.getHeaderField("location");
					if (location != null) {
						url = new URL(location);
						continue;
					}
				}
				if (responseCode != 200) {
					throw new IOException("HTTP code " + responseCode + " response: " + response);
				}
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
			return response;
		}
		throw new IOException("Too many redirects");
	}

}
