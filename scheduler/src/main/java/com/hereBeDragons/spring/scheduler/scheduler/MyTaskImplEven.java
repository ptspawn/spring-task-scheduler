package com.hereBeDragons.spring.scheduler.scheduler;

import com.hereBeDragons.spring.scheduler.mockobjects.MockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyTaskImplEven extends AbstractMyTask {

	private static final int DEFAULT_THREAD_POOL_SIZE = 10;
	private static final List<Integer> databaseObjects = new ArrayList<>();
	private static final ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

	@Autowired
	private MockRepository mockRepository;

	public MyTaskImplEven(String name) {
		super(name);
	}

	public MyTaskImplEven(String name, String cronExpression) {
		super(name, cronExpression);
	}

	@Override
	public void scheduleTask(TaskScheduler taskScheduler) {
		scheduling = taskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				logger.info("Running scheduled task " + getName());
				databaseObjects.clear();
				databaseObjects.addAll(findAllPendingObjects());
				System.out.println("Abstrack Sedr Task was invoked "+ ++taskCounter + " times. Please @Override it to actually do something");
				// Actually do stuff;
			}
		}, new Trigger() {
			@Override
			public Date nextExecutionTime(TriggerContext triggerContext) {
				return new CronTrigger(cronExpression).nextExecutionTime(triggerContext);
			}
		});
	}

	private List<Integer> findAllPendingObjects() {
		List<Integer> paymentList = mockRepository.findAllEvenNumbers();
		logger.info("Found " + paymentList.size() + " even numbers.");
		return paymentList;
	}

	private void checkNumberStatus() {
		databaseObjects.forEach(number -> {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					logger.info("Checking number " + number);
				}
			});
		});
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
