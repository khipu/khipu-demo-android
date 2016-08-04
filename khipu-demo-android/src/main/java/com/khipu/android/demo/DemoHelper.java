package com.khipu.android.demo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class DemoHelper {

	private static final Integer ID_DEL_COBRADOR = 4332;//2581;
	private static final String SECRET_DEL_COBRADOR = "fb957e59e95905c8764a97474380b4300eec7aa5";//"0ce9d2d383eb387ddf757fcd9eac8e4c53ee2c1f";


	static JSONObject createPayment(String subject, String amount, String payerEmail) {

		Integer receiverId = ID_DEL_COBRADOR;
		String secret = SECRET_DEL_COBRADOR;
		String method = "POST";
		String url = "https://khipu.com/api/2.0/payments";
		String toSign = method.toUpperCase() + "&" + percentEncode(url);

		HashMap<String, String> map = new HashMap<>();
		map.put("subject", subject);
		map.put("amount", amount);
		map.put("currency", "CLP");
		map.put("payer_email", payerEmail);
		map.put("return_url", "khipudemo://return.khipu.com");
		map.put("cancel_url", "khipudemo://cancel.khipu.com");

		List<String> keys = new LinkedList<>(map.keySet());
		Collections.sort(keys);

		for (String key : keys) {
			toSign += "&" + percentEncode(key) + "=" + percentEncode(map.get(key));
		}

		String sign = hmacSHA256(secret, toSign);
		String authorization = receiverId + ":" + sign;
		System.out.println(authorization);


		// ESTA INFORMACION SE USA PARA CREAR EL PAGO, ESTO SE DEBE HACER EN EL SERVIDOR DEL COMERCIO
		// VER DOCUMENTACION EN https://khipu.com/page/api

		try {
			URL u = new URL(url);

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, String> param : map.entrySet()) {
				if (postData.length() != 0) postData.append('&');
				postData.append(percentEncode(param.getKey()));
				postData.append('=');
				postData.append(percentEncode(param.getValue()));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", authorization);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setDoOutput(true);
			conn.getOutputStream().write(postDataBytes);

			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));




			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;)
				sb.append((char)c);
			String response = sb.toString();

			return new JSONObject(response);


		} catch (MalformedURLException m) {

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static String hmacSHA256(String secret, String data) {
		try {
			if (secret == null || data == null) {
				return "";
			}
			SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKeySpec);
			byte[] digest = mac.doFinal(data.getBytes("UTF-8"));
			return byteArrayToString(digest);
		} catch (InvalidKeyException ignored) {
			throw new RuntimeException("Invalid key exception while converting to HMac SHA256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String byteArrayToString(byte[] data) {
		BigInteger bigInteger = new BigInteger(1, data);
		String hash = bigInteger.toString(16);
		while (hash.length() < 64) {
			hash = "0" + hash;
		}
		return hash;
	}

	public static String percentEncode(String string) {
		if (string == null) {
			return "";
		}
		try {
			return URLEncoder.encode(string, "UTF-8")
					.replace("+", "%20")
					.replace("*", "%2A")
					.replace("%7E", "~");
		} catch (UnsupportedEncodingException exception) {
			throw new RuntimeException(exception.getMessage(), exception);
		}
	}
}
