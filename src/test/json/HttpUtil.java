package test.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class HttpUtil {
	public static String getJsonContent(String urlStr) {
		try {// 获取HttpURLConnection连接对象
			URL url = new URL(urlStr);
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			// 设置连接属性
			httpConn.setConnectTimeout(3000);
			httpConn.setDoInput(true);
			httpConn.setRequestMethod("GET");
			// 获取相应码
			int respCode = httpConn.getResponseCode();
			if (respCode == 200) {
				return ConvertStream2Json(httpConn.getInputStream());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String ConvertStream2Json(InputStream inputStream) {
		String jsonStr = "";
		// ByteArrayOutputStream相当于内存输出流
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		// 将输入流转移到内存输出流中
		try {
			while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, len);
			}
			// 将内存流转换为字符串
			jsonStr = new String(out.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	// public static T getPerson(String jsonString, Class cls) {
	// T t = null;
	// try {
	// t = JSON.parseObject(jsonString, cls);
	// } catch (Exception e) {
	// // TODO: handle exception
	// }
	// return t;
	// }
	public static List getPersons(String jsonString, Class cls) {
		List list = new ArrayList();
		try {
			list = JSON.parseArray(jsonString, cls);
			// JSON.parse
		} catch (Exception e) {
		}
		return list;
	}

	public static List listKeyMaps(String jsonString) {
		List list = new ArrayList();
		try {
			list = JSON.parseObject(jsonString, new TypeReference() {
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
		return list;
	}

}
