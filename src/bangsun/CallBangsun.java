package bangsun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import bangsun.InDataDomain;

public class CallBangsun {

	private String readFilePath = null;
	private String writeFilePath = null;
	private IdWorker idWorker = IdWorker.getFlowIdWorkerInstance();

	public static void main(String[] args) throws Exception {
		// CallBangSheng bs = new CallBangSheng("F:/aMidFolder/log.txt" ,
		// "F:/aMidFolder/log_result.txt");
		CallBangsun bs = new CallBangsun(args[0], args[1]);
		bs.controller();

		// CallBangsun bs = new CallBangsun("", "");
		// bs.convertDate("020160411214640");
	}

	public CallBangsun(String readFilePath, String writeFiltePath) {
		this.readFilePath = readFilePath;
		this.writeFilePath = writeFiltePath;
	}

	private void controller() {
		// 声明数据结构domain
		InDataDomain domain = null;
		try {
			// 声明文件：调用完记日志的文件
			File logFile = new File(writeFilePath);
			FileWriter fw = new FileWriter(logFile);

			// 声明文件：读取的文件
			File file = new File(readFilePath);
			if (file.exists()) {
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				while (line != null) {
					// 解析文件，放到domain
					String[] arr = line.split(" ");
					domain = new InDataDomain();

					long frms_trans_time = 0;
					frms_trans_time = convertDate(arr[0].trim());
					if (frms_trans_time <= 0) {
						line = br.readLine();
						continue;
					}
					domain.setFrms_trans_time(frms_trans_time);

					domain.setFrms_url(arr[1].trim());
					domain.setFrms_ip_cdn(arr[2].trim());
					domain.setFrms_ip_user(arr[3].trim());
					domain.setUser_name(arr[4].trim());
					// 调用接口
					call(fw, domain, line);
					// 继续读取输入流的数据
					line = br.readLine();
				}
				// 关闭
				br.close();
				fw.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 调用邦盛服务
	 * 
	 * @param domain
	 *            入参是拼接json串的几个属性
	 */
	private void call(FileWriter fw, InDataDomain domain, String inputLine) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL("http://10.2.240.100:8686/audit");
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("method", "post");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("User-Agent",
					"Apache-HttpClient/4.2.6 (java 1.5)");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			out = new PrintWriter(conn.getOutputStream());

			// String uuid = getUUID();
			String uuid = Long.toString(idWorker.nextId());
			StringBuffer sb = new StringBuffer();
			sb.append("[{ ")
					.append("\"@type\":\"cn.com.bsfit.frms.obj.AuditObject\"")
					// .append(",\"frms_biz_code\":\"PAY.QUERY\"")
					.append(",\"frms_biz_code\":\"PAY.REG\"")
					.append(",\"frms_ip\":")
					.append("\"" + domain.getFrms_ip_user() + "\"")
					.append(",\"frms_ip_cdn\":")
					.append("\"" + domain.getFrms_ip_cdn() + "\"")
					.append(",\"frms_trans_time\":")
					.append(domain.getFrms_trans_time())
					.append(",\"frms_url\":")
					.append("\"" + domain.getFrms_url() + "\"")
					.append(",\"frms_uuid\":").append("\"" + uuid + "\"")
					.append(",\"frms_user_id\":")
					.append("\"" + domain.getUser_name() + "\"").append("}]");
			out.print(sb.toString());
			out.flush();
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line = null, logFileString = "-1";
			while ((line = in.readLine()) != null) {
				result += line;
				// 解析json
				JSONArray b = JSONArray.parseArray(result);
				JSONObject c = (JSONObject) b.get(0);
				try {
					String cc = (String) c.get("retCode");
					if (!"200".equals(cc.trim()))
						continue;
					JSONArray d = (JSONArray) c.get("risks");
					JSONObject e = (JSONObject) d.get(0);
					// logFileString = ((String) e.get("ruleName")).substring(0,
					// 1);
					logFileString = (((String) e.get("ruleName")).split(":"))[0]
							.trim();
				} catch (Exception e) {
					logFileString = "0";
				}

			}
			// 写文件记录日志
			writeFile(fw, uuid, inputLine, logFileString);
		} catch (Exception e) {
			// System.out.println("发送 POST请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 自动生成UUID
	 * 
	 * @return 返回UUID
	 */
	private String getUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 获取当前时间，记日志用。
	 * 
	 * @return 当前时间，yyyyMMddHHmmssSSS型
	 */
	private String getCurrentTime() {
		SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss.SSS");
		return "[" + yyyyMMddHHmmssSSS.format(new Date()) + "] ";
	}

	/**
	 * 日期格式转换
	 * 
	 * @param dateStr
	 *            yyyyMMddHHmmss类型的时间
	 * @return 返回long行的时间
	 */
	private long convertDate(String dateStr) {

		if (dateStr.length() == 15 && dateStr.startsWith("0"))
			dateStr = dateStr.substring(1, dateStr.length());

		Date dt = null;
		try {
			SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyyMMddHHmmss");
			dt = YYYY_MM_DD.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dt.getTime();
	}

	private void writeFile(FileWriter fw, String uuid, String request,
			String response) {
		try {
			fw.write(generateWriteFileString(uuid, request, response));
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String generateWriteFileString(String uuid, String request,
			String response) {
		StringBuffer sb = new StringBuffer();
		sb.append(getCurrentTime() + " ").append(request).append(" ")
				.append(response).append("\n");
		return sb.toString();
	}

}
