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
package org.drftpd.protocol.imdb.slave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.drftpd.protocol.slave.AbstractHandler;
import org.drftpd.protocol.slave.SlaveProtocolCentral;
import org.drftpd.protocol.imdb.common.IMDBInfo;
import org.drftpd.protocol.imdb.common.async.AsyncResponseIMDBInfo;
import org.drftpd.slave.Slave;
import org.drftpd.slave.async.AsyncCommandArgument;
import org.drftpd.slave.async.AsyncResponse;
import org.drftpd.slave.async.AsyncResponseException;

/**
 * Handler for NFO requests.
 * @author lh
 */
public class IMDBHandler extends AbstractHandler {
	
	public IMDBHandler(SlaveProtocolCentral central) {
		super(central);
	}

	public AsyncResponse handleIMDBFile(AsyncCommandArgument ac) {
		try {
			return new AsyncResponseIMDBInfo(ac.getIndex(),
					getIMDBFile(getSlaveObject(), getSlaveObject().mapPathToRenameQueue(ac.getArgs())));
		} catch (IOException e) {
			return new AsyncResponseException(ac.getIndex(), e);
		}
	}

	private IMDBInfo getIMDBFile(Slave slave, String path) throws IOException {
		BufferedReader reader = null;
		File file = slave.getRoots().getFile(path);
		CRC32 checksum = new CRC32();
		CheckedInputStream in = null;
		try {
			in = new CheckedInputStream(new FileInputStream(file), checksum);
			byte[] buf = new byte[4096];
			while (in.read(buf) != -1) {
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		try {
			reader = new BufferedReader(new FileReader(file));
			IMDBInfo imdbInfo = IMDBInfo.importNFOInfoFromFile(reader);
			imdbInfo.setNFOFileName(file.getName());
			imdbInfo.setChecksum(checksum.getValue());
			return imdbInfo;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
}
