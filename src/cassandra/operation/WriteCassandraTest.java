package cassandra.operation;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class WriteCassandraTest {

	public static void main(String[] args) {
		WriteCassandraTest w = new WriteCassandraTest();
		Cluster cluster = Cluster.builder().addContactPoint("10.1.199.81")
				.build();
		Session session = cluster.connect("bangsun_risk_info");
		w.query(session);
		session.close();
		cluster.close();
	}


	private void query(Session session) {

		// session.execute("use bangsun_risk_info");
		// session.execute("INSERT INTO user_info (user_name,sex,age) VALUES ('张龙','男','35')");

		ResultSet results = session.execute("SELECT * FROM user_info");
		for (Row row : results) {
			System.out.format("%s %s %s \n", row.getString("user_name"),
					row.getString("sex"), row.getString("age"));
		}
	}

}
