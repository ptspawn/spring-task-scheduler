package com.hereBeDragons.spring.scheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * MultiThreaded Spring Based Task Scheduler
 */
@EnableScheduling
public class MyTaskScheduler implements SchedulingConfigurer {

	private Logger logger = LoggerFactory.getLogger(MyTaskScheduler.class);

	private static final String THREAD_PREFIX = "Scheduler-Manager-Thread";
	private static final int DEFAULT_THREAD_POOL_SIZE = 1;
	private static final ScheduledTaskRegistrar scheduledTaskRegistrar = new ScheduledTaskRegistrar();
	private static MyTaskScheduler myTaskScheduler;

	private int threadPool;
	private TaskScheduler taskScheduler;

	private final Set<MyTask> taskToSchedule = new LinkedHashSet<>();

    /**
     * Singleton factory method to get an instance of {@link MyTaskScheduler}
     * <B>There can be only one!</B>
     * One single {@link Thread} will be available.
     * For more use {@link MyTaskScheduler#getInstance(int, MyTask...)} instead
     *
     * @param tasks one or more {@link MyTask}. Null tasks will be ignored
     * @return A singleton instance of {@link MyTaskScheduler}
     * @throws IllegalArgumentException if not tasks are provided
     */
	public static MyTaskScheduler getInstance(MyTask... tasks) throws IllegalArgumentException {
		return getInstance(DEFAULT_THREAD_POOL_SIZE, tasks);
	}

    /**
     * Singleton factory method to get an instance of {@link MyTaskScheduler}
     * <B>There can be only one!</B>
     *
     * @param threadPool number of available threads
     * @param tasks one or more {@link MyTask}. Null tasks will be ignored
     * @return A singleton instance of {@link MyTaskScheduler}
     *
     * @throws IllegalArgumentException if not tasks are provided
     */
	public static MyTaskScheduler getInstance(int threadPool, MyTask... tasks) throws IllegalArgumentException {
		if (tasks == null || tasks.length == 0)
			throw new IllegalArgumentException("Invalid tasks provided");

		if (myTaskScheduler == null)
			myTaskScheduler = new MyTaskScheduler(threadPool < 1 ? DEFAULT_THREAD_POOL_SIZE : threadPool, tasks);
		return myTaskScheduler;
	}

	private MyTaskScheduler(int threadPool, MyTask... tasks) throws IllegalArgumentException {
		this.threadPool = threadPool;
		addTasksToSchedule(tasks);
	}

    /**
     * Is only public because of the {@link SchedulingConfigurer} interface.
     * <B>Not meant to be used outside the scope of this class</B>
     *
     * @param scheduledTaskRegistrar
     */
	@Override
	public final void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {

		logger.info("Configuring scheduled tasks.");

		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(threadPool);
		threadPoolTaskScheduler.setThreadNamePrefix(THREAD_PREFIX);
		threadPoolTaskScheduler.initialize();

		taskToSchedule.forEach(task -> {
		    if (task != null) {
                task.scheduleTask(threadPoolTaskScheduler);
                logger.info(task.getName() + " was added with the schedule " + task.getScheduling().toString());
            }
		});

		this.taskScheduler = threadPoolTaskScheduler;
		scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

		logger.info("Scheduler now running...");
	}

    /**
     * Reschedules the provided task
     *
     * @param task  {@link MyTask}
     * @param newSchedule String with valid cron expression according to {@link AbstractMyTask#reSchedule(String)}
     *
     * @throws IllegalArgumentException if the {@link MyTask} is not in the scheduler or if it's null
     */
	public void reScheduleTask(MyTask task, String newSchedule) throws IllegalArgumentException{
	    if (task == null)
            throw new IllegalArgumentException("MyTask can't be null");
		if (!taskToSchedule.contains(task))
			throw new IllegalArgumentException("MyTask does not exist");
        logger.info("Rescheduling task " + task.getName() + " to schedule:" + newSchedule);
        task.reSchedule(newSchedule);
	}

    /**
     * Method to add task to the scheduler
     * @param tasks One or more {@link MyTask}
     *              Does nothing for null {@link MyTask}
     */
	public void addTasksToSchedule(MyTask... tasks) {
		// Can't use Collections.AddAll() because if there were repeated items, the second one would be ignored...
		for (MyTask taskToAdd : tasks)
		    if (taskToAdd != null)
			    logger.info(taskToSchedule.remove(taskToAdd) ? "Task " + taskToAdd.getName() + " has been added."
                    : "Task " + taskToAdd.getName() + " was already in the scheduler. No duplicates allowed");
	}

    /**
     * Method to remove task from the scheduler
     * @param task One or more {@link MyTask}
     *             Does nothing for null {@link MyTask}
     */
	public void removeTaskFromSchedule(MyTask... task) {
		for (MyTask taskToRemove : task)
		    if (taskToRemove != null)
                logger.info(taskToSchedule.remove(taskToRemove) ? "Task " + taskToRemove.getName() + " has been removed."
                    : "Task " + taskToRemove.getName() + " was not in the scheduler.");
	}

    /**
     * @return A list with all the scheduled tasks
     */
	public List<MyTask> listScheduledTasks(){
	    return new ArrayList<>(taskToSchedule);
    }

    /**
     * Method to reset the Scheduler.
     * Call after changing a {@link MyTask}'s schedule
     * So that changes take place immediately
     */
	public void refreshCronSchedule() {
		logger.info("Refresing the scheduler");
		taskToSchedule.forEach(task -> {
			if (task.getScheduling() != null) {
				task.getScheduling().cancel(true);
				task.scheduleTask(taskScheduler);
			}
		});
	}

    /**
     * Initialization method.
     * It's called by spring after instantiation
     */
	@PostConstruct
	public void init() {
		configureTasks(scheduledTaskRegistrar);
	}
}
