/*
 * This file is part of DrFTPD, Distributed FTP Daemon.
 *
 * DrFTPD is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * DrFTPD is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DrFTPD; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package org.drftpd.usermanager;

import net.sf.drftpd.DuplicateElementException;
import net.sf.drftpd.HostMaskCollection;
import net.sf.drftpd.ObjectNotFoundException;
import net.sf.drftpd.master.ConnectionManager;

import java.util.List;


/**
 * @author mog
 * @version $Id: User.java,v 1.2 2004/11/05 13:27:23 mog Exp $
 */
public interface User {
    public UserManager getUserManager();

    public void addAllMasks(HostMaskCollection hostMaskCollection);

    public void addIPMask(String mask) throws DuplicateElementException;

    //    public void addRacesLost();
    //
    //    public void addRacesParticipated();
    //
    //    public void addRacesWon();
    //
    //    public void addRequests();
    //
    //    public void addRequestsFilled();
    public void addSecondaryGroup(String group)
        throws DuplicateElementException;

    public boolean checkPassword(String password);

    /**
     * Commit changes.
     *
     * @throws UserFileException
     *             if an error saving the userfile occured.
     */
    public void commit() throws UserFileException;

    public String getComment();

    /**
     * authenticates and logs in the user.
     *
     * @param user
     *            given password
     */
    public long getCreated();

    /**
     * Returns the credits.
     *
     * @return long
     */
    public long getCredits();

    public long getDownloadedBytes();

    public long getDownloadedBytesDay();

    public long getDownloadedBytesForTrialPeriod(int period);

    public long getDownloadedBytesMonth();

    public long getDownloadedBytesWeek();

    public int getDownloadedFiles();

    public int getDownloadedFilesDay();

    public int getDownloadedFilesForTrialPeriod(int i);

    public int getDownloadedFilesMonth();

    public int getDownloadedFilesWeek();

    public long getDownloadedMilliseconds();

    public long getDownloadedMilliSecondsForTrialPeriod(int i);

    public short getGroupLeechSlots();

    public String getGroupName();

    public List getGroups();

    public short getGroupSlots();

    public HostMaskCollection getHostMaskCollection();

    /**
     * Returns the idleTime.
     *
     * @return long
     */
    public int getIdleTime();

    /**
     * Get last access time
     */
    public long getLastAccessTime();

    //    /**
    //     * Returns the lastNuked.
    //     *
    //     * @return long
    //     */
    //    public long getLastNuked();
    public long getLastReset();

    /**
     * Returns the logins.
     *
     * @return int
     */
    public int getLogins();

    /**
     * Returns the maxLogins.
     *
     * @return int
     */
    public int getMaxLogins();

    /**
     * Returns the maxLoginsPerIP.
     *
     * @return int
     */
    public int getMaxLoginsPerIP();

    //    /**
    //     * Returns the nukedBytes.
    //     *
    //     * @return long
    //     */
    //    public long getNukedBytes();
    //    public int getRacesLost();
    //
    //    public int getRacesParticipated();
    //    /**
    //     * Returns the downloadedSecondsWeek.
    //     *
    //     * @return int
    //     */
    //    public int getRacesWon();

    /**
     * Returns the ratio.
     *
     * @return float
     */
    public float getRatio();

    //    public int getRequests();
    //
    //    public int getRequestsFilled();
    //    /**
    //     * Returns the nuked.
    //     *
    //     * @return int
    //     */
    //    public int getTimesNuked();

    /**
     * Returns the uploadedBytes.
     *
     * @return long
     */
    public long getUploadedBytes();

    /**
     * Returns the uploadedBytesDay.
     *
     * @return long
     */
    public long getUploadedBytesDay();

    public long getUploadedBytesForTrialPeriod(int period);

    /**
     * Returns the uploadedBytesMonth.
     *
     * @return long
     */
    public long getUploadedBytesMonth();

    /**
     * Returns the uploadedBytesWeek.
     *
     * @return long
     */
    public long getUploadedBytesWeek();

    /**
     * Returns the uploadedFiles.
     *
     * @return int
     */
    public int getUploadedFiles();

    /**
     * Returns the uploadedFilesDay.
     *
     * @return int
     */
    public int getUploadedFilesDay();

    public int getUploadedFilesForTrialPeriod(int period);

    /**
     * Returns the uploadedFilesMonth.
     *
     * @return int
     */
    public int getUploadedFilesMonth();

    /**
     * Returns the uploadedFilesWeek.
     *
     * @return int
     */
    public int getUploadedFilesWeek();

    public long getUploadedMilliseconds();

    public long getUploadedMillisecondsForTrialPeriod(int i);

    public String getUsername();

    public long getWeeklyAllotment();

    public boolean isAdmin();

    /**
     * Returns the deleted.
     *
     * @return boolean
     */
    public boolean isDeleted();

    public boolean isExempt();

    public boolean isGroupAdmin();

    public boolean isMemberOf(String group);

    /**
     * Returns the nuker.
     *
     * @return boolean
     */
    public boolean isNuker();

    /**
     * User login.
     */
    public void login();

    /**
     * User logout
     */
    public void logout();

    public void purge();

    public void removeIpMask(String mask) throws NoSuchFieldException;

    public void removeSecondaryGroup(String group) throws NoSuchFieldException;

    public void rename(String username)
        throws UserExistsException, UserFileException;

    public void reset(ConnectionManager manager) throws UserFileException;

    public void setComment(String comment);

    public void setCreated(long created);

    /**
     * Sets the credits.
     *
     * @param credits
     *            The credits to set
     */
    public void setCredits(long credits);

    /**
     * Sets the deleted.
     *
     * @param deleted
     *            The deleted to set
     */
    public void setDeleted(boolean deleted);

    public void setDownloadedBytes(long bytes);

    public void setDownloadedBytesDay(long bytes);

    public void setDownloadedBytesForTrialPeriod(int period, long bytes);

    public void setDownloadedBytesMonth(long bytes);

    public void setDownloadedBytesWeek(long bytes);

    public void setDownloadedFiles(int files);

    public void setDownloadedFilesDay(int files);

    public void setDownloadedFilesForTrialPeriod(int period, int files);

    public void setDownloadedFilesMonth(int files);

    public void setDownloadedFilesWeek(int files);

    public void setDownloadedMilliSeconds(long millis);

    public void setDownloadedMillisecondsDay(long millis);

    public void setDownloadedMillisecondsForTrialPeriod(int i, long millis);

    public void setDownloadedMillisecondsMonth(long millis);

    public void setDownloadedMillisecondsWeek(long millis);

    public void setDownloadedSeconds(int millis);

    public void setDownloadedSecondsDay(int millis);

    public void setDownloadedSecondsMonth(int millis);

    public void setDownloadedSecondsWeek(int millis);

    public void setGroup(String group);

    public void setGroupLeechSlots(short s);

    public void setGroupSlots(short s);

    /**
     * Sets the idleTime.
     *
     * @param idleTime
     *            The idleTime to set
     */
    public void setIdleTime(int idleTime);

    /**
     * Sets the lastAccessTime.
     *
     * @param lastAccessTime
     *            The lastAccessTime to set
     */
    public void setLastAccessTime(long lastAccessTime);

    /**
     * Sets the logins.
     *
     * @param logins
     *            The logins to set
     */
    public void setLogins(int logins);

    /**
     * Sets the maxLogins.
     *
     * @param maxLogins
     *            The maxLogins to set
     */
    public void setMaxLogins(int maxLogins);

    /**
     * Sets the maxLoginsPerIP.
     *
     * @param maxLoginsPerIP
     *            The maxLoginsPerIP to set
     */
    public void setMaxLoginsPerIP(int maxLoginsPerIP);

    //    /**
    //     * Sets the nukedBytes.
    //     *
    //     * @param nukedBytes
    //     *            The nukedBytes to set
    //     */
    //    public void setNukedBytes(long nukedBytes);
    public void setPassword(String password);

    /**
     * Sets the ratio.
     *
     * @param ratio
     *            The ratio to set
     */
    public void setRatio(float ratio);

    //    public void setTimesNuked(int nuked);
    public void setUploadedBytes(long bytes);

    public void setUploadedBytesDay(long bytes);

    public void setUploadedBytesForTrialPeriod(int i, long l);

    public void setUploadedBytesMonth(long bytes);

    public void setUploadedBytesWeek(long bytes);

    public void setUploadedFiles(int files);

    public void setUploadedFilesDay(int files);

    public void setUploadedFilesForTrialPeriod(int period, int files);

    public void setUploadedFilesMonth(int files);

    public void setUploadedFilesWeek(int files);

    public void setUploadedMilliseconds(long millis);

    public void setUploadedMillisecondsDay(long millis);

    public void setUploadedMillisecondsForTrialPeriod(int i, long millis);

    public void setUploadedMillisecondsMonth(long millis);

    public void setUploadedMillisecondsWeek(long millis);

    public void setUploadedSeconds(int millis);

    public void setUploadedSecondsDay(int millis);

    public void setUploadedSecondsMonth(int millis);

    public void setUploadedSecondsWeek(int millis);

    public void setWeeklyAllotment(long weeklyAllotment);

    public void toggleGroup(String string);

    public void updateCredits(long credits);

    public void updateDownloadedBytes(long bytes);

    public void updateDownloadedFiles(int i);

    public void updateDownloadedMilliseconds(long millis);

    /**
     * Hit user - update last access time
     */
    public void updateLastAccessTime();

    //    public void updateNukedBytes(long bytes);
    //
    //    public void updateTimesNuked(int timesNuked);
    public void updateUploadedBytes(long bytes);

    public void updateUploadedFiles(int i);

    public void updateUploadedMilliseconds(long millis);

    public void putObject(Key key, Object obj);

    public Object getObject(Key key) throws KeyNotFoundException;

    public Object getObject(Key key, Object def);

    public void incrementObjectLong(Key key);

    public String getObjectString(Key key);

    public int getObjectInt(Key key);

    public void incrementObjectInt(Key nuked, int i);

    /**
     * @param fullCommandArgument
     */
    public void setTagline(String fullCommandArgument);

    /**
     * @return
     */
    public String getTagline();

    /**
     * @return
     */
    public int getTimesNuked();

    /**
     * @return
     */
    public long getNukedBytes();

    /**
     * @param nukedBytes
     */
    public void setNukedBytes(long nukedBytes);

    /**
     * @param timesNuked
     */
    public void setTimesNuked(int timesNuked);

    /**
     * @param lastnuked
     * @return
     */
    public long getObjectLong(Key lastnuked);
}