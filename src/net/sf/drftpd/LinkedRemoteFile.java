package net.sf.drftpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * Represents the file attributes of a remote file.
 * 
 * @author Morgan Christiansson <mog@linux.nu>
 */
public class LinkedRemoteFile extends RemoteFile implements Serializable {

	/**
	 * Creates an empty RemoteFile directory, usually used as an empty root directory that
	 * <link>{merge()}</link> can be called on.
	 */
	public LinkedRemoteFile() {
		canRead = true;
		canWrite = false;
		lastModified = System.currentTimeMillis();
		length = 0;
		isDirectory = true;
		isFile = false;
		parent = null;
		name = "";
		files = new Hashtable();
		slaves = new Vector(1); // there will always be at least 1 RemoteSlave.
	}

	/**
	 * The slave argument may be null, if it is null, no slaves will be added.
	 */
	public LinkedRemoteFile(RemoteSlave slave, File file) {
		this(slave, (LinkedRemoteFile) null, file);
	}

	/**
	 * Creates a RemoteFile from file or creates a directory tree representation.
	 * @param file file that this RemoteFile object should represent.
	 */
	public LinkedRemoteFile(
		RemoteSlave slave,
		LinkedRemoteFile parent,
		File file) {
		canRead = file.canRead();
		canWrite = file.canWrite();
		lastModified = file.lastModified();
		length = file.length();
		//isHidden = file.isHidden();
		isDirectory = file.isDirectory();
		isFile = file.isFile();
		if (parent == null) {
			name = "";
		} else {
			name = file.getName();
		}
		//path = file.getPath();
		/* serialize directory*/
		this.parent = parent;

		slaves = new Vector(1);
		if (slave != null) {
			slaves.add(slave);
		}
		if (isDirectory()) {
			try {
				if (!file.getCanonicalPath().equals(file.getAbsolutePath())) {
					System.out.println(
						"NOT following possible symlink: "
							+ file.getAbsolutePath());
					return;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			/* get existing file entries */
			File cache = new File(file.getPath() + "/.drftpd");
			Hashtable oldtable = null;
			try {
				ObjectInputStream is =
					new ObjectInputStream(new FileInputStream(cache));
				oldtable = (Hashtable) is.readObject();
			} catch (FileNotFoundException ex) {
				//it's ok if it doesn't exist
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				// this class must exist
				ex.printStackTrace();
				System.exit(-1);
			}
			/* END get existing file entries*/

			File dir[] = file.listFiles(new DrftpdFileFilter());
			files = new Hashtable(dir.length);
			Stack dirstack = new Stack();
			for (int i = 0; i < dir.length; i++) {
				File file2 = dir[i];
				//				System.out.println("III " + file2);
				if (file2.isDirectory()) {
					dirstack.push(file2);
					continue;
				}
				LinkedRemoteFile oldfile = null;
				if (oldtable != null)
					oldfile = (LinkedRemoteFile) oldtable.get(file.getName());
				if (oldfile != null) {
					files.put(file2.getName(), oldfile);
				} else {
					files.put(
						file2.getName(),
						new LinkedRemoteFile(slave, this, file2));
				}
			}

			/*
					//don't need to serialize/cache old files... we won't save any additional data about them anyway..
						try {
							new ObjectOutputStream(
								new FileOutputStream(cache)).writeObject(
								files);
						} catch (FileNotFoundException ex) {
							System.out.println("Could not open file: " + ex.getMessage());
						} catch (Exception ex) {
							ex.printStackTrace();
						}
			*/
			// OK, now the Object is saved, continue with serializing the dir's
			Enumeration e = dirstack.elements();
			while (e.hasMoreElements()) {
				File file2 = (File) e.nextElement();
				String filename = file2.getName();
				//				System.out.println(">>> " + file2.getName());
				if (oldtable != null) {
					LinkedRemoteFile oldfile =
						(LinkedRemoteFile) oldtable.get(filename);
					if (oldfile != null) {
						files.put(filename, oldfile);
					} else {
						files.put(
							filename,
							new LinkedRemoteFile(slave, this, file2));
					}
				} else {
					files.put(
						filename,
						new LinkedRemoteFile(slave, this, file2));
				}
			}
		} /* serialize directory */
	}

	public LinkedRemoteFile[] listFiles() {
		return (LinkedRemoteFile[]) files.values().toArray(
			new LinkedRemoteFile[0]);
	}

	public LinkedRemoteFile lookupFile(String path)
		throws FileNotFoundException {
		StringTokenizer st = new StringTokenizer(path, "/");
		LinkedRemoteFile currfile = this;
		while (st.hasMoreTokens()) {
			String nextToken = st.nextToken();
			currfile =
				(LinkedRemoteFile) currfile.getHashtable().get(nextToken);
			if (currfile == null)
				throw new FileNotFoundException();
		}
		return currfile;
	}

	/**
	 * A remote file never exists locally, therefore we return false.
	 * If the current RemoteSlave has the file this call could return true
	 * but as we don't know the root directory it's not possible right now.
	 */
	public boolean exists() {
		return false;
	}

	private Hashtable files;
	public Hashtable getHashtable() {
		return files;
	}

	private LinkedRemoteFile parent;
	public LinkedRemoteFile getParentFile() {
		return parent;
	}
	public String getParent() {
		if (getParentFile() == null)
			return null;
		return getParentFile().getPath();
	}

	//private String path;
	private String name;
	public String getName() {
		//return path.substring(path.lastIndexOf(separatorChar) + 1);
		return name;
	}

	public String getPath() {
		//return path;
		StringBuffer path = new StringBuffer("/" + getName());
		LinkedRemoteFile parent = getParentFile();
		while (parent != null && parent.getParentFile() != null) {
			path.insert(0, "/" + parent.getName());
			parent = parent.getParentFile();
		}
		return path.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof LinkedRemoteFile
			&& ((LinkedRemoteFile) obj).getPath().equals(getPath())) {
			return true;
		}
		return false;
	}

	/**
	 * Merges two RemoteFile directories.
	 * If duplicates exist, the slaves are added to this object and the file-attributes of the oldest file (lastModified) are kept.
	 */
	public synchronized void merge(LinkedRemoteFile dir) {
		if (!isDirectory())
			throw new IllegalArgumentException(
				"merge() called on a non-directory: "
					+ this
					+ " argument: "
					+ dir);
		if (!dir.isDirectory())
			throw new IllegalArgumentException(
				"argument is not a directory: " + dir + " this: " + this);

		Hashtable map = getHashtable();
		Hashtable mergemap = dir.getHashtable();
		if (mergemap == null)
			return;
		// remote directory wasn't added, it might have been a symlink.

		System.out.println(
			"Adding " + dir.getPath() + " with " + mergemap.size() + " files");

		Iterator i = mergemap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			String filename = (String) entry.getKey();
			//RemoteFile file = (RemoteFile) entry.getValue();
			LinkedRemoteFile file = (LinkedRemoteFile) files.get(filename);
			LinkedRemoteFile mergefile = (LinkedRemoteFile) entry.getValue();
			//RemoteFile mergefile = (RemoteFile) mergemap.get(getName());

			// two scenarios:, local file [does not] exists
			if (file == null) {
				// local file does not exist, just put it in the hashtable
				map.put(mergefile.getName(), mergefile);
			} else {

				if (lastModified() > mergefile.lastModified()) {
					System.out.println(
						"Last modified changed from "
							+ lastModified()
							+ " to "
							+ mergefile.lastModified());
					lastModified = mergefile.lastModified();
				} else {
					System.out.println(
						"Last modified NOT changed from "
							+ lastModified()
							+ " to "
							+ mergefile.lastModified());
				}

				// 4 scenarios: new/existing file/directory
				if (mergefile.isDirectory()) {
					if (!file.isDirectory())
						throw new RuntimeException("!!! WARNING: File/Directory conflict!!");
					file.merge(mergefile);
				} else {
					if (file.isDirectory())
						throw new RuntimeException("!!! WARNING: File/Directory conflict!!");
				}
				Collection slaves2 = mergefile.getSlaves();
				file.addSlaves(slaves2);
				System.out.println("Result file: " + file);
				/*
				System.out.println("Old file: "+map.get(filename));
				map.put(filename, file);
				*/
			}
		}

		// directory backdating, do other attrbiutes need "backdating"? if so fix it! :)
		if (lastModified() > dir.lastModified()) {
			lastModified = dir.lastModified();
		}
	}
}
