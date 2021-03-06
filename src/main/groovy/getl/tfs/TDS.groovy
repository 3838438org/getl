/*
 GETL - based package in Groovy, which automates the work of loading and transforming data. His name is an acronym for "Groovy ETL".

 GETL is a set of libraries of pre-built classes and objects that can be used to solve problems unpacking,
 transform and load data into programs written in Groovy, or Java, as well as from any software that supports
 the work with Java classes.
 
 Copyright (C) 2013-2015  Alexsey Konstantonov (ASCRUS)

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License and
 GNU Lesser General Public License along with this program.
 If not, see <http://www.gnu.org/licenses/>.
*/

package getl.tfs

import groovy.transform.InheritConstructors
import getl.h2.*
import getl.jdbc.*
import getl.data.*
import getl.exception.*
import getl.utils.*
import org.h2.tools.DeleteDbFiles

/**
 * Temporary data storage manager class
 * @author Alexsey Konstantinov
 *
 */
@InheritConstructors
class TDS extends H2Connection {
	TDS () {
		super(defaultParams)
		
		if (connectURL == null && params."inMemory" == null) inMemory = true
		if (connectURL == null && connectDatabase == null) connectDatabase = "getl"
		if (login == null && password == null) {
			login = "easyload"
			password = "easydata"
		}
		if (connectProperty."PAGE_SIZE" == null) {
			connectProperty."PAGE_SIZE" = 8192
		}
		if (connectProperty."LOG" == null) {
			connectProperty."LOG" = 0
		}
		if (connectProperty."UNDO_LOG" == null) {
			connectProperty."UNDO_LOG" = 0
		}
		if (connectProperty."MVCC" == null) {
			connectProperty."MVCC" = false
		}
		config = "getl_tds"
	}
	
	TDS (Map initParams) {
		super(defaultParams + initParams)
		
		if (this.getClass().name == 'getl.tfs.TDS') methodParams.validation("Super", params)
		
		if (connectURL == null && params."inMemory" == null) inMemory = true
		if (connectURL == null && connectDatabase == null) {
			if (inMemory) {
				connectDatabase = "getl"
			}
			else {
                tempPath = TFS.systemPath
				connectDatabase = "$tempPath/getl"
			}
		}
		if (login == null && password == null) {
			login = "easyload"
			password = "easydata"
		}
		if (connectProperty."PAGE_SIZE" == null) {
			connectProperty."PAGE_SIZE" = 8192
		}
		if (connectProperty."LOG" == null) {
			connectProperty."LOG" = 0
		}
		if (connectProperty."UNDO_LOG" == null) {
			connectProperty."UNDO_LOG" = 0
		}
		if (connectProperty."MVCC" == null) {
			connectProperty."MVCC" = false
		}
		if (params."config" == null) config = "getl_tds"
	}

    /**
     * Temp path of database file
     */
    private String tempPath
	
	/**
	 * Internal name in config section
	 */
	protected String internalConfigName() { "getl_tds" }
	
	/**
	 * Default parameters
	 */
	public static Map defaultParams = [:]
	
	@Override
	protected void onLoadConfig (Map configSection) {
		super.onLoadConfig(configSection)
		if (this.getClass().name == 'getl.tfs.TDS') methodParams.validation("Super", params)
	}
	
	@Override
	protected void doBeforeConnect () {
		super.doBeforeConnect()
		if (connectHost == null) {
			connectProperty."IFEXISTS" = "FALSE"
		}
		if (inMemory && connectProperty."DB_CLOSE_DELAY" == null) connectProperty."DB_CLOSE_DELAY" = -1
		autoCommit = true
	}

    @Override
    protected void doDoneDisconnect () {
        super.doDoneDisconnect()
        if (tempPath != null) {
            DeleteDbFiles.execute(tempPath, 'getl', true)
        }
    }
	
	/**
	 * Generate new table from temporary data stage
	 * @param params
	 * @return
	 */
	public static TableDataset dataset (Map initParams) {
		TDS con = new TDS()
		con.newDataset(initParams)
	}
	
	/**
	 * Generate new table from temporary data stage
	 * @return
	 */
	public static TableDataset dataset () {
		dataset(null)
	}
	
	/**
	 * Generate new table
	 * @param initParams
	 * @return
	 */
	public TableDataset newDataset (Map initParams) {
		def p = [:]
		p.connection = this
		p.tableName = "TDS_" + StringUtils.RandomStr().replace("-", "_").toUpperCase()
		if (initParams != null) p.putAll(initParams)
		TableDataset t = new TableDataset(p)

		t
	}
	
	/**
	 * Generate new table
	 * @return
	 */
	public TableDataset newDataset () {
		newDataset(null)
	}
}
