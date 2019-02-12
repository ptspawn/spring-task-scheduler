package com.hereBeDragons.spring.scheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;

public class AbstractMyTask implements MyTask{

	protected Logger logger = LoggerFactory.getLogger(MyTask.class);
	// Pattern courtesy of https://github.com/quartznet/quartznet/blob/master/src/Quartz/Xml/job_scheduling_data_2_0.xsd
	private static final Pattern cronPattern = Pattern.compile("(((([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?,)*([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?)|(([\\*]|[0-9]|[0-5][0-9])\\/([0-9]|[0-5][0-9]))|([\\?])|([\\*]))[\\s](((([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?,)*([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?)|(([\\*]|[0-9]|[0-5][0-9])\\/([0-9]|[0-5][0-9]))|([\\?])|([\\*]))[\\s](((([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?,)*([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?)|(([\\*]|[0-9]|[0-1][0-9]|[2][0-3])\\/([0-9]|[0-1][0-9]|[2][0-3]))|([\\?])|([\\*]))[\\s](((([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?,)*([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?(C)?)|(([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])\\/([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?)|(L(-[0-9])?)|(L(-[1-2][0-9])?)|(L(-[3][0-1])?)|(LW)|([1-9]W)|([1-3][0-9]W)|([\\?])|([\\*]))[\\s](((([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?,)*([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?)|(([1-9]|0[1-9]|1[0-2])\\/([1-9]|0[1-9]|1[0-2]))|(((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?,)*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)|((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\\/(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))|([\\?])|([\\*]))[\\s]((([1-7](-([1-7]))?,)*([1-7])(-([1-7]))?)|([1-7]\\/([1-7]))|(((MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?,)*(MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?(C)?)|((MON|TUE|WED|THU|FRI|SAT|SUN)\\/(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?)|(([1-7]|(MON|TUE|WED|THU|FRI|SAT|SUN))?(L|LW)?)|(([1-7]|MON|TUE|WED|THU|FRI|SAT|SUN)#([1-7])?)|([\\?])|([\\*]))([\\s]?(([\\*])?|(19[7-9][0-9])|(20[0-9][0-9]))?| (((19[7-9][0-9])|(20[0-9][0-9]))\\/((19[7-9][0-9])|(20[0-9][0-9])))?| ((((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?,)*((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?)?)(\\?)");

	// https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm
	protected String cronExpression = "0/5 * * * * ?";  // can be pulled from db as AppResource or configured via XML

	protected ScheduledFuture<?> scheduling;

	protected String name;

	protected static int taskCounter;

	public AbstractMyTask(String name) {
		this.name = name;
	}

	public AbstractMyTask(String name, String cronExpression) {
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Name can't be null");
		if (cronExpression == null || cronPattern.matcher(cronExpression).matches())
			throw new IllegalArgumentException("Invalid Cron Expression");
		this.cronExpression = cronExpression;
		this.name = name;
	}

	/**
	 * Adds the current task to the provided {@link TaskScheduler}
	 * The actual task to run should be inside the run() method
	 *
	 * @param taskScheduler {@link TaskScheduler}
	 */
	@Override
	public void scheduleTask(TaskScheduler taskScheduler) {
		scheduling = taskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
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

	@Override
	public final ScheduledFuture<?> getScheduling() {
		return scheduling;
	}

	@Override
	public void reSchedule(String schedule) {
		if (!cronPattern.matcher(schedule).matches()) {
			logger.warn("The provided cronExpression was invalid");
			return;
		}
		this.cronExpression = schedule;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractMyTask)) return false;
		AbstractMyTask that = (AbstractMyTask) o;
		return Objects.equals(scheduling, that.scheduling) &&
				name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scheduling, name);
	}

	@Override
	public String toString() {
		return "MyTask{" +
				"name='" + name + '\'' +
				", cronExpression='" + cronExpression + '\'' +
				'}';
	}
}
