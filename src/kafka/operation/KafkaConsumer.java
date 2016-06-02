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
		KafkaStream<byte[], byte[]> stream = messageStreams.get("matua.source").get(0);// 获取每次接收到的这个数据
		ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
		while (iterator.hasNext()) {
			String message = new String(iterator.next().message());
			System.out.println("接收到: " + message);
		}
	}

	private ConsumerConnector createConsumer() {
		Properties properties = new Properties();
//		properties.put("zookeeper.connect", "198.218.6.166:2181");// 声明zk
		properties.put("zookeeper.connect", "10.2.234.3:2181");// 声明zk
		properties.put("group.id", "test-consumer-group");// 组名称，consumer.properties中的默认值
		return Consumer.createJavaConsumerConnector(new ConsumerConfig(
				properties));
	}

	public static void main(String[] args) {
		new KafkaConsumer().run();// 使用kafka集群中创建好的主题 test

	}
}
