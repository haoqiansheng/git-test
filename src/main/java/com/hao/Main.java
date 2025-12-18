package com.hao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        //TIP 当文本光标位于高亮显示的文本处时按 <shortcut actionId="ShowIntentionActions"/>
        // 查看 IntelliJ IDEA 建议如何修正。
        System.out.println("Hello and welcome!");
        System.out.println("push test");
        System.out.println("pull test");
        String lastName ="张";
        String firstName ="三";
        System.out.println("fullName:"+lastName+firstName);
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 定义格式：年-月-日 时:分:秒
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        // 格式化
        String formattedTime = now.format(formatter);
        System.out.println("格式化后：" + formattedTime);

        for (int i = 1; i <= 5; i++) {
            //TIP 按 <shortcut actionId="Debug"/> 开始调试代码。我们已经设置了一个 <icon src="AllIcons.Debugger.Db_set_breakpoint"/> 断点
            // 但您始终可以通过按 <shortcut actionId="ToggleLineBreakpoint"/> 添加更多断点。
            System.out.println("i = " + i);
        }
    }
}
