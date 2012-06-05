package com.openedit.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;

public class ExecutorManager
{
	private static final Log log = LogFactory.getLog(ExecutorManager.class);
	
	protected ExecutorService fieldSharedExecutor;
	protected Integer fieldThreadCount;
	
	public Integer getThreadCount()
	{
		if (fieldThreadCount == null)
		{
			fieldThreadCount = Runtime.getRuntime().availableProcessors();
		}

		return fieldThreadCount;
	}

	public void setThreadCount(Integer inThreadCount)
	{
		fieldThreadCount = inThreadCount;
	}

	public ExecutorService createExecutor() 
	{
			//fieldExecutor = Executors.newCachedThreadPool();
			//fieldExecutor = Executors.newFixedThreadPool(8);
			int minimum = getThreadCount();
			
			return new ThreadPoolExecutor(minimum, minimum,
                    10L, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
	}
	public ExecutorService getSharedExecutor()
	{
		if (fieldSharedExecutor == null)
		{
			fieldSharedExecutor = createExecutor();
		}
		return fieldSharedExecutor;
	}
	
	public void execute(Runnable inRunnable)
	{
		getSharedExecutor().execute(inRunnable);
	}
	public void waitForIt(ExecutorService inExec)
	{
		inExec.shutdown();
		try
		{
			inExec.awaitTermination(30L, TimeUnit.MINUTES);
		}
		catch (InterruptedException e)
		{
			throw new OpenEditException(e);
		}
		log.debug("Exec completed");
	}
}
