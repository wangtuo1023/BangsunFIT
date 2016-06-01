package cassandra.operation;

import java.util.HashSet;
import java.util.Set;


import cassandra.log.LogUtil;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraOperationDemo {

	private Cluster cluster;
	private Session session;

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	private void connect(String node) {
		cluster = Cluster.builder().addContactPoint(node).build();
		Metadata metadata = cluster.getMetadata();

		// TODO: log
		LogUtil.getCassandraSystemLog().info("Connetcted to cluster : %s\n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			LogUtil.getCassandraSystemLog().info(
					"  DataCenter: %s\n  Hosts: %s\n  Rack:%s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}

		this.session = cluster.connect();

	}

	private void insertData() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO bangsun_risk_info.user_info")
				.append("( user_name, sex, age , emails , mobile_no )")
				.append("VALUES ( ?,?,?,?,? );");
		PreparedStatement insertStatement = getSession().prepare(sb.toString());
		BoundStatement boundStatement = new BoundStatement(insertStatement);
		Set<String> emails = new HashSet<String>();
		emails.add("wangtuo@163.com");
		emails.add("wangtuo@sohu.com");

		getSession().execute(
				boundStatement.bind("测试1", "女", "18", "ceshi@sina.com",
						"18699999999"));

	}
	
	
	private void loadData() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM bangsun_risk_info.user_info");
		ResultSet resultSet = getSession().execute(sb.toString());

		System.out.println(String.format("%-10s\t%-4s\t%-4s\t%-25s\t%-11s\n%s",
				"姓名", "性别", "年龄", "邮箱", "手机号码",
				"----------|----|----|-------------------------|-----------"));

		for (Row row : resultSet) {
			System.out.println(String.format(
					"%-10s\t%-4s\t%-4s\t%-25s\t%-11s\n",
					row.getString("user_name"), row.getString("sex"),
					row.getString("age"), row.getString("emails"),
					row.getString("mobile_no")));
		}
	}
	
	
	private void close(){
		cluster.close();
	}
	
	
	
	public static void main(String[] args) {
		CassandraOperationDemo c = new CassandraOperationDemo();
		c.connect("10.1.199.81");
		c.insertData();
		c.loadData();
		c.session.close();
		c.close();
	}

}
