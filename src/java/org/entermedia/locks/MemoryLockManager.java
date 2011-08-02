package org.entermedia.locks;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.data.Searcher;
import org.openedit.util.DateStorageUtil;

import com.openedit.OpenEditException;

public class MemoryLockManager implements LockManager
{
	private static final Log log = LogFactory.getLog(MemoryLockManager.class);

	protected Map<String,Stack> fieldLocks;
	protected long fieldLockId = 0;
	
	protected Map<String,Stack> getLocks()
	{
		if (fieldLocks == null)
		{
			fieldLocks = new ConcurrentHashMap<String,Stack>();
		}
		return fieldLocks;
	}

	public Lock lock(String inCatId, String inPath, String inOwnerId)
	{
		Lock lockrequest = addLock(inPath, inOwnerId);

		Lock lock = loadLock(inCatId,inPath);
		int tries = 0;
		while( !isOwner(lock, inOwnerId))
		{
			tries++;
			log.info("Could not lock trying again  " + tries);
			if( tries > 9)
			{
				release(inCatId,lockrequest);
				throw new OpenEditException("Could not lock file " + inPath + " locked by " + lock.getOwnerId() );
			}
			try
			{
				Thread.sleep(250);
			}
			catch( Exception ex)
			{
				//does not happen
				log.info(ex);
			}
			
			lock = loadLock(inCatId,inPath);
		}
		return lock;

	}

	protected long nextId()
	{
		return fieldLockId++;
	}
	@Override
	public Lock loadLock(String inCatId, String inPath)
	{
		
		Stack stack = getStack(inPath);
		if( stack.empty())
		{
			return null;
		}
		Lock lock = (Lock)stack.firstElement();
		return lock;
	}

	@Override
	public Collection getLocksByDate(String inCatId, String inPath)
	{
		Stack stack = getStack(inPath);
		return stack;
	}

	@Override
	public Lock lockIfPossible(String inCatId, String inPath, String inOwnerId)
	{

		Lock lockrequest = addLock(inPath, inOwnerId);

		Lock lock = loadLock(inCatId,inPath);
		if( isOwner(lock,inOwnerId))
		{
			return lockrequest;
		}
		release(inCatId, lockrequest);
		return null;
	}

	@Override
	public boolean release(String inCatId, Lock inLock)
	{
		Stack<Lock> locks = getStack(inLock.getPath());
		return locks.remove(inLock);
	}

	@Override
	public void releaseAll(String inCatalogId, String inPath)
	{
		getLocks().clear();
	}

	@Override
	public boolean isOwner(Lock inLock, String inOwnerId)
	{
		boolean is = inLock.isOwner("inmemory", inOwnerId);
		return is;
	}

	protected Lock addLock(String inPath, String inOwnerId)
	{
		Lock lockrequest = new Lock();
		lockrequest.setPath(inPath);
		lockrequest.setOwnerId(inOwnerId);
		lockrequest.setDate(new Date());
		lockrequest.setNodeId("inmemory");
		lockrequest.setProperty("date", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		push(lockrequest);
		return lockrequest;
	}

	protected void push(Lock inLockrequest)
	{
		Stack locks = getStack(inLockrequest.getPath());
		locks.push(inLockrequest);
	}

	protected Stack getStack(String inPath)
	{
		Stack locks = getLocks().get(inPath);
		if( locks == null)
		{
			locks = new Stack<Lock>();
			getLocks().put(inPath,locks);
		}
		return locks;
	}
}