package com.hereBeDragons.spring.scheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.PostConstruct;
import java.util.LinkedHashSet;
import java.util.Set;

@EnableScheduling
public class MyTaskScheduler implements SchedulingConfigurer {

	private Logger logger = LoggerFactory.getLogger(MyTaskScheduler.class);

	private static final String THREAD_PREFIX = "Scheduler-Manager-Thread";
	private static final int DEFAULT_THREAD_POOL_SIZE = 10;
	private static final ScheduledTaskRegistrar scheduledTaskRegistrar = new ScheduledTaskRegistrar();
	private static MyTaskScheduler myTaskScheduler;

	private int threadPool;
	private TaskScheduler taskScheduler;

	private final Set<MyTask> taskToSchedule = new LinkedHashSet<>();

	public static MyTaskScheduler getInstance(MyTask... tasks) throws IllegalArgumentException {
		return getInstance(DEFAULT_THREAD_POOL_SIZE, tasks);
	}

	// Singleton. 'There can be only one!'
	public static MyTaskScheduler getInstance(int threadPool, MyTask... tasks) throws IllegalArgumentException {
		if (tasks == null || tasks.length == 0)
			throw new IllegalArgumentException("Invalid tasks provided");

		if (myTaskScheduler == null)
			myTaskScheduler = new MyTaskScheduler(threadPool, tasks);
		return myTaskScheduler;
	}

	private MyTaskScheduler(int threadPool, MyTask... tasks) throws IllegalArgumentException {
		this.threadPool = threadPool;
		addTasksToSchedule(tasks);
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

		logger.info("Configuring scheduled tasks.");

		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(threadPool);
		threadPoolTaskScheduler.setThreadNamePrefix(THREAD_PREFIX);
		threadPoolTaskScheduler.initialize();

		taskToSchedule.forEach(task -> {
			task.scheduleTask(threadPoolTaskScheduler);
			logger.info(task.getName() + " was added with the schedule " + task.getScheduling().toString());
		});

		this.taskScheduler = threadPoolTaskScheduler;
		scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

		logger.info("Scheduled tasks now running...");
	}

	public void reScheduleTask(MyTask task, String newSchedule) {
		if (!taskToSchedule.contains(task))
			throw new IllegalArgumentException("MyTask does not exist");

		logger.info("Rescheduling task " + task.getName() + " to schedule:" + newSchedule);
		task.reSchedule(newSchedule);
	}

	public void addTasksToSchedule(MyTask... tasks) throws IllegalArgumentException {
		// Can't use Collections.AddAll() because if there were repeated items, the second one would be ignored...
		for (MyTask task : tasks) {
			if (!taskToSchedule.add(task))
				throw new IllegalArgumentException("All tasks must be unique");

			logger.info(task + " added to the task Scheduler");
		}
	}

	public void removeTaskFromSchedule(MyTask... task) {
		for (MyTask taskToRemove : task)
			taskToSchedule.remove(taskToRemove);
	}

	public void refreshCronSchedule() {
		logger.info("Refresing the scheduler");
		taskToSchedule.forEach(task -> {
			if (task.getScheduling() != null) {
				task.getScheduling().cancel(true);
				task.scheduleTask(taskScheduler);
			}
		});
	}

	@PostConstruct
	public void init() {
		configureTasks(scheduledTaskRegistrar);
	}
}
