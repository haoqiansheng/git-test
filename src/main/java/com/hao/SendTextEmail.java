package com.hao;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.quartz.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

// 1. 定义任务类（实现Job接口）
public class SendTextEmail implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 1. 配置邮件服务器参数
        String smtpHost = "smtp.qq.com"; // SMTP服务器地址
        int smtpPort = 465; // SSL端口
        String fromEmail = "1765417199@qq.com"; // 发件人邮箱
        String password = "gnfpjwpnwdlpbcbg"; // 邮箱授权码（非登录密码）
        String toEmail = "421849502@qq.com"; // 收件人邮箱421849502

        // 2. 设置邮件属性
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost); // SMTP服务器
        props.put("mail.smtp.port", smtpPort); // 端口
        props.put("mail.smtp.auth", "true"); // 需要认证
        props.put("mail.smtp.ssl.enable", "true"); // 启用SSL加密

        // 3. 创建认证器（用于输入发件人账号密码）
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };

        // 4. 创建邮件会话
        Session session = Session.getInstance(props, authenticator);
        session.setDebug(true); // 开启调试模式（可选，打印发送过程）

        try {
            //获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 定义格式：年-月-日 时:分:秒
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
            // 格式化
            String formattedTime = now.format(formatter);
            // 5. 创建邮件消息
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail)); // 发件人
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail)); // 收件人
            message.setSubject(formattedTime); // 主题
            message.setText(formattedTime); // 文本内容
            // 6. 发送邮件
            Transport.send(message);
            System.out.println("邮件发送成功！");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("邮件发送失败！");
        }
    }

}

