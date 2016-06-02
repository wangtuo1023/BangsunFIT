package kafka.operation;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by blake on 6/1/16.
 */
public class KafkaConsumer {

	public KafkaConsumer() {
		super();
	}

	public void run() {
		String topic = "matua.source";
		ConsumerConnector consumer = createConsumer();
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, 1); // 一次从主题中获取一个数据
		Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumer
				.createMessageStreams(topicCountMap);
		KafkaStream<byte[], byte[]> stream = messageStreams.get("matua.source")
				.get(0);// 获取每次接收到的这个数据
		ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
		while (iterator.hasNext()) {
			String message = new String(iterator.next().message());
			process(message);
//			System.out.println("接收到: " + message);
		}
	}

	private ConsumerConnector createConsumer() {
		Properties properties = new Properties();
		// properties.put("zookeeper.connect", "198.218.6.166:2181");// 声明zk
		properties.put("zookeeper.connect", "10.2.234.3:2181");// 声明zk
		properties.put("group.id", "test-consumer-group");// 组名称，consumer.properties中的默认值
		return Consumer.createJavaConsumerConnector(new ConsumerConfig(
				properties));
	}

	public static void main(String[] args) {
		new KafkaConsumer().run();// 使用kafka集群中创建好的主题 test

	}

	private void process(String message) {

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
				stringBuffer.append(message.replaceAll(",", "##"));
			} else if (v1Arr1.length < 13) {
				// log.warn(v1.message());
				stringBuffer.append(message.replaceAll(",", "##"));
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
					stringBuffer.append(v1Arr1[j]).append("##");
				}
				if (stringBuffer.length() > 2) {
					stringBuffer.deleteCharAt(stringBuffer.length() - 1);
				}
			}
		}

		System.out.println(">>>>> " + stringBuffer.toString());
		
	}

}
