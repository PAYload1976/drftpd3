/*
 * This file is part of DrFTPD, Distributed FTP Daemon.
 * 
 * DrFTPD is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * DrFTPD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DrFTPD; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.drftpd.mirroring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.drftpd.master.RemoteSlave;
import net.sf.drftpd.master.usermanager.User;
import net.sf.drftpd.remotefile.LinkedRemoteFileInterface;

/**
 * @author zubov
 * @author mog
 * @version $Id: Job.java,v 1.18 2004/05/18 20:28:18 zubov Exp $
 */
public class Job {
	protected List _destSlaves;
	protected LinkedRemoteFileInterface _file;
	protected User _owner;
	protected int _priority;
	protected Object _source;
	protected long _timeCreated;
	protected long _timeSpent;

	public Job(
		LinkedRemoteFileInterface file,
		Collection destSlaves,
		Object source,
		User owner,
		int priority) {
		super();
		_destSlaves = new ArrayList(destSlaves);
		_file = file;
		_source = source;
		_owner = owner;
		_priority = priority;
		_timeCreated = System.currentTimeMillis();
		_timeSpent = 0;
	}
	/**
	 * Add destination slaves to this job
	 */
	public synchronized void addSlaves(List list) {
		_destSlaves.addAll(list);
	}

	public void addTimeSpent(long time) {
		_timeSpent += time;
	}

	/**
	 * Returns a List of slaves that can be used with {@see net.sf.drftpd.master.SlaveManagerImpl#getASlave(Collection, char, FtpConfig)}
	 */
	public List getDestinationSlaves() {
		return _destSlaves;
	}

	/**
	 * Returns the file (or directory, if directories can be submitted as jobs,) for this job.
	 * This file is used to tell the slaves what file to transfer & receive.
	 */
	public LinkedRemoteFileInterface getFile() {
		return _file;
	}

	/**
	 * Returns user that is the owner of this file/job or null (or exception) if not applicable.
	 */
	public User getOwner() {
		return _owner;
	}

	/**
	 * Returns the priority of this job.
	 */
	public int getPriority() {
		return _priority;
	}

	/**
	 * Instance that submitted this object.
	 * 
	 * For example so that Archive instance can see if this job was submitted by itself
	 * 
	 * @see java.util.EventObject#getSource()
	 */
	public Object getSource() {
		return _source;
	}

	/**
	 * This is the time that the job was created
	 */
	public long getTimeCreated() {
		return _timeCreated;
	}
	/**
	 * This is the amount of time spent processing this job
	 */
	public long getTimeSpent() {
		return _timeSpent;
	}

	/**
	 * returns true if this job has nothing more to send
	 */
	public boolean isDone() {
		return getDestinationSlaves().isEmpty();
	}

	public synchronized boolean removeDestinationSlave(RemoteSlave slave) {
		if (_destSlaves.contains(slave))
			return _destSlaves.remove(slave);
		else return _destSlaves.remove(null);
	}

	private String outputDestinationSlaves() {
		String toReturn = new String();
		for (Iterator iter = getDestinationSlaves().iterator();iter.hasNext();) {
			RemoteSlave rslave = (RemoteSlave) iter.next();
			String name;
			if (rslave == null) {
				name = "null";
			} else {
				name = rslave.getName();
			}
			if (!iter.hasNext())
				return toReturn + name;
			toReturn = toReturn + name + ",";
		}
		return toReturn;
	}
	
	public String toString() {
		String toReturn =
			"Job[file="
				+ getFile().getName()
				+ ",dest="
				+ outputDestinationSlaves()
				+ ",owner=";
		if (getOwner() != null) {
			toReturn += getOwner().getUsername();
		} else {
			toReturn += "null";
		}
		toReturn += "]";
		return toReturn;
	}
}