/**
 * This file is under LGPL license , 
 * created 2011/6/24 19:30  TonyQ 
 */
package org.zkoss.recaptcha;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class URLUtil {

	public static String fetch(String url) throws ConnectException {

		try {
			return fetch(new URL(url));
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("wrong url", ex);
		}
	}

	public static String post(String urlstr, String[] params) {

		try {
			// Construct data
			StringBuffer param = new StringBuffer();
			for (int i = 0; i < params.length; i += 2) {
				param.append(URLEncoder.encode(params[i], "UTF-8") + "=" + URLEncoder.encode(params[i + 1], "UTF-8")
						+ "&");
			}

			// Send data
			URL url = new URL(urlstr);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(param.toString());
			wr.flush();
			wr.close();
			
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer content = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				content.append(line+"\n");
			}
			
			rd.close();
			return content.toString();
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("wrong url", ex);
		} catch (Exception ex) {
			throw new RuntimeException("we meet trouble", ex);
		}
	}

	public static String fetch(URL u) throws ConnectException {

		StringBuffer content = new StringBuffer();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(u.openConnection().getInputStream()));

			String input = br.readLine();
			while (input != null) {
				content.append(input);
				input = br.readLine();
			}

			br.close();
		} catch (FileNotFoundException e) {
			throw new ConnectException("host reject conenction");
		} catch (ConnectException e) {
			throw e;
		} catch (IOException e) {
			return "";
		}
		return content.toString();
	}
}
