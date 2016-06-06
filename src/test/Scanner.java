package test;

import cn.com.bsfit.frms.obj.AuditObject;
import cn.com.bsfit.frms.obj.AuditResult;
import cn.com.bsfit.frms.obj.Risk;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.catalina.util.RequestUtil;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zqq on 2016/5/3.
 */
public class Scanner {

    private String resultFilePath = null;
    private String exceptionLogFilePath = null;
    private String url;

    public static void main(String[] args) throws Exception {
        BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));
//        File file = new File("D:\\Downloads\\so.log");
//        InputStream fileInputStream = new FileInputStream(file);
//        BufferedReader brIn = new BufferedReader(new InputStreamReader(fileInputStream));
//        Scanner bs = new Scanner("D:\\Downloads\\result.txt", "D:\\Downloads\\exc.txt");
        String resultPath = "./result.log";
        String excPath = "./exc.log";
        String url = "http://10.2.240.100:8686/audit";
        if (args != null) {
            if (args.length >= 2) {
                resultPath = args[0];
                excPath = args[1];
            }
            if (args.length >= 3) {
                url = args[2];
            }
        }
        Scanner scanner = new Scanner(resultPath, excPath, url);
        scanner.scan(brIn);
    }

    public Scanner(String resultFilePath, String exceptionLogFilePath, String url) {
        this.resultFilePath = resultFilePath;
        this.exceptionLogFilePath = exceptionLogFilePath;
        this.url = url;
    }

    private void scan(BufferedReader brIn) {
        // 声明数据结构domain
        AuditObject ao = null;
        try {
            // 声明文件：调用完记日志的文件
            File resultFile = new File(resultFilePath);
            FileWriter resultFileWriter = new FileWriter(resultFile);
            File exceptionLogFile = new File(exceptionLogFilePath);
            FileWriter exceptionFileWriter = new FileWriter(exceptionLogFile);

            // 数据源：标准输入流
            String line = null;
            while ((line = brIn.readLine()) != null) {
                String[] arr = line.split(",");
                if (arr.length < 12) {
                    writeExceptionLogFile(exceptionFileWriter, null, line, null, new Exception("数据格式不对(长度)"));
                    continue;
                }
                ao = new AuditObject();
                //前四个字段固定是时间,用户号,sessionid和url
                Date frms_trans_time = convertDate(arr[0].trim());
                String url = arr[3].trim();
                if (frms_trans_time == null || frms_trans_time.getTime() <= 0) {
                    writeExceptionLogFile(exceptionFileWriter, null, line, null, new Exception("数据格式不对(交易时间)"));
                    continue;
                }

                ao.setTransTime(frms_trans_time);
                ao.setUserId(arr[1].trim());
                ao.put("frms_session_id", arr[2].trim());
                ao.put("frms_url", url.trim());

                //第5个字段是url参数里可能包括逗号,所以现在开始倒着取元素
                //最后一个字段固定是用户ip归属地信息,找不到归属地的情况下是Not found
                int locationOff = 2;
                if (arr[arr.length - 1].equals("Not found")) {
                    locationOff = 0;
                }
                //之后的端口号和cdn服务器名都是固定的
                ao.put("frms_port", arr[arr.length - 2 - locationOff]);
                String[] cdnArr = arr[arr.length - 3 - locationOff].split(" |:", 3);
                if (cdnArr.length == 3) {
                    ao.put("frms_cdn", cdnArr[1]);
                }
                int cookieOff = 0;
                boolean err = false;
                //接下来的cookie又是可能包含逗号了,只能结合下一个字段"referer"要么为空,要么就是http开头来判断
                if (!arr[arr.length - 4 - locationOff].equals("")) {
                    for (int i = 0; ; i++) {
                        if (arr.length - 5 - i - locationOff <= 6) {
                            err = true;
                            break;
                        }
                        if (arr[arr.length - 5 - i - locationOff].equals("") || arr[arr.length - 5 - i - locationOff].startsWith("http")) {
                            cookieOff += i;
                            break;
                        }
                    }
                }
                if (err) {
                    writeExceptionLogFile(exceptionFileWriter, null, line, null, new Exception("数据格式不对(长度)"));
                    continue;
                }
                ao.put("frms_referer", arr[arr.length - 5 - cookieOff - locationOff]);
                ao.put("frms_ip", arr[arr.length - 6 - cookieOff - locationOff]);
                ao.put("frms_ip_cdn", arr[arr.length - 7 - cookieOff - locationOff]);
                String params = "";
                for (int i = 4; i < arr.length - 7 - cookieOff - locationOff; i++) {
                    params += arr[i] + ',';
                }
                Map<String, String[]> values = new HashMap<>();
                if (params.length() > 1) {
                    String temp = params.substring(0, params.length() - 1);
                    ao.put("frms_params", temp);
                    RequestUtil.parseParameters(values, temp, "UTF-8");
                }
                if (url == null) {
                    writeExceptionLogFile(exceptionFileWriter, null, line, null, new Exception("数据格式不对(url)"));
                    continue;
                }
                switch (url) {
                    case Globals.URL_ALL_GET_PASS_CODE:
                        if ("login".equals(getSingleValue(values, "module"))) {
                            ao.put("frms_trade_mode", "2");
                            ao.setBizCode(Globals.BIZ_PAY_LOGIN);
                        } else {
                            ao.setBizCode(Globals.BIZ_PAY_BUY);
                        }
                        break;
                    case Globals.URL_QUERY_LOG:
                        ao.put("frms_trade_mode", "1");
                        ao.setBizCode(Globals.BIZ_PAY_QUERY);
                        break;
                    case Globals.URL_QUERY_QUERY:
                        ao.put("frms_trade_mode", "2");
                        ao.setBizCode(Globals.BIZ_PAY_QUERY);
                        break;
                    case Globals.URL_REG_CHECK:
                        ao.put("frms_trade_mode", "1");
                        ao.setBizCode(Globals.BIZ_PAY_REG);
                        ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
                        break;
                    case Globals.URL_REG_GET:
                        ao.put("frms_trade_mode", "2");
                        ao.setBizCode(Globals.BIZ_PAY_REG);
                        ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
                        break;
                    case Globals.URL_REG_SUB:
                        ao.put("frms_trade_mode", "3");
                        ao.setBizCode(Globals.BIZ_PAY_REG);
                        ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
                        break;
                    case Globals.URL_LOGIN_CHECK:
                        ao.put("frms_trade_mode", "1");
                        ao.setBizCode(Globals.BIZ_PAY_BUY);
                        break;
                    case Globals.URL_BUY_SUB:
                        ao.put("frms_trade_mode", "2");
                        ao.setBizCode(Globals.BIZ_PAY_BUY);
                        break;
                    case Globals.URL_LOGIN_LOGIN:
                        ao.put("frms_trade_mode", "1");
                        ao.setBizCode(Globals.BIZ_PAY_LOGIN);
                        ao.setUserId(getSingleValue(values, "loginUserDTO.user_name"));
                        break;
//                    case "/otn/":
//                        writeExceptionLogFile(exceptionFileWriter, null, line, null, new Exception("不需要调引擎的数据"));
//                        continue;
                    default:
                        ao.put("frms_trade_mode", "99");
                        ao.setBizCode(Globals.BIZ_PAY_OTHER);
                        break;
                }

                ao.setUuid(getUUID());

                // 调用接口
                call(resultFileWriter, exceptionFileWriter, ao, line);
            }
            // 关闭
            brIn.close();
            resultFileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用邦盛服务
     *
     * @param ao 入参是拼接json串的几个属性
     */
    private void call(FileWriter resultFileWriter, FileWriter exceptionFileWriter, AuditObject ao, String inputLine) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String uuid = null;
        try {
            String logFileString = "-1";
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("method", "post");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent",
                    "Apache-HttpClient/4.2.6 (java 1.5)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());

            List list = new ArrayList();
            list.add(ao);
            out.print(JSON.toJSONString(list, SerializerFeature.WriteClassName).toString());

            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                JSONArray b = JSONArray.parseArray(result);
                AuditResult auditResult = (AuditResult) b.get(0);
                String retCode = auditResult.getRetCode();
                if (!"200".equals(retCode.trim())) {
                    writeExceptionLogFile(exceptionFileWriter, uuid, inputLine, "-3", new Exception());
                    continue;
                }
                List<Risk> risks = auditResult.getRisks();
                if (risks.size() == 0) {
                    logFileString = "0";
                    continue;
                }
                logFileString = "";
                for (Risk risk : risks) {
                    logFileString += risk.getRuleName().split(":")[1].trim() + ",";
                }
            }
            // 写文件记录日志
            writeResultFile(resultFileWriter, uuid, inputLine, logFileString);
        } catch (Exception e) {
            // System.out.println("发送 POST请求出现异常！" + e);
            writeExceptionLogFile(exceptionFileWriter, uuid, inputLine, "-2", e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 自动生成UUID
     *
     * @return 返回UUID
     */
    private String getUUID() {
        return UUID.randomUUID().toString();
    }

    private Date convertDate(String dateStr) {
        Date dt = null;
        try {
            SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyyMMddHHmmss");
            dt = YYYY_MM_DD.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dt;
    }

    /**
     * 获取当前时间，记日志用。
     *
     * @return 当前时间，yyyyMMddHHmmssSSS型
     */
    private String getCurrentTime() {
        SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss.SSS");
        return "[" + yyyyMMddHHmmssSSS.format(new Date()) + "] ";
    }

    /**
     * 将查询流立方的结果写入文件
     *
     * @param fw       FileWriter
     * @param uuid     UUID
     * @param request  查询请求
     * @param response 流立方返回的结果
     */
    private void writeResultFile(FileWriter fw, String uuid, String request,
                                 String response) {
        try {
            fw.write(generateWriteFileString(uuid, request, response) + "\n");
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将查询流立方的异常信息写入文件
     *
     * @param fw       FileWriter
     * @param uuid     UUID
     * @param request  查询请求
     * @param response 流立方返回的结果
     */
    private void writeExceptionLogFile(FileWriter fw, String uuid, String request,
                                       String response, Exception exception) {
        try {
            fw.write(generateWriteFileString(uuid, request, response) + " " + exception.getMessage() + "\n");
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成写结果文件的字符串
     *
     * @param uuid     UUID
     * @param request  查询请求
     * @param response 流立方返回的结果
     * @return 写结果文件的字符串
     */
    private String generateWriteFileString(String uuid, String request,
                                           String response) {
        StringBuffer sb = new StringBuffer();
        sb.append(getCurrentTime() + " ").append(request).append(" ")
                .append(response);
        return sb.toString();
    }

    private String getSingleValue(Map<String, String[]> params, String key) {
        String[] values = params.get(key);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return "";
    }

}

