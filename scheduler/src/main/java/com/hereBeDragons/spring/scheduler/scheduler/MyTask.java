package com.hereBeDragons.spring.scheduler.scheduler;

import org.springframework.scheduling.TaskScheduler;

import java.util.concurrent.ScheduledFuture;

public interface MyTask {

	String getName();

    /**
     * @see {@link AbstractMyTask#scheduleTask(TaskScheduler)}
     * @param myTaskScheduler
     */
	void scheduleTask(TaskScheduler myTaskScheduler);

    /**
     * @see {@link AbstractMyTask#executeTask()}
     */
	void executeTask();

    /**
     * @see {@link AbstractMyTask#getScheduling()}
     * @return {@link ScheduledFuture}
     */
	ScheduledFuture<?> getScheduling();

    /**
     * @see {@link AbstractMyTask#reSchedule(String)}
     * @param schedule
     */
	void reSchedule(String schedule);
}
