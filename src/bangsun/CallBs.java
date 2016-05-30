package bangsun;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import bangsun.InDataDomain;

public class CallBs {

	private String resultFilePath = null;
	private String exceptionLogFilePath = null;

	// private IdWorker idWorker = IdWorker.getFlowIdWorkerInstance();

	public static void main(String[] args) throws Exception {
		BufferedReader brIn = new BufferedReader(new InputStreamReader(
				System.in));
		// File file = new File("D:\\Downloads\\222.txt");
		// InputStream fileInputStream = new FileInputStream(file);
		// BufferedReader brIn = new BufferedReader(new
		// InputStreamReader(fileInputStream));
		CallBs bs = new CallBs(args[0], args[1]);
		// CallBs bs = new CallBs("D:\\Downloads\\result.txt",
		// "D:\\Downloads\\exc.txt");
		bs.controller(brIn);
	}

	public CallBs(String resultFilePath, String exceptionLogFilePath) {
		this.resultFilePath = resultFilePath;
		this.exceptionLogFilePath = exceptionLogFilePath;
	}

	private void controller(BufferedReader brIn) {
		// 声明数据结构domain
		InDataDomain domain = null;
		try {
			// 声明文件：调用完记日志的文件
			File resultFile = new File(resultFilePath);
			FileWriter resultFileWriter = new FileWriter(resultFile);
			File exceptionLogFile = new File(exceptionLogFilePath);
			FileWriter exceptionFileWriter = new FileWriter(exceptionLogFile);

			// 数据源：标准输入流
			String line = brIn.readLine();
			while (true) {
				// 解析文件，放到domaina
				if (line == null) {
					try {
						Thread.sleep(10);
					} catch (Exception e) {
					}
					continue;
				}
				String[] arr = line.split(" ");
				domain = new InDataDomain();
				long frms_trans_time = 0;
				frms_trans_time = convertDate(arr[0].trim());
				if (frms_trans_time <= 0) {
					line = brIn.readLine();
					continue;
				}
				try {
					domain.setFrms_trans_time(frms_trans_time);
					domain.setFrms_url(arr[1].trim());
					domain.setFrms_ip_cdn(arr[2].trim());
					domain.setFrms_ip_user(arr[3].trim());
					domain.setUser_name(arr[4].trim());
					// 调用接口
					call(resultFileWriter, exceptionFileWriter, domain, line);
				} catch (Exception e) {
					System.out.println("line--->" + line);
				}
				// 继续读取输入流的数据
				line = brIn.readLine();
			}
			// 关闭
			// brIn.close();
			// resultFileWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 调用邦盛服务
	 * 
	 * @param domain
	 *            入参是拼接json串的几个属性
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void call(FileWriter resultFileWriter,
			FileWriter exceptionFileWriter, InDataDomain domain,
			String inputLine) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		String uuid = null;
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

			uuid = getUUID();
			// uuid = Long.toString(idWorker.nextId());
			// StringBuffer sb = new StringBuffer();
			// sb.append("[{ ")
			// .append("\"@type\":\"cn.com.bsfit.frms.obj.AuditObject\"")
			// // .append(",\"frms_biz_code\":\"PAY.QUERY\"")
			// .append(",\"frms_biz_code\":\"PAY.REG\"")
			// .append(",\"frms_ip\":")
			// .append("\"" + domain.getFrms_ip_user() + "\"")
			// .append(",\"frms_ip_cdn\":")
			// .append("\"" + domain.getFrms_ip_cdn() + "\"")
			// .append(",\"frms_trans_time\":")
			// .append(domain.getFrms_trans_time())
			// .append(",\"frms_url\":")
			// .append("\"" + domain.getFrms_url() + "\"")
			// .append(",\"frms_uuid\":").append("\"" + uuid + "\"")
			// .append(",\"frms_user_id\":").append("\"" + domain.getUser_name()
			// + "\"")
			// .append("}]");
			// out.print(sb.toString());

			List list = new ArrayList();
			JSONObject obj = new JSONObject();
			obj.put("@type", "cn.com.bsfit.frms.obj.AuditObject");
			obj.put("frms_uuid", uuid);
			obj.put("frms_ip", domain.getFrms_ip_user());
			obj.put("frms_ip_cdn", domain.getFrms_ip_cdn());
			obj.put("frms_biz_code", "PAY.REG");
			obj.put("frms_url", domain.getFrms_url());
			obj.put("frms_user_id", domain.getUser_name());
			obj.put("frms_trans_time", domain.getFrms_trans_time());
			list.add(obj);
			out.print(list.toString());

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
					if (!"200".equals(cc.trim())) {
						writeExceptionLogFile(exceptionFileWriter, uuid,
								inputLine, "-3", new Exception());
						continue;
					}
					JSONArray d = (JSONArray) c.get("risks");
					JSONObject e = (JSONObject) d.get(0);
					// 2016.04.14 拿ruleName里的“规则名称”，是数组第2个元素
					// logFileString = ((String) e.get("ruleName")).substring(0,
					// 1);
					logFileString = (((String) e.get("ruleName")).split(":"))[1]
							.trim();
				} catch (Exception e) {
					logFileString = "0";
				}

			}
			// 写文件记录日志
			writeResultFile(resultFileWriter, uuid, inputLine, logFileString);
		} catch (Exception e) {
			// System.out.println("发送 POST请求出现异常！" + e);
			writeExceptionLogFile(exceptionFileWriter, uuid, inputLine, "-2", e);
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
	 * 日期格式转换
	 * 
	 * @param dateStr
	 *            yyyyMMddHHmmss类型的时间
	 * @return 返回long型的时间
	 */
	private long convertDate(String dateStr) {

		if (dateStr.length() == 15 && "0".equals(dateStr.startsWith("0")))
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
	 * 将查询流立方的结果写入文件
	 * 
	 * @param fw
	 *            FileWriter
	 * @param uuid
	 *            UUID
	 * @param request
	 *            查询请求
	 * @param response
	 *            流立方返回的结果
	 */
	private void writeResultFile(FileWriter fw, String uuid, String request,
			String response) {
		try {
			fw.write(generateWriteFileString(uuid, request, response));
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将查询流立方的异常信息写入文件
	 * 
	 * @param fw
	 *            FileWriter
	 * @param uuid
	 *            UUID
	 * @param request
	 *            查询请求
	 * @param response
	 *            流立方返回的结果
	 */
	private void writeExceptionLogFile(FileWriter fw, String uuid,
			String request, String response, Exception exception) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(getCurrentTime() + " ").append(request).append(" ")
					.append(response);

			fw.write(sb.toString() + " exception:" + exception.toString()
					+ "\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成写结果文件的字符串
	 * 
	 * @param uuid
	 *            UUID
	 * @param request
	 *            查询请求
	 * @param response
	 *            流立方返回的结果
	 * @return 写结果文件的字符串
	 */
	private String generateWriteFileString(String uuid, String request,
			String response) {
		StringBuffer sb = new StringBuffer();
		sb.append(getCurrentTime() + " ").append(request).append(" ")
				.append(response).append("\n");
		return sb.toString();
	}

}
