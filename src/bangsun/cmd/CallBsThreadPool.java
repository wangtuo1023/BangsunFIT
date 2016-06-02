package bangsun.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import bangsun.bean.IdWorker;
import bangsun.bean.InDataDomain;

public class CallBsThreadPool implements Runnable{

	private String writeFilePath = null;
	private IdWorker idWorker = IdWorker.getFlowIdWorkerInstance();

	public static void main(String[] args) {
		BufferedReader brIn = new BufferedReader(new InputStreamReader(
				System.in));
		CallBsThreadPool bs = new CallBsThreadPool(args[0]);
		bs.controller(brIn);
	}

	@Override
	public void run() {
		
	}
	
	
	public CallBsThreadPool(String writeFiltePath) {
		this.writeFilePath = writeFiltePath;
	}

	private void controller(BufferedReader brIn) {
		// 声明数据结构domain
		InDataDomain domain = null;
		try {
			// 声明文件：调用完记日志的文件
			File logFile = new File(writeFilePath);
			FileWriter fw = new FileWriter(logFile);

			// 数据源：标准输入流
			String line = brIn.readLine();
			while (line != null) {
				// 解析文件，放到domain
				String[] arr = line.split(" ");
				domain = new InDataDomain();
				domain.setFrms_trans_time(convertDate(arr[0].trim()));
				domain.setFrms_url(arr[1].trim());
				domain.setFrms_ip_cdn(arr[2].trim());
				domain.setFrms_ip_user(arr[3].trim());
				// 调用接口
				call(fw, domain);
				// 继续读取输入流的数据
				line = brIn.readLine();
			}
			// 关闭
			brIn.close();
			fw.close();

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
	private void call(FileWriter fw, InDataDomain domain) {
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
					.append(",\"frms_biz_code\":\"PAY.QUERY\"")
					.append(",\"frms_ip\":")
					.append("\"" + domain.getFrms_ip_user() + "\"")
					.append(",\"frms_ip_cdn\":")
					.append("\"" + domain.getFrms_ip_cdn() + "\"")
					.append(",\"frms_trans_time\":")
					.append(domain.getFrms_trans_time())
					.append(",\"frms_url\":")
					.append("\"" + domain.getFrms_url() + "\"")
					.append(",\"frms_uuid\":").append("\"" + uuid + "\"")
					.append("}]");
			out.print(sb.toString());
			out.flush();
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line = null, logFileString = null;
			while ((line = in.readLine()) != null) {
				result += line;
				// 解析json
				JSONArray b = JSONArray.parseArray(result);
				JSONObject c = (JSONObject) b.get(0);
				try {
					JSONArray d = (JSONArray) c.get("risks");
					JSONObject e = (JSONObject) d.get(0);
					logFileString = String.format("s%,s%,s%,s%,s%",
							domain.getFrms_ip_user(), domain.getFrms_url(),
							e.get("uuid"), e.get("createTime"),
							((String) e.get("ruleName")).substring(0, 1));
				} catch (Exception e) {
					// e.printStackTrace();
				}

			}
			// 写文件记录日志
			writeFile(fw, uuid, sb.toString(), logFileString);
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
	 * 日期格式转换
	 * 
	 * @param dateStr
	 *            yyyyMMddHHmmss类型的时间
	 * @return 返回long型的时间
	 */
	private long convertDate(String dateStr) {
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
	private void writeFile(FileWriter fw, String uuid, String request,
			String response) {
		try {
			fw.write(generateWriteFileString(uuid, request, response));
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
		sb.append(getCurrentTime() + "Request >>> ").append(request)
				.append("\n").append(getCurrentTime() + "Response >>> ")
				.append(response).append("\n");
		return sb.toString();
	}


}
