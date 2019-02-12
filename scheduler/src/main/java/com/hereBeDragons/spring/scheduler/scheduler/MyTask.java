package com.hereBeDragons.spring.scheduler.scheduler;

import org.springframework.scheduling.TaskScheduler;

import java.util.concurrent.ScheduledFuture;

public interface MyTask {

	String getName();

	void scheduleTask(TaskScheduler myTaskScheduler);

	ScheduledFuture<?> getScheduling();

	void reSchedule(String schedule);
}
