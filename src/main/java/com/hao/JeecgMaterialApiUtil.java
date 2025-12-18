package com.hao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * JeecgBoot 物料列表接口工具类
 * 封装HttpClient请求、动态参数、JSON解析逻辑
 */
public class JeecgMaterialApiUtil {

    // 接口基础URL
    private static final String BASE_API_URL = "https://zrcloud.zrxdjt.com:18888/jeecgboot/eam/htEasMaterial/list";
    // 核心认证Token（需替换为实际有效Token，或添加登录接口自动获取）
    private static final String AUTHORIZATION_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IjEwNzg0OCIsImV4cCI6MTc2NjA2OTMzMn0.G3W9NF3DoMcrL6MMayQ-V4O_mM1JtsIAaFGSrzkyHEM";
    private static final String X_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IjEwNzg0OCIsImV4cCI6MTc2NjA2OTMzMn0.G3W9NF3DoMcrL6MMayQ-V4O_mM1JtsIAaFGSrzkyHEM";
    // 全局HttpClient（复用连接，提升性能）
    private static final HttpClient HTTP_CLIENT;
    //日期
    //private static final String yesterday = "2025-12-14";
    private static final String orgDesc = "巨润";
    //申报人员集合
    private static final List<String> modfiyUserNameList = Arrays.asList("段作涛","冯昌帅","刘全磊","王营玮","史同磊","付天可","赵国强","温洪生","庞黎明","张昭航","张芹芹","杨孔卫","马彦田","让红星","张俊凯","郭洪考","吕复标","张品品");
    //审核人员集合
    private static final List<String> auditUserNameList = Arrays.asList("黄洪帅","李鲁","姚树学","刘培","徐发顺","刘来硕","李含冰");

    // 初始化HttpClient（静态代码块）
    static {
        try {
            // 构建支持HTTPS（忽略证书，测试用；生产环境删除SSL相关配置）
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }}, new SecureRandom());

            // 初始化HttpClient，设置超时和SSL
            HTTP_CLIENT = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .sslContext(sslContext)
                    .connectTimeout(java.time.Duration.ofSeconds(10))  // 连接超时
                    //.readTimeout(java.time.Duration.ofSeconds(30))     // 读取超时
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("初始化HttpClient失败", e);
        }
    }

    /**
     * 调用物料列表接口，获取result.total值
     * @param paramMap 动态参数（支持：statusFlag、orgDesc、modfiyUsername、modfiyTime、factoryauditUsername、factoryauditTime、pageNo、pageSize）
     * @return 成功返回result.total值；失败返回-1
     * @throws Exception 网络异常/解析异常
     */
    public static long getMaterialListTotal(Map<String, Object> paramMap,String status) throws Exception {
        // 1. 动态构建请求URL
        String requestUrl = buildRequestUrl(paramMap);

        // 2. 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                // 核心认证头
                .header("Authorization", AUTHORIZATION_TOKEN)
                .header("X-Access-Token", X_ACCESS_TOKEN)
                // 基础标识头
                //.header("Host", "zrcloud.zrxdjt.com:18888")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0")
                .header("Accept", "application/json, text/plain, */*")
                .build();

        // 3. 发送请求并获取响应
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        // 4. 解析响应结果
        return parseResponse(response.body(),status);
    }

    /**
     * 动态构建请求URL（处理参数拼接和中文编码）
     * @param paramMap 动态参数
     * @return 完整请求URL
     */
    private static String buildRequestUrl(Map<String, Object> paramMap) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }

        // 基础参数（固定值）
        StringBuilder urlBuilder = new StringBuilder(BASE_API_URL);
        urlBuilder.append("?column=easCode&order=asc");

        // 动态参数拼接（支持传入一个或多个参数）
        // 1. 状态标识
        if (paramMap.containsKey("statusFlag")) {
            urlBuilder.append("&statusFlag=").append(URLEncoder.encode(paramMap.get("statusFlag").toString(), StandardCharsets.UTF_8));
        }
        // 2. 组织描述（带通配符*）
        if (paramMap.containsKey("orgDesc")) {
            String orgDesc = URLEncoder.encode(paramMap.get("orgDesc").toString(), StandardCharsets.UTF_8);
            urlBuilder.append("&orgDesc=*").append(orgDesc).append("*");
        }
        // 3. 修改人
        if (paramMap.containsKey("modfiyUsername")) {
            String modfiyUsername = URLEncoder.encode(paramMap.get("modfiyUsername").toString(), StandardCharsets.UTF_8);
            urlBuilder.append("&modfiyUsername=*").append(modfiyUsername).append("*");
        }
        // 4. 修改时间（需传入yyyy-MM-dd格式，拆分为begin/end）
        if (paramMap.containsKey("modfiyTime")) {
            String modfiyTime = paramMap.get("modfiyTime").toString();
            urlBuilder.append("&modfiyTime_begin=").append(modfiyTime).append("&modfiyTime_end=").append(modfiyTime);
        }
        // 5. 工厂审核人
        if (paramMap.containsKey("factoryauditUsername")) {
            String factoryauditUsername = URLEncoder.encode(paramMap.get("factoryauditUsername").toString(), StandardCharsets.UTF_8);
            urlBuilder.append("&factoryauditUsername=*").append(factoryauditUsername).append("*");
        }
        // 6. 工厂审核时间（需传入yyyy-MM-dd格式，拆分为begin/end）
        if (paramMap.containsKey("factoryauditTime")) {
            String factoryauditTime = paramMap.get("factoryauditTime").toString();
            urlBuilder.append("&factoryauditTime_begin=").append(factoryauditTime).append("&factoryauditTime_end=").append(factoryauditTime);
        }
        // 7. 页码（默认1）
        int pageNo = paramMap.containsKey("pageNo") ? Integer.parseInt(paramMap.get("pageNo").toString()) : 1;
        urlBuilder.append("&pageNo=").append(pageNo);
        // 8. 页大小（默认10）
        int pageSize = paramMap.containsKey("pageSize") ? Integer.parseInt(paramMap.get("pageSize").toString()) : 10;
        urlBuilder.append("&pageSize=").append(pageSize);
        // 9. 动态时间戳（防止缓存）
        urlBuilder.append("&_t=").append(System.currentTimeMillis());

        return urlBuilder.toString();
    }

    /**
     * 解析响应JSON，提取result.total值
     * @param responseBody 响应体字符串
     * @return 成功返回total值；失败返回-1
     */
    private static long parseResponse(String responseBody,String status) {
        try {
            // 解析JSON（使用fastjson2）
            JSONObject responseJson = JSONObject.parseObject(responseBody);
            // 校验code是否为200
            int code = responseJson.getIntValue("code");
            if (code != 200) {
                System.err.println("接口请求失败，code=" + code + "，响应体：" + responseBody);
                return -1;
            }
            // 提取result下的total值
            JSONObject resultJson = responseJson.getJSONObject("result");
            if (Objects.isNull(resultJson)) {
                System.err.println("响应中未找到result字段，响应体：" + responseBody);
                return -1;
            }
            if (!"退回".equals(status)) {
                return resultJson.getLongValue("total");
            }else {
                return findRefuseCount(resultJson);
            }

        } catch (Exception e) {
            System.err.println("解析响应JSON失败：" + e.getMessage() + "，响应体：" + responseBody);
            return -1;
        }
    }
    //获取退回的数量
    private static long findRefuseCount(JSONObject resultJson){
        long refuseCount =0;
        JSONArray records = resultJson.getJSONArray("records");

        // 遍历数组统计非空memoText
        for (int i = 0; i < records.size(); i++) {
            JSONObject record = records.getJSONObject(i);
            String memoText = record.getString("memoText");
            // 判断是否不为空（排除null、空字符串、纯空格）
            if (memoText != null && !memoText.trim().isEmpty()) {
                refuseCount++;
            }
        }
        return refuseCount;
    }


    //当日提交数量
    private static void yesterdaySubmitCount(String date){
        try {
            System.out.println(date+"当日提交数量");
            for (String modfiyUserName : modfiyUserNameList) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("orgDesc", orgDesc);
                paramMap.put("modfiyUsername", modfiyUserName);
                paramMap.put("modfiyTime", date);
                long total2 = getMaterialListTotal(paramMap,null);
                System.out.println(total2);
            }
        } catch (Exception e) {
            System.err.println(date+"当日提交数量查询失败：" + e.getMessage());
        }
    }
    //各状态数量
    private static void everyStatusCount(String status){
        try {
            int statusFlag = 0;
            switch (status) {
                case "退回":
                case "未提交":
                    statusFlag = 0;break;
                case "已提交":statusFlag = 1;break;
                case "分厂审核":statusFlag = 2;break;
                case "专业审核":statusFlag = 3;break;
            }
            System.out.println(status+"数量");
            for (String modfiyUserName : modfiyUserNameList) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("orgDesc", orgDesc);
                paramMap.put("modfiyUsername", modfiyUserName);
                paramMap.put("statusFlag", statusFlag);
                if ("退回".equals(status)) {
                    paramMap.put("pageNo", 1);
                    paramMap.put("pageSize", 500);
                }
                long total2 = getMaterialListTotal(paramMap,status);
                System.out.println(total2);
            }
        } catch (Exception e) {
            System.err.println(status+"数量查询失败：" + e.getMessage());
        }
    }

    //审核数量
    private static void auditCount(String date){
        try {
            System.out.println(date+"审核数量");
            for (String auditUserName : auditUserNameList) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("orgDesc", orgDesc);
                paramMap.put("statusFlag", 2);
                paramMap.put("factoryauditUsername", auditUserName);
                if (!"".equals(date) && date != null) {
                    paramMap.put("factoryauditTime", date);
                }
                long total2 = getMaterialListTotal(paramMap,null);
                System.out.println(total2);
            }
        } catch (Exception e) {
            System.err.println(date+"审核数量查询失败：" + e.getMessage());
        }
    }
    // 测试示例
    public static void main(String[] args) {
        String yesterday = "2025-12-17";
        yesterdaySubmitCount(yesterday);//当日提交数量
        everyStatusCount("已提交");//已提交数量
        everyStatusCount("分厂审核");//分厂审核数量
        everyStatusCount("专业审核");//专业审核数量
        everyStatusCount("未提交");//未提交数量
        everyStatusCount("退回");//退回数量
        auditCount("");//审核数量
        auditCount(yesterday);//当日审核数量
    }
}
