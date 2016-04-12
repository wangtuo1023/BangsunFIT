package bangsun.bean;

import org.codehaus.jackson.annotate.JsonProperty;

//import org.codehaus.jackson.annotate.JsonIgnoreProperties;
//import org.codehaus.jackson.annotate.JsonProperty;

public class Risks {

	private String comments;
	@JsonProperty("createTime")
	private int createtime;
	private Customization customization;
	@JsonProperty("notifyPolicy")
	private Notifypolicy notifypolicy;
	@JsonProperty("ruleName")
	private String rulename;
	@JsonProperty("rulePackageName")
	private String rulepackagename;
	private int score;
	private String uuid;
	@JsonProperty("verifyPolicy")
	private Verifypolicy verifypolicy;
	private int weight;

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getComments() {
		return comments;
	}

	public void setCreatetime(int createtime) {
		this.createtime = createtime;
	}

	public int getCreatetime() {
		return createtime;
	}

	public void setCustomization(Customization customization) {
		this.customization = customization;
	}

	public Customization getCustomization() {
		return customization;
	}

	public void setNotifypolicy(Notifypolicy notifypolicy) {
		this.notifypolicy = notifypolicy;
	}

	public Notifypolicy getNotifypolicy() {
		return notifypolicy;
	}

	public void setRulename(String rulename) {
		this.rulename = rulename;
	}

	public String getRulename() {
		return rulename;
	}

	public void setRulepackagename(String rulepackagename) {
		this.rulepackagename = rulepackagename;
	}

	public String getRulepackagename() {
		return rulepackagename;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setVerifypolicy(Verifypolicy verifypolicy) {
		this.verifypolicy = verifypolicy;
	}

	public Verifypolicy getVerifypolicy() {
		return verifypolicy;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}

}