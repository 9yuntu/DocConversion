/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.jingyue.DocConversion.internal;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import com.jingyue.DocConversion.common.YuntuException;

public class HttpUtils {

	/**
	 * Send a HTTP GET request.
	 * 
	 * @param host
	 * @param path
	 * @param method
	 * @param headers
	 * @param querys
	 * @return
	 * @throws YuntuException
	 */
	public static synchronized String get(String host, String path,
			Map<String, String> headers, Map<String, String> querys)
			throws YuntuException {

		try {
			String url = buildUrl(host, path, querys);
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl
					.openConnection();

			for (Map.Entry<String, String> header : headers.entrySet()) {
				conn.setRequestProperty(header.getKey(), header.getValue());
			}

			if (conn.getResponseCode() == 200) {
				return getResponseAsString(conn);
			} else {
				throw new YuntuException(getResponseAsString(conn));
			}
		} catch (IOException e) {
			String response = "Please check the AppCode, "
					+ e.getLocalizedMessage();

			throw new YuntuException(response);
		}
	}

	/**
	 * Send a HTTP POST request.
	 * 
	 * @param host
	 * @param path
	 * @param method
	 * @param headers
	 * @param querys
	 * @return
	 * @throws YuntuException
	 */
	public static synchronized String post(String host, String path,
			Map<String, String> headers, Map<String, String> querys,
			InputStream inStream, String mimeType) throws YuntuException {

		try {
			String url = buildUrl(host, path, querys);
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl
					.openConnection();

			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", mimeType);
			BufferedOutputStream out = new BufferedOutputStream(
					conn.getOutputStream());

			byte[] bytes = new byte[1024];
			int numReadByte = 0;
			while ((numReadByte = inStream.read(bytes, 0, 1024)) > 0) {

				out.write(bytes, 0, numReadByte);
			}
			out.flush();
			inStream.close();

			for (Map.Entry<String, String> header : headers.entrySet()) {
				conn.setRequestProperty(header.getKey(), header.getValue());
			}

			if (conn.getResponseCode() == 200) {
				return getResponseAsString(conn);
			} else {
				throw new YuntuException(getResponseAsString(conn));
			}
		} catch (IOException e) {
			String response = "Please check the AppCode, "
					+ e.getLocalizedMessage();

			throw new YuntuException(response);
		}
	}

	private static String buildUrl(String host, String path,
			Map<String, String> querys) throws UnsupportedEncodingException {

		StringBuilder sbUrl = new StringBuilder();

		sbUrl.append(host);
		sbUrl.append(path);
		if (null != querys) {
			StringBuilder sbQuery = new StringBuilder();

			for (Map.Entry<String, String> query : querys.entrySet()) {
				if (query != null && query.getValue() != null) {
					if (0 < sbQuery.length()) {
						sbQuery.append("&");
					}
					sbQuery.append(query.getKey());
					sbQuery.append("=");
					sbQuery.append(URLEncoder.encode(query.getValue(), "utf-8"));
				}
			}
			if (0 < sbQuery.length()) {
				sbUrl.append("?").append(sbQuery);
			}
		}
		return sbUrl.toString();
	}

	private static String getResponseAsString(HttpURLConnection conn)
			throws IOException {

		InputStream es = conn.getErrorStream();

		if (es == null) {
			return getStreamAsString(conn.getInputStream(), "UTF-8");
		} else {
			String msg = getStreamAsString(es, "UTF-8");
			throw new IOException(conn.getResponseCode() + ":" + msg);
		}
	}

	private static String getStreamAsString(InputStream stream, String charset)
			throws IOException {

		try {
			int count;
			char[] chars = new char[256];
			StringWriter writer = new StringWriter();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream, charset));

			while ((count = reader.read(chars)) > 0) {
				writer.write(chars, 0, count);
			}
			return writer.toString();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}
