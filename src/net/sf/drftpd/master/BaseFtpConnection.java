package net.sf.drftpd.master;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Properties;

//import ranab.util.Message;
/*import ranab.io.StreamConnectorObserver;*/

/**
 * This is a generic ftp connection handler. It delegates 
 * the request to appropriate methods in subclasses.
 *
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public class BaseFtpConnection implements Runnable {

	protected final static Class[] METHOD_INPUT_SIG =
		new Class[] { FtpRequest.class, Writer.class };

	//protected FtpConfig mConfig                 = null;
	protected Properties cfg;
	/**
	 * @todo Put back shared FtpStatus
	 */
	protected FtpStatus mFtpStatus = null;
	//protected FtpDataConnection mDataConnection = null;
	protected FtpUser mUser = null;
	//protected SpyConnectionInterface mSpy       = null;
	//protected FtpConnectionObserver mObserver   = null;
	protected Socket mControlSocket = null;
	protected Writer mWriter = null;
	protected boolean mbStopRequest = false;

	protected SlaveManager slavemanager;
	public void setSlaveManager(SlaveManager slavemanager) {
		this.slavemanager = slavemanager;
	}
	/**
	 * Set configuration file and the control socket.
	 */
	/*
	public BaseFtpConnection(FtpConfig ftpConfig, Socket soc) {
	    //mConfig = ftpConfig;
	    //mFtpStatus = mConfig.getStatus();
	mFtpStatus = new FtpStatus();
	    mControlSocket = soc;
	    mUser = new FtpUser();
	}
	*/
	public BaseFtpConnection(Socket soc, Properties cfg) {
		//mConfig = ftpConfig;
		this.cfg = cfg;
		//mFtpStatus = mConfig.getStatus();
		mFtpStatus = new FtpStatus();
		mControlSocket = soc;
		//mUser = new FtpUser();
	}

	/**
	 * Server one FTP connection.
	 */
	public void run() {
		InetAddress clientAddress = mControlSocket.getInetAddress();
		//mConfig.getLogger().info("Handling new request from " + clientAddress.getHostAddress());
		System.out.println(
			"Handling new request from " + clientAddress.getHostAddress());
		//mDataConnection = new FtpDataConnection(mConfig);
		mUser.setClientAddress(clientAddress);
		//mConfig.getConnectionService().newConnection(this);

		BufferedReader in = null;
		try {
			in =
				new BufferedReader(
					new InputStreamReader(mControlSocket.getInputStream()));

			//mWriter = new FtpWriter(mControlSocket, mConfig);                    
			mWriter = new FtpWriter(mControlSocket.getOutputStream());

			// permission check
			/*
			    if( !mConfig.getIpRestrictor().hasPermission(mControlSocket.getInetAddress()) ) {
			        mWriter.write(mFtpStatus.getResponse(530, null, mUser, null));
			        return;
			    }
			*/
			mWriter.write(mFtpStatus.getResponse(220, null, mUser, null));

			do {
				//notifyObserver();
				String commandLine = in.readLine();

				// test command line
				if (commandLine == null) {
					break;
				}

				//spyRequest(commandLine);
				if (commandLine.equals("")) {
					continue;
				}

				FtpRequest request = new FtpRequest(commandLine);
				/*
				        if(!hasPermission(request)) {
				            mWriter.write(mFtpStatus.getResponse(530, request, mUser, null));
				            break;
				        }
				*/
				// execute command
				service(request, mWriter);
			} while (!mbStopRequest);
		} catch (SocketException ex) {
			System.out.println(ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
				mWriter.close();
			} catch (Exception ex2) {
				System.err.println("Warning, exception closing stream");
				ex2.printStackTrace();
			}
			/*
			ConnectionService conService = mConfig.getConnectionService();
			if (conService != null) {
			    conService.closeConnection(mUser.getSessionId());
			}
			*/
		}
	}

	/**
	 * Execute the ftp command.
	 */
	public void service(FtpRequest request, Writer writer) throws IOException {
		System.out.println("<< " + request.getCommandLine());
		try {
			String metName;
			if (request.getCommand().equals("SITE")) {
				int index = request.getArgument().indexOf(" ");
				if (index == -1) {
					metName = "doSITE"+request.getArgument().toUpperCase();
				} else {
					metName = "doSITE"+request.getArgument().substring(0, index).toUpperCase();
				}
			} else {
				metName = "do" + request.getCommand();
			}
			System.out.println("!!! " + metName);
			Method actionMet =
				getClass().getDeclaredMethod(metName, METHOD_INPUT_SIG);
			actionMet.invoke(this, new Object[] { request, writer });
		} catch (NoSuchMethodException ex) {
			writer.write(mFtpStatus.getResponse(502, request, mUser, null));
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
			writer.write(mFtpStatus.getResponse(500, request, mUser, null));
			Throwable th = ex.getTargetException();
			if (th instanceof java.io.IOException) {
				throw (IOException) th;
			}
			/*
			    else {
			       mConfig.getLogger().warn(th);
			    }
			*/
		} catch (Exception ex) {
			writer.write(mFtpStatus.getResponse(500, request, mUser, null));
			ex.printStackTrace();
			if (ex instanceof java.io.IOException) {
				throw (IOException) ex;
			}
			/*
			    else {
			       mConfig.getLogger().warn(ex);
			    }
			*/
		}
	}

	/**
	 * Check permission - default implementation - does nothing.
	 */
	protected boolean hasPermission(FtpRequest request) {
		return true;
	}

	/**
	 * User logout and stop this thread.
	 */
	public void stop() {
		mbStopRequest = true;
		/*
		if (mDataConnection != null) {
		    mDataConnection.reset();
		    mDataConnection = null;
		}
		*/
		if (mControlSocket != null) {
			try {
				mControlSocket.close();
			} catch (Exception ex) {
			}
			mControlSocket = null;
		}
		if (mUser.hasLoggedIn()) {
			mUser.logout();
		}
		//mObserver = null;
	}

	/**
	 * Is the connection closed?
	 */
	public boolean isClosed() {
		return mbStopRequest;
	}

	/**
	 * Monitor the user request.
	 */
	/*
	protected void spyRequest(final String str) {
	    final SpyConnectionInterface spy = mSpy;
	    if (spy != null) {
	        Message msg = new Message() {
	            public void execute() {
	                try {
	                    spy.request(str + '\n');
	                }
	                catch(Exception ex) {
	                    mSpy = null;
	                    mConfig.getLogger().error(ex);
	                }
	            }
	        };
	        mConfig.getMessageQueue().add(msg);
	    }
	}
	*/
	/**
	 * Get user object
	 */
	/*
	public FtpUser getUser() {
	    return mUser;
	}
	*/

	/**
	 * Get connection spy object
	 */
	/*
	public SpyConnectionInterface getSpyObject() {
	    return mSpy;
	}
	*/

	/**
	 * Set spy object
	 */
	/*
	public void setSpyObject(SpyConnectionInterface spy) {
	    mWriter.setSpyObject(spy);
	    mSpy = spy;
	}
	*/

	/**
	 * Get observer
	 */
	/*
	public FtpConnectionObserver getObserver() {
	    return mObserver;
	}
	*/
	/**
	 * Set observer
	 */
	/*
	public void setObserver(FtpConnectionObserver obsr) {
	    mObserver = obsr;
	}
	*/

	/**
	 * Notify observer.
	 */
	/*
	public void notifyObserver() {
	   mUser.hitUser();
	   final FtpUser thisUser = mUser; 
	   final FtpConnectionObserver obsr = mObserver;
	
	   if (obsr != null) {
	        Message msg = new Message() {
	            public void execute() {
	                obsr.updateConnection(thisUser);
	            }
	        };
	        mConfig.getMessageQueue().add(msg);
	   }
	}
	*/
	/**
	 * This method tracks data transfer.
	 */
	public void dataTransferred(int sz) {
		//notifyObserver();
	}

	/**
	 * Get config object
	 */
	/*
	public FtpConfig getConfig() {
	    return mConfig;
	} 
	*/

	/**
	 * Get status object
	 */
	public FtpStatus getStatus() {
		return mFtpStatus;
	}
	/***** DATA CONNECTION *****/
	protected Socket mDataSoc = null;
	protected ServerSocket mServSoc = null;
	protected InetAddress mAddress = null;
	protected int miPort = 0;

	protected boolean mbPort = false;
	protected boolean mbPasv = false;

	/**
	 * Constructor.
	 * @param cfg ftp config object.
	 */
	/*
	public FtpDataConnection(FtpConfig cfg) {
	    mConfig = cfg;
	}
	*/

	/**
	 * Reset all the member variables. Close all sockets.
	 */
	public void reset() {

		// close data socket
		if (mDataSoc != null) {
			try {
				mDataSoc.close();
			} catch (Exception ex) {
				//mConfig.getLogger().warn(ex);
				ex.printStackTrace();
			}
			mDataSoc = null;
		}

		// close server socket
		if (mServSoc != null) {
			try {
				mServSoc.close();
			} catch (Exception ex) {
				//mConfig.getLogger().warn(ex);
				ex.printStackTrace();
			}
			mServSoc = null;
		}

		// reset other variables
		mAddress = null;
		miPort = 0;

		mbPort = false;
		mbPasv = false;
	}

	/**
	 * Port command.
	 */
	public void setPortCommand(InetAddress addr, int port) {
		reset();
		mbPort = true;
		mAddress = addr;
		miPort = port;
	}

	/**
	 * Passive command. It returns the success flag.
	 */
	public boolean setPasvCommand() {
		boolean bRet = false;
		try {
			reset();
			//mAddress = mConfig.getSelfAddress();
			//mServSoc = new ServerSocket(0, 1, mAddress);
			mServSoc = new ServerSocket(0, 1);
			mServSoc.setSoTimeout(60000);
			miPort = mServSoc.getLocalPort();
			mbPasv = true;
			bRet = true;
		} catch (Exception ex) {
			//mConfig.getLogger().warn(ex);
			ex.printStackTrace();
		}
		return bRet;
	}

	/**
	 * Listen for passive socket connection. It returns the success flag.
	 */
	public boolean listenPasvConnection() {
		boolean bRet = false;
		mDataSoc = null;
		try {
			mDataSoc = mServSoc.accept();
			mDataSoc.setSoTimeout(60000);
			bRet = true;
		} catch (Exception ex) {
			//mConfig.getLogger().warn(ex);
		}
		return bRet;
	}

	/**
	 * Get client address.
	 */
	public InetAddress getInetAddress() {
		return mAddress;
	}

	/**
	 * Get port number.
	 */
	public int getPort() {
		return miPort;
	}

	/**
	 * Get the data socket. In case of error returns null.
	 */
	public Socket getDataSocket() {

		// get socket depending on the selection
		if (mbPort) {
			try {
				mDataSoc = new Socket(mAddress, miPort);
				mDataSoc.setSoTimeout(60000);
			} catch (Exception ex) {
				//mConfig.getLogger().warn(ex);
				ex.printStackTrace();
				mDataSoc = null;
			}
		} else if (!mbPasv) {
			if (mDataSoc != null) {
				try {
					mDataSoc.close();
				} catch (Exception ex) {
					//mConfig.getLogger().warn(ex);
					ex.printStackTrace();
				}
				mDataSoc = null;
			}
		}

		// result check
		return mDataSoc;
	}

	/**
	 * Last defense - close connections.
	 */
	protected void finalize() throws Throwable {
		reset();
		super.finalize();
	}
}
