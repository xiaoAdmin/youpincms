import grails.plugin.quartz2.ClosureJob

import org.quartz.impl.triggers.SimpleTriggerImpl

grails.plugin.quartz2.autoStartup = true

org{
	quartz{
		//anything here will get merged into the quartz.properties so you don't need another file
		scheduler.instanceName = 'MyAppScheduler'
		threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
		threadPool.threadCount = 20
		threadPool.threadsInheritContextClassLoaderOfInitializingThread = true
		jobStore.class = 'org.quartz.simpl.RAMJobStore'
	}
}

//you can drive the setup. just give them a unique key like "buyTheTicket" below.
//the quartzScheduler bean and application context are passed to your closure
grails.plugin.quartz2.jobSetup.test = {
	quartzScheduler, ctx ->
	//how it should look
	def jobDetail = ClosureJob.createJob { jobCtx , appCtx->
		appCtx.GoodsService.test()
	}

	def trigger1 = new SimpleTriggerImpl(name:"test", startTime:new Date(),repeatInterval:1000,repeatCount:10)

	quartzScheduler.scheduleJob(jobDetail, trigger1)
}

//grails.plugin.quartz2.jobSetup.buyTicket2 = { quartzScheduler, ctx  ->
//	//example a service call using the InvokeMethodJob and quartz's new builder syntax
//	def props = new JobDataMap([targetObject:ctx.hunterService,targetMethod:'takeTheRide',arguments:[true]])
//	JobDetail jobDetail = newJob(InvokeMethodJob.class) //use the static helper newJob from org.quartz.JobBuilder
//	.withIdentity("take the ride")
//	.usingJobData(props)
//	.build()
//
//	Trigger trigger = newTrigger().withIdentity("hunter trigger")
//	.withSchedule(
//	simpleSchedule().withIntervalInSeconds(1).repeatForever()
//	)
//	.startNow().build()
//
//	quartzScheduler.scheduleJob(jobDetail, trigger)
//}