package bangsun.bean;

public class InDataDomain {

	String frms_ip_user;
	String frms_ip_cdn;
	long frms_trans_time;
	String frms_url;
	String user_name;

	public String getFrms_ip_user() {
		return frms_ip_user;
	}
	public void setFrms_ip_user(String frms_ip_user) {
		this.frms_ip_user = frms_ip_user;
	}
	public String getFrms_ip_cdn() {
		return frms_ip_cdn;
	}
	public void setFrms_ip_cdn(String frms_ip_cdn) {
		this.frms_ip_cdn = frms_ip_cdn;
	}

	public long getFrms_trans_time() {
		return frms_trans_time;
	}
	public void setFrms_trans_time(long frms_trans_time) {
		this.frms_trans_time = frms_trans_time;
	}
	public String getFrms_url() {
		return frms_url;
	}
	public void setFrms_url(String frms_url) {
		this.frms_url = frms_url;
	}
	
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	@Override
	public String toString() {
		return "InDataDomain [frms_ip_user=" + frms_ip_user + ", frms_ip_cdn="
				+ frms_ip_cdn + ", frms_trans_time=" + frms_trans_time
				+ ", frms_url=" + frms_url + "]";
	}

}
