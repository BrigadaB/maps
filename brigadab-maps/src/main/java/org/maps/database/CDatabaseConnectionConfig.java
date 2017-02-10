package org.maps.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;

import commonlibs.commonclasses.CLanguage;
import commonlibs.extendedlogger.CExtendedLogger;

public class CDatabaseConnectionConfig implements Serializable {

    private static final long serialVersionUID = 3434139873022621518L;

    protected String strDriver = null;
    protected String strPrefix = null;
    protected String strHost = null;
    protected String strPort = null;
    protected String strDatabase = null;
    protected String strUser = null;
    protected String strPassword = null;

    public CDatabaseConnectionConfig( String strDriver, String strPrefix, String strHost, String strPort, String strDatabase, String strUser, String strPassword ) {
        
        this.strDriver = strDriver;
        this.strPrefix = strPrefix;
        this.strHost = strHost;
        this.strPort = strPort;
        this.strDatabase = strDatabase;
        this.strUser = strUser;
        this.strPassword = strPassword;
        
    }

    public CDatabaseConnectionConfig( ) {
        
    }
    

    public boolean loadConfig ( String strConfigPath, CExtendedLogger localLogger, CLanguage localLanguage ){
    	
    	boolean bResult = false;
    	
    	try  {
    		
    		File configFilePath = new File (strConfigPath); 
    		if (configFilePath.exists()) {
    			
    			Properties configsData= new Properties ();
    			FileInputStream inputStream = new FileInputStream (configFilePath);
    			configsData.loadFromXML( inputStream );
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Readed config values from file [%s]" ,  strConfigPath ) );
    			this.strDriver = configsData.getProperty("driver");
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Loaded value for [%s] [%s]", "driver",  this.strDriver ) );
    			this.strPrefix = configsData.getProperty("prefix");
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Loaded value for [%s] [%s]", "driver",  this.strPrefix ) );
    			this.strHost = configsData.getProperty("host");
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Loaded value for [%s] [%s]", "driver",  this.strHost ) );
    		    this.strPort = configsData.getProperty("port");
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Loaded value for [%s] [%s]", "driver",  this.strPort ) );
    		    this.strDatabase = configsData.getProperty("database");
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Loaded value for [%s] [%s]", "driver",  this.strDatabase ) );
    		    this.strUser = configsData.getProperty("user");
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Loaded value for [%s] [%s]", "driver",  this.strUser ) );
    		    this.strPassword = configsData.getProperty("password");
                localLogger.logMessage( "1" , CLanguage.translateIf( localLanguage, "Loaded value for [%s] [%s]", "driver",  this.strPassword ) );
    		    inputStream.close();
    		    
    		    bResult = true;
    			
    		} else 
               if ( localLogger != null ) {
                    
                    localLogger.logError( "-1001" , CLanguage.translateIf( localLanguage, "Config file in path [%s] not found" ,  strConfigPath ) );
             			
    		}
    		
    	} 
    	catch (Exception ex) 	{
    	      if ( localLogger != null ) {
                  localLogger.logException( "-1021" , ex.getMessage(), ex );
              }
    	}
    	return bResult ;
    }
    
    
    
    public String getDriver() {
		return strDriver;
	}

	public void setDriver(String strDriver) {
		this.strDriver = strDriver;
	}

	public String getPrefix() {
		return strPrefix;
	}

	public void setPrefix(String strPrefix) {
		this.strPrefix = strPrefix;
	}

	public String getHost() {
		return strHost;
	}

	public void setHost(String strHost) {
		this.strHost = strHost;
	}

	public String getPort() {
		return strPort;
	}

	public void setPort(String strPort) {
		this.strPort = strPort;
	}

	public String getDatabase() {
		return strDatabase;
	}

	public void setDatabase(String strDatabase) {
		this.strDatabase = strDatabase;
	}

	public String getUser() {
		return strUser;
	}

	public void setUser(String strUser) {
		this.strUser = strUser;
	}

	public String getPassword() {
		return strPassword;
	}

	public void setPassword(String strPassword) {
		this.strPassword = strPassword;
	}   
    
    
}