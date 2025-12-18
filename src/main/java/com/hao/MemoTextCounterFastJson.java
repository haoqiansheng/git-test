package com.hao;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class MemoTextCounterFastJson {
    public static void main(String[] args) {
        int nonEmptyCount = 0;
        try {
            // 读取JSON文件内容
            String jsonContent = new String(Files.readAllBytes(Paths.get("src/main/java/com/hao/test.json")), StandardCharsets.UTF_8);

            // 解析为JSONObject
            JSONObject root = JSON.parseObject(jsonContent);

            // 获取result对象中的records数组
            JSONObject result = root.getJSONObject("result");
            JSONArray records = result.getJSONArray("records");

            // 遍历数组统计非空memoText
            for (int i = 0; i < records.size(); i++) {
                JSONObject record = records.getJSONObject(i);
                String memoText = record.getString("memoText");

                // 判断是否不为空（排除null、空字符串、纯空格）
                if (memoText != null && !memoText.trim().isEmpty()) {
                    nonEmptyCount++;
                }
            }

            System.out.println("memoText不为空的数量：" + nonEmptyCount); // 输出结果应为2

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
