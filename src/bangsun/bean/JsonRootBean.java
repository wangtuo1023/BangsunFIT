package bangsun.bean;

import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
public class JsonRootBean {

    private String @type;
    private Customization customization;
    private List<String> items;
    @JsonProperty("notifyPolicy")
    private Notifypolicy notifypolicy;
    @JsonProperty("retCode")
    private String retcode;
    private List<Risks> risks;
    private int score;
    private String uuid;
    @JsonProperty("verifyPolicy")
    private Verifypolicy verifypolicy;
    public void set@type(String @type) {
         this.@type = @type;
     }
     public String get@type() {
         return @type;
     }

    public void setCustomization(Customization customization) {
         this.customization = customization;
     }
     public Customization getCustomization() {
         return customization;
     }

    public void setItems(List<String> items) {
         this.items = items;
     }
     public List<String> getItems() {
         return items;
     }

    public void setNotifypolicy(Notifypolicy notifypolicy) {
         this.notifypolicy = notifypolicy;
     }
     public Notifypolicy getNotifypolicy() {
         return notifypolicy;
     }

    public void setRetcode(String retcode) {
         this.retcode = retcode;
     }
     public String getRetcode() {
         return retcode;
     }

    public void setRisks(List<Risks> risks) {
         this.risks = risks;
     }
     public List<Risks> getRisks() {
         return risks;
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

}