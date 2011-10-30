package com.robbix.mp5.ui;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncLoader
{
	public interface Callback
	{
		public void loadComplete(SpriteSet set);
		public void loadFailed(File file, Exception exc);
	}
	
	private static class Job
	{
		public final File file;
		public final Callback callback;
		
		public Job(File file, Callback callback)
		{
			this.file = file;
			this.callback = callback;
		}
	}
	
	private static final AtomicInteger nextSerial = new AtomicInteger(); 
	
	private Thread thread;
	private LinkedList<Job> jobs = new LinkedList<Job>();
	
	public void load(File file, Callback callback)
	{
		synchronized (jobs)
		{
			jobs.addLast(new Job(file, callback));
			
			if (thread == null)
			{
				thread = new Thread(new DoLoad());
				thread.setDaemon(true);
				thread.setName("MP5-AsyncLoader-" + nextSerial.getAndIncrement());
				thread.start();
			}
		}
	}
	
	public boolean isBusy()
	{
		return ! jobs.isEmpty();
	}
	
	private class DoLoad implements Runnable
	{
		public void run()
		{
			for(;;)
			{
				Job job = null;
				
				synchronized (jobs)
				{
					if (jobs.isEmpty())
						break;
					
					job = jobs.removeFirst();
				}
				
				try
				{
					SpriteSet set = new SpriteSetXMLLoader(job.file).load();
					job.callback.loadComplete(set);
				}
				catch (IOException ioe)
				{
					job.callback.loadFailed(job.file, ioe);
				}
			}
			
			thread = null;
		}
	}
}
