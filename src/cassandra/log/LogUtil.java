package cassandra.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

	private final static Logger cassandraOperationLog;
	private final static Logger cassandraSystemLog;
	private final static Logger bangsunSystemLog;

	static {
		cassandraOperationLog = LoggerFactory
				.getLogger("cassandra.operationLog");
		cassandraSystemLog = LoggerFactory.getLogger("cassandra.systemLog");
		bangsunSystemLog = LoggerFactory.getLogger("bangsun.systemLog");
	}

	public static Logger getCassandraOperationLog() {
		return cassandraOperationLog;
	}

	public static Logger getCassandraSystemLog() {
		return cassandraSystemLog;
	}

	public static Logger getBangsunSystemLog() {
		return bangsunSystemLog;
	}

}
