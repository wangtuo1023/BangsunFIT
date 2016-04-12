package bangsun.bean;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

public class Verifypolicy {

	private String code;
	@JsonProperty("failControl")
	private String failcontrol;
	private String name;
	private int priority;
	@JsonProperty("succControl")
	private String succcontrol;

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setFailcontrol(String failcontrol) {
		this.failcontrol = failcontrol;
	}

	public String getFailcontrol() {
		return failcontrol;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public void setSucccontrol(String succcontrol) {
		this.succcontrol = succcontrol;
	}

	public String getSucccontrol() {
		return succcontrol;
	}

}