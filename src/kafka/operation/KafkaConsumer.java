package kafka.operation;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.catalina.util.RequestUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import test.Globals;

import cn.com.bsfit.frms.obj.AuditObject;
import cn.com.bsfit.frms.obj.AuditResult;
import cn.com.bsfit.frms.obj.Risk;

public class KafkaConsumer {

	public static void main(String[] args) {
		new KafkaConsumer().run();// 使用kafka集群中创建好的主题 test

	}

	public KafkaConsumer() {
		super();
	}

	/**
	 * 启动程序
	 */
	public void run() {
		// 1. 初始化Kafka消费端
		String topic = "matua.source";
		ConsumerConnector consumer = createConsumer();
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, 1); // 一次从主题中获取一个数据
		Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumer
				.createMessageStreams(topicCountMap);
		KafkaStream<byte[], byte[]> stream = messageStreams.get("matua.source")
				.get(0);
		// 2. 得到消费数据，
		ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
		while (iterator.hasNext()) {
			// 3. 正则解析消息
			String originalMessage = new String(iterator.next().message());
			String processedMessage = processMessage(originalMessage);
			// 4. 转换为流立方引擎的AuditObject对象，并调用
			if (null != processedMessage && !"".equals(processedMessage)) {
				AuditObject ao = convertMessageToAuditObject(processedMessage);
				if (null != ao) {
					BufferedReader in = callStreamCube(ao, processedMessage);
					// 5.处理流立方结果
					boolean isOk = processStreamCubeResult(in);
					// 不管ok不ok，都要取下一条数据
					// if(!isOk)
					// continue;
				} else
					continue;
			} else
				continue;
			// System.out.println("接收到: " + message);
		}
	}

	/**
	 * 初始化Kafka消费端Connector
	 * 
	 * @return ConsumerConnector
	 */
	private ConsumerConnector createConsumer() {
		Properties properties = new Properties();
		// properties.put("zookeeper.connect", "198.218.6.166:2181");// 声明zk
		// 声明zk
		properties.put("zookeeper.connect", "10.2.234.3:2181");
		// 组名称，consumer.properties中的默认值
		properties.put("group.id", "test-consumer-group");
		return Consumer.createJavaConsumerConnector(new ConsumerConfig(
				properties));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private BufferedReader callStreamCube(AuditObject ao,
			String processedMessage) {
		PrintWriter out = null;
		BufferedReader in = null;
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
			// out.print(ao.toString());
			List list = new ArrayList();
			list.add(ao);
			out.print(JSON.toJSONString(list, SerializerFeature.WriteClassName)
					.toString());

			out.flush();

			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			return in;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	}

	
	private boolean processStreamCubeResult(BufferedReader in) {
		boolean processAck = true;
		String line = null, log = "", result = "";
		if (null == in) {
			processAck = false;
			return processAck;
		}
		try {
			while ((line = in.readLine()) != null) {
				result += line;
				// 解析json
				JSONArray b = JSONArray.parseArray(result);
				AuditResult auditResult = (AuditResult) b.get(0);
				String retCode = auditResult.getRetCode();
				if (!"200".equals(retCode.trim())) {
					continue;
				}
				List<Risk> risks = auditResult.getRisks();
				if (risks.size() == 0) {
					continue;
				}
				for (Risk risk : risks) {
					log += risk.getRuleName().split(":")[1].trim() + ",";
				}
//				System.out.println("=== " + log);
			}
		} catch (Exception e) {
			e.printStackTrace();
			processAck = false;
			return processAck;
		}
		return processAck;
	}
	
	private AuditObject convertMessageToAuditObject(String message) {
		// ao对象，每次都要new个新的。
		AuditObject ao = new AuditObject();
		String[] arr = message.split("\t");

		ao.setUuid(getUUID());

		if (arr.length < 12) {
			// writeExceptionLogFile(exceptionFileWriter, null, line, null,
			// new Exception("数据格式不对(长度)"));
			// continue;
			return null;
		}
		String dateStr = arr[2].trim();
		if (dateStr.length() != 14)
			return null;
		Date frms_trans_time = convertDate(dateStr);
		ao.setTransTime(frms_trans_time);
		ao.setUserId(arr[3].trim()); // 用户名
		ao.put("frms_session_id", arr[4].trim());
		String url = arr[5].trim();
		ao.put("frms_url", url);
		String params = arr[6].trim();
		ao.put("frms_params", params); // 请求参数
		ao.put("frms_ip_cdn", arr[7]); // CDN ip
		ao.put("frms_ip", arr[8]); // 用户ip
		ao.put("frms_referer", arr[9]);
		// ao.put("frms_cookie",arr[10]); // 预留
		String cdnInfo = arr[11].trim();
		String[] cdnArr = cdnInfo.split(":");
		if (cdnArr.length >= 2)
			ao.put("frms_cdn", cdnArr[0].substring(3, cdnArr[0].length()));
		// 用户端口号不一定有
		try {
			if (arr[12] != null)
				ao.put("frms_port", arr[12]);
			else
				ao.put("frms_port", "");
		} catch (Exception e) {
			e.printStackTrace();
			ao.put("frms_port", "");
		}

		Map<String, String[]> values = new HashMap<>();
		if (params.length() > 1) {
			String temp = params.substring(0, params.length() - 1);
			ao.put("frms_params", temp);
			RequestUtil.parseParameters(values, temp, "UTF-8");
		}

		switch (url) {
		case Globals.URL_ALL_GET_PASS_CODE:
			if ("login".equals(getSingleValue(values, "module"))) {
				ao.put("frms_trade_mode", "2");
				ao.setBizCode(Globals.BIZ_PAY_LOGIN);
			} else {
				ao.setBizCode(Globals.BIZ_PAY_BUY);
			}
			break;
		case Globals.URL_QUERY_LOG:
			ao.put("frms_trade_mode", "1");
			ao.setBizCode(Globals.BIZ_PAY_QUERY);
			break;
		case Globals.URL_QUERY_QUERY:
			ao.put("frms_trade_mode", "2");
			ao.setBizCode(Globals.BIZ_PAY_QUERY);
			break;
		case Globals.URL_REG_CHECK:
			ao.put("frms_trade_mode", "1");
			ao.setBizCode(Globals.BIZ_PAY_REG);
			ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
			break;
		case Globals.URL_REG_GET:
			ao.put("frms_trade_mode", "2");
			ao.setBizCode(Globals.BIZ_PAY_REG);
			ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
			break;
		case Globals.URL_REG_SUB:
			ao.put("frms_trade_mode", "3");
			ao.setBizCode(Globals.BIZ_PAY_REG);
			ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
			break;
		case Globals.URL_LOGIN_CHECK:
			ao.put("frms_trade_mode", "1");
			ao.setBizCode(Globals.BIZ_PAY_BUY);
			break;
		case Globals.URL_BUY_SUB:
			ao.put("frms_trade_mode", "2");
			ao.setBizCode(Globals.BIZ_PAY_BUY);
			break;
		case Globals.URL_LOGIN_LOGIN:
			ao.put("frms_trade_mode", "1");
			ao.setBizCode(Globals.BIZ_PAY_LOGIN);
			ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
			break;
		// case "/otn/":
		// writeExceptionLogFile(exceptionFileWriter, null, line, null, new
		// Exception("不需要调引擎的数据"));
		// continue;
		default:
			ao.put("frms_trade_mode", "99");
			ao.setBizCode(Globals.BIZ_PAY_OTHER);
			break;
		}

		return ao;
	}

	private String getSingleValue(Map<String, String[]> params, String key) {
		String[] values = params.get(key);
		if (values != null && values.length > 0) {
			return values[0];
		}
		return "";
	}

	/**
	 * 自动生成UUID
	 * 
	 * @return 返回UUID
	 */
	private String getUUID() {
		return UUID.randomUUID().toString();
	}

	private Date convertDate(String dateStr) {
		Date dt = null;
		try {
			SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyyMMddHHmmss");
			dt = YYYY_MM_DD.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dt;
	}


	private String processMessage(String message) {

		System.out.println(message);
		StringBuffer stringBuffer = new StringBuffer();
		if (message.matches(".*\\|;.*")) {
			String[] v1Arr1 = message.split("\\|;");
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < v1Arr1.length; i++) {
				if (i != 10 && i != 12 && i != 13 && i != 14 && i != 17) {
					stringBuffer.append(v1Arr1[i] + "\t");
				} else {
					buffer.append(v1Arr1[i] + "\t");
				}
			}
			stringBuffer.append(buffer);
		} else {
			String[] v1Arr1 = message.split(",");
			if (v1Arr1.length == 13) {
				stringBuffer.append(message.replaceAll(",", "\t"));
			} else if (v1Arr1.length < 13) {
				// log.warn(v1.message());
				stringBuffer.append(message.replaceAll(",", "\t"));
			} else if (v1Arr1.length > 13) {
				// log.error(v1.message());
				int dealIndex = 6;
				StringBuffer sBuf = new StringBuffer();
				for (int i = 0; i < dealIndex; i++) {
					sBuf.append(v1Arr1[i]).append(",");
				}
				int i = dealIndex;
				while (i < v1Arr1.length && !v1Arr1[i].endsWith("&")) {
					sBuf.append(v1Arr1[i]).append(".");
					i++;
				}
				for (; i < v1Arr1.length; i++) {
					sBuf.append(v1Arr1[i]).append(",");
				}
				sBuf.deleteCharAt(sBuf.length() - 1);
				v1Arr1 = sBuf.toString().split(",");
				for (int j = 0; j < v1Arr1.length; j++) {
					stringBuffer.append(v1Arr1[j]).append("\t");
				}
				if (stringBuffer.length() > 2) {
					stringBuffer.deleteCharAt(stringBuffer.length() - 1);
				}
			}
		}

		// System.out.println(">>>>> " + stringBuffer.toString());
		return stringBuffer.toString();
	}

}
