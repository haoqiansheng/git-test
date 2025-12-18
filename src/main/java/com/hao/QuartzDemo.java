package com.hao;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

// 2. 配置并启动调度器
public class QuartzDemo {
    public static void main(String[] args) throws SchedulerException {
        // 2.1 创建调度器
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        // 2.2 创建任务详情（绑定任务类）
        JobDetail job = JobBuilder.newJob(SendTextEmail.class)
                .withIdentity("myJob", "group1") // 任务唯一标识（名称+组名）
                .build();

        // 2.3 创建触发器（定义执行规则）
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "group1") // 触发器唯一标识
                // 方式1：使用cron表达式
                .withSchedule(CronScheduleBuilder.cronSchedule("11 11 11,13 * * ?"))
                // 方式2：简单触发（延迟0秒，每隔1秒执行）
                // .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                //         .withIntervalInSeconds(1)
                //         .repeatForever())
                .build();

        // 2.4 注册任务和触发器到调度器
        scheduler.scheduleJob(job, trigger);

        // 2.5 启动调度器
        scheduler.start();

    }
}
