//package kafka.operation;
//
//import kafka.common.TopicAndPartition;
//import kafka.message.MessageAndMetadata;
//import kafka.serializer.StringDecoder;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FSDataOutputStream;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.spark.SparkConf;
//import org.apache.spark.SparkContext;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.function.Function;
//import org.apache.spark.api.java.function.VoidFunction;
//import org.apache.spark.streaming.Duration;
//import org.apache.spark.streaming.StreamingContext;
//import org.apache.spark.streaming.api.java.JavaInputDStream;
//import org.apache.spark.streaming.api.java.JavaStreamingContext;
//import org.apache.spark.streaming.kafka.HasOffsetRanges;
//import org.apache.spark.streaming.kafka.KafkaCluster;
//import org.apache.spark.streaming.kafka.KafkaUtils;
//import org.apache.spark.streaming.kafka.OffsetRange;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import scala.Predef;
//import scala.Tuple2;
//import scala.collection.JavaConversions;
//
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * 本地测试：
// * ${SPARK_HOME}/bin/spark-submit --master local[2] \
// --class com.liuc.EtlJob /home/blake/idea-IC-141.1532.4/IdeaProjects/behaviorAnalysis/logstream/target/logstream-1.2.jar \
// local[2] blake:9092 my-consumer-group test3 /hive/warehouse 5000 0
// * Created by blake on 6/1/16.
// */
//public class SparkConsumer {
//    private static Logger log = LoggerFactory.getLogger(EtlJob.class);
//
////	private static String hiveRoot = "/hive/warehouse";
//
//    /**
//     * process_log : 只是PC端otn的processLog
//     * process_log_pm:包括PC端和mobile端的processlog
//     */
//    private static String process_log = "/process_log_pm";
//
//    public static void main(String[] args) throws IOException {
//
//        /**
//         */
//        if (args.length < 5) {
//            System.err.println("Usage: EtlJob <master> <brokers> <group> <topics> <hive_root> <duration> <offset_lag>");
//            System.exit(1);
//        }
//        String master = args[0];
//        String brokers = args[1];
//        String group = args[2];
//        String topics = args[3];
//        final String hdfs = args[4];//hive_root
//        String duration = args[5];
//        //如果从未读队列消息的某一段开始读，传负数。
//        //比如：各个partition的lag值为100，如果要从队尾开始读，则此处要传 -100.如果从一半位置开始读，传 -50
//        Long offset_lag = 0L;
//        try{
//            offset_lag = Long.valueOf(args[6]);
//        }catch(NumberFormatException e){
//            log.warn("传入offset_lag值不合法，使用默认的0。");
//        }
//
//        SparkConf conf = new SparkConf().setAppName("etljob");
//        conf.setMaster(master);
//        SparkContext sc = new SparkContext(conf);
//
//        StreamingContext jss = new StreamingContext(sc, new Duration(Long.valueOf(duration)));
//        JavaStreamingContext jssc = new JavaStreamingContext(jss);
//
//        java.util.Map<kafka.common.TopicAndPartition, Long> fromOffsets = new java.util.HashMap<kafka.common.TopicAndPartition, Long>();
//        final AtomicReference<OffsetRange[]> offsetRanges = new AtomicReference<OffsetRange[]>();
//
//        HashSet<String> topicsSet = new HashSet<String>(Arrays.asList(topics.split(",")));
//        final HashMap<String, String> kafkaParams = new HashMap<String, String>();
//        kafkaParams.put("metadata.broker.list", brokers);
//        kafkaParams.put("group.id", group);
//        scala.collection.mutable.Map<String, String> mutableKafkaParam = JavaConversions.mapAsScalaMap(kafkaParams);
//        scala.collection.immutable.Map<String, String> immutableKafkaParam = mutableKafkaParam
//                .toMap(new Predef.$less$colon$less<Tuple2<String, String>, Tuple2<String, String>>() {
//                    public Tuple2<String, String> apply(Tuple2<String, String> v1) {
//                        return v1;
//                    }
//                });
//        final KafkaCluster kafkaCluster = new KafkaCluster(immutableKafkaParam);
//        scala.collection.mutable.Set<String> mutableTopics = JavaConversions.asScalaSet(topicsSet);
//        scala.collection.immutable.Set<String> immutableTopics = mutableTopics.toSet();
//        scala.collection.immutable.Set<TopicAndPartition> scalaTopicAndPartitionSet = kafkaCluster
//                .getPartitions(immutableTopics).right().get();
//
//        if (kafkaCluster.getConsumerOffsets(kafkaParams.get("group.id"), scalaTopicAndPartitionSet).isLeft()) {
//            Set<TopicAndPartition> javaTopicAndPartitionSet = JavaConversions.setAsJavaSet(scalaTopicAndPartitionSet);
//            for (TopicAndPartition topicAndPartition : javaTopicAndPartitionSet) {
//                fromOffsets.put(topicAndPartition, offset_lag);
//
//            }
//        } else {
//            scala.collection.immutable.Map<TopicAndPartition, Object> consumerOffsetsTemp = kafkaCluster
//                    .getConsumerOffsets(kafkaParams.get("group.id"), scalaTopicAndPartitionSet).right().get();
//
//            Map<TopicAndPartition, Object> consumerOffsets = JavaConversions.mapAsJavaMap(consumerOffsetsTemp);
//            Set<TopicAndPartition> javaTopicAndPartitionSet = JavaConversions.setAsJavaSet(scalaTopicAndPartitionSet);
//            for (TopicAndPartition topicAndPartition : javaTopicAndPartitionSet) {
//                Long offset = (Long) consumerOffsets.get(topicAndPartition);
//                fromOffsets.put(topicAndPartition, offset - offset_lag);
//            }
//        }
//        // Create direct kafka stream with brokers and
//        // topicsscala.collection.mutable.Set<String> mutableTopics =
//        // JavaConversions
//        JavaInputDStream<String> messages = KafkaUtils.createDirectStream(jssc, String.class, String.class,
//                StringDecoder.class, StringDecoder.class, String.class, kafkaParams, fromOffsets,
//                new Function<MessageAndMetadata<String, String>, String>() {
//                    public String call(MessageAndMetadata<String, String> v1) throws Exception {
//                        StringBuffer stringBuffer = new StringBuffer();
//                        stringBuffer.append(v1.partition()).append("\001");
//                        if (v1.message().matches(".*\\|;.*")) {
//                            String[] v1Arr1 = v1.message().split("\\|;");
//                            StringBuffer buffer = new StringBuffer();
//                            for (int i = 0; i < v1Arr1.length; i++) {
//                                if (i != 10 && i != 12 && i != 13 && i != 14 && i != 17) {
//                                    stringBuffer.append(v1Arr1[i] + "\t");
//                                } else {
//                                    buffer.append(v1Arr1[i] + "\t");
//                                }
//                            }
//                            stringBuffer.append(buffer);
//                        } else {
//                            String[] v1Arr1 = v1.message().split(",");
//                            if (v1Arr1.length == 13) {
//                                stringBuffer.append(v1.message().replaceAll(",", "\t"));
//                            } else if (v1Arr1.length < 13) {
//                                //                            log.warn(v1.message());
//                                stringBuffer.append(v1.message().replaceAll(",", "\t"));
//                            } else if (v1Arr1.length > 13) {
//                                //                            log.error(v1.message());
//                                int dealIndex = 6;
//                                StringBuffer sBuf = new StringBuffer();
//                                for (int i = 0; i < dealIndex; i++) {
//                                    sBuf.append(v1Arr1[i]).append(",");
//                                }
//                                int i = dealIndex;
//                                while (i < v1Arr1.length && !v1Arr1[i].endsWith("&")) {
//                                    sBuf.append(v1Arr1[i]).append(".");
//                                    i++;
//                                }
//                                for (; i < v1Arr1.length; i++) {
//                                    sBuf.append(v1Arr1[i]).append(",");
//                                }
//                                sBuf.deleteCharAt(sBuf.length() - 1);
//                                v1Arr1 = sBuf.toString().split(",");
//                                for (int j = 0; j < v1Arr1.length; j++) {
//                                    stringBuffer.append(v1Arr1[j]).append("\t");
//                                }
//                                if (stringBuffer.length() > 2) {
//                                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
//                                }
//                            }
//                        }
//                        return stringBuffer.toString();
////						return v1.partition() + "\t" + v1.message();
//                    }
//                });
//        messages.foreachRDD(new Function<JavaRDD<String>, Void>() {
//
//            public Void call(JavaRDD<String> rdd) throws Exception {
//                rdd.foreachPartition(new VoidFunction<Iterator<String>>() {
//                    @Override
//                    public void call(Iterator<String> lines) throws Exception {
//                        Configuration config = new Configuration();
//                        FileSystem fs = null;
//                        SimpleDateFormat day = new SimpleDateFormat("yyyyMMdd");
//                        SimpleDateFormat hour = new SimpleDateFormat("HH");
//                        Date date = new Date();
//                        FSDataOutputStream fin = null;
//                        String path = hdfs + process_log + "/date=" + day.format(date) + "/hour=" + hour.format(date) + "/process.log";
//                        try {
//                            if (lines.hasNext()) {
//                                String ls[] = lines.next().split("\001");
//                                fs = FileSystem.get(config);
//                                Path filenamePath = new Path(path + "_" + ls[0]);
//                                if (!fs.exists(filenamePath)) {
//                                    fin = fs.create(filenamePath);
//                                } else {
//                                    fin = fs.append(filenamePath);
//                                }
//                                fin.write(ls[1].trim().getBytes());
//                                fin.write("\n".getBytes());
//                            }
//                            while (lines.hasNext()) {
//                                String ls[] = lines.next().split("\001");
//                                fin.write(ls[1].trim().getBytes());
//                                fin.write("\n".getBytes());
//                            }
//                            if (fin != null) {
//                                fin.hflush();
//                                fin.hsync();
//                            }
//                        } catch (Exception e) {
//                            log.error(e.getMessage(), e);
//                        } finally {
//                            if (fin != null) {
//                                try {
//                                    fin.close();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            if (fs != null) {
//                                try {
//                                    fs.close();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                });
//
//                OffsetRange[] offsets = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
//                for (OffsetRange offsetRange : offsets) {
//                    // 封装topic.partition 与 offset对应关系 java Map
//                    TopicAndPartition topicAndPartition = new TopicAndPartition(offsetRange.topic(), offsetRange
//                            .partition());
//                    Map<TopicAndPartition, Object> topicAndPartitionObjectMap = new HashMap<TopicAndPartition, Object>();
//                    topicAndPartitionObjectMap.put(topicAndPartition, offsetRange.untilOffset());
//
//                    // 转换java map to scala immutable.map
//                    scala.collection.mutable.Map<TopicAndPartition, Object> map = JavaConversions
//                            .mapAsScalaMap(topicAndPartitionObjectMap);
//                    scala.collection.immutable.Map<TopicAndPartition, Object> scalatopicAndPartitionObjectMap = map
//                            .toMap(new Predef.$less$colon$less<Tuple2<TopicAndPartition, Object>, Tuple2<TopicAndPartition, Object>>() {
//                                public Tuple2<TopicAndPartition, Object> apply(Tuple2<TopicAndPartition, Object> v1) {
//                                    return v1;
//                                }
//                            });
//                    // 更新offset到kafkaCluster
//                    kafkaCluster.setConsumerOffsets(kafkaParams.get("group.id"), scalatopicAndPartitionObjectMap);
//                }
//                return null;
//            }
//        });
//
//        jssc.start();
//        jssc.awaitTermination();
//    }
//}
