package com.abel.example.common.component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DailyCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Getter
    private String today; // 可选，用于记录日期

    @PostConstruct
    public void init() {
        today = java.time.LocalDate.now().toString();
    }

    // 累加器调用
    public int incrementAndGet() {
        return counter.incrementAndGet();
    }

    // 获取当前计数
    public int get() {
        return counter.get();
    }

    // 每天凌晨 0 点重置
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyCount() {
        counter.set(0);
        today = java.time.LocalDate.now().toString();
    }
}
