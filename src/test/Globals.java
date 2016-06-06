package test;

public class Globals {

    // 维度
    /**
     * 用户号 or 账户号
     **/
    public static final String TAG_USER = "u";
    /**
     * 银行卡号
     **/
    public static final String TAG_CARD_NO = "c";
    /**
     * 证件号
     **/
    public static final String TAG_ID_NO = "idno";
    /**
     * 手机号
     **/
    public static final String TAG_PHONE = "phone";
    /**
     * IP地址
     **/
    public static final String TAG_IP = "ip";
    /**
     * 用户姓名
     **/
    public static final String TAG_USER_NAME = "uname";
    /**
     * 设备指纹
     **/
    public static final String TAG_FINGER_PRINT = "fp";
    /**
     * 商户号
     **/
    public static final String TAG_MCH_ID = "m";
    /**
     * 终端号
     **/
    public static final String TAG_TERMINAL_ID = "t";
    public static final String TAG_SESSION_ID = "sid";//SessionId


    /**
     * 名单
     **/
    public static final String TAG_NAME_LIST = "namelist";

    /**
     * 名单专用业务类型
     **/
    public static final String BIZ_NL = "NL";

    // BIZ
    public static final String BIZ_PAY_All = "PAY.ALL";
    public static final String BIZ_PAY_QUERY = "PAY.QUERY";
    public static final String BIZ_PAY_REG = "PAY.REG";
    public static final String BIZ_PAY_LOGIN = "PAY.LOGIN";
    public static final String BIZ_PAY_BUY = "PAY.BUY";
    public static final String BIZ_PAY_OTHER = "PAY.OTHER";

    //引擎返回状态码
    public static final String ENG_RET_CODE = "frms_ret_code";//返回状态
    public static final String ENG_RET_CODE_SUS = "200";//正常状态
    public static final String ENG_RET_CODE_ERR_INT = "500";//内部错误
    public static final String ENG_RET_CODE_ERR_FORMAT = "600";//数据格式错误

    //客户数据的所有url
    public static final String URL_ALL_GET_PASS_CODE = "/otn/passcodeNew/getPassCodeNew";
    public static final String URL_QUERY_LOG = "/otn/leftTicket/log";
    public static final String URL_QUERY_QUERY = "/otn/leftTicket/query";
    public static final String URL_REG_CHECK = "/otn/regist/checkUserName";
    public static final String URL_REG_GET = "/otn/regist/getRandCode";
    public static final String URL_REG_SUB = "/otn/regist/subDetail";
    public static final String URL_LOGIN_LOGIN = "/otn/login/loginAysnSuggest";
    public static final String URL_LOGIN_CHECK = "/otn/login/checkUser";
    public static final String URL_BUY_SUB = "/otn/leftTicket/submitOrderRequest";
}

