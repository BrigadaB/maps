package org.maps.zksubsystem;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.maps.constant.SystemConstants;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zk.ui.util.DesktopInit;
import org.zkoss.zk.ui.util.ExecutionCleanup;
import org.zkoss.zk.ui.util.ExecutionInit;
import org.zkoss.zk.ui.util.SessionCleanup;
import org.zkoss.zk.ui.util.SessionInit;
import org.zkoss.zk.ui.util.WebAppCleanup;
import org.zkoss.zk.ui.util.WebAppInit;

import commonlibs.commonclasses.CLanguage;
import commonlibs.commonclasses.ConstantsCommonClasses;
import commonlibs.extendedlogger.CExtendedConfigLogger;
import commonlibs.extendedlogger.CExtendedLogger;

public class CZKSubsystemEvents implements DesktopInit, DesktopCleanup, SessionInit, SessionCleanup, WebAppInit, WebAppCleanup, ExecutionInit, ExecutionCleanup {


	public void cleanup(Execution execution0, Execution execution1, List<Throwable> arg2) throws Exception {
		System.out.println("Execution Cleanup");
		
	}
	

	public void init(Execution execution0, Execution execution1) throws Exception {
		System.out.println("Execution Init");
		
	}


	public void cleanup(WebApp webApp) throws Exception {
		System.out.println("Web Cleanup");
		try {
			//Obtenemos el logger
			CExtendedLogger webAppLogger = CExtendedLogger.getLogger(ConstantsCommonClasses._Webapp_Logger_Name);
			if (webAppLogger != null) {
				//escribimos un mensaje al log
			  webAppLogger.logMessage("1", CLanguage.translateIf(null, "Webapp ending now."));
			  //cerramos el log
			  webAppLogger.flushAndClose();
			}
			//eliminamos el attributo del webapp
			webApp.removeAttribute(ConstantsCommonClasses._Webapp_Logger_Name);

		} 
		catch (Exception ex){
			System.out.println(ex.getMessage());
		} 
}
		


	public void init(WebApp webApp) throws Exception {
		System.out.println("Web Init");
		try {
			String strRunningPath = webApp.getRealPath(ConstantsCommonClasses._WEB_INF_Dir) + File.separator;
		
			CExtendedConfigLogger configLogger = new CExtendedConfigLogger ();
			String strConfigPath = strRunningPath + ConstantsCommonClasses._Config_Dir + ConstantsCommonClasses._Logger_Config_File_Name;
			if  ( configLogger.loadConfig(strConfigPath, null, null)) {
				//Aqui creamos el logger como tal
				CExtendedLogger webAppLogger = CExtendedLogger.getLogger(ConstantsCommonClasses._Webapp_Logger_Name);
				if ( webAppLogger.getSetupSet() == false) {   //preguntamos si todavia no esta configurado
					//Aquí le decimos donde va a crear los achivos de log WEB-INF/logs/system
					String strLogPath = strRunningPath + ConstantsCommonClasses._Logs_Dir + ConstantsCommonClasses._System_Dir;
					//Configuramos el logger según los parámetros de el archivo logger.config.xml y la ruta para escribir los archivos de log
					webAppLogger.setupLogger(configLogger.getInstanceID(), configLogger.getLogToScreen(), strLogPath, ConstantsCommonClasses._Webapp_Logger_File_Log, configLogger.getClassNameMethodName(), configLogger.getExactMatch(), configLogger.getLevel(), configLogger.getLogIP(), configLogger.getLogPort(), configLogger.getHTTPLogURL(), configLogger.getHTTPLogUser(), configLogger.getHTTPLogPassword(), configLogger.getProxyIP(), configLogger.getProxyPort(), configLogger.getProxyUser(), configLogger.getProxyPassword());
					//Guardamos el logger principal en un atributo del webapp
					webApp.setAttribute(ConstantsCommonClasses._Webapp_Logger_App_Attribute_Key, webAppLogger);                  
				}
				//Aquí escribimos al log en un archivo en WEB-INF/logs/system/webapplogger.log
                //Fijense en la clase CLanguage pero no le presten mucha atención todavía
                //Es una clase que permite escribir los mensajes del log en varios idiomas
          webAppLogger.logMessage("1", CLanguage.translateIf(null, "Webapp logger loaded and configured [%s].",ConstantsCommonClasses._Webapp_Logger_Name ) );
			}
		}
        catch ( Exception ex ) {
            System.out.println( ex.getMessage() );
            }  //enviamos a la consola el mensaje por no tener log todavia
	}


	public void cleanup(Session session) throws Exception {
		System.out.println("Session Cleanup");
		
		@SuppressWarnings("unchecked")
		LinkedList<String> loggedSessionLoggers = (LinkedList<String>) session.getAttribute( SystemConstants._Logged_Session_Loggers );
		
		for ( String strLoggerName : loggedSessionLoggers ) {
			
			CExtendedLogger currentLogger = CExtendedLogger.getLogger(strLoggerName);
			
			currentLogger.flushAndClose(); //Cerrar el logger
		}
	  //vaciar la lista
		loggedSessionLoggers.clear();
	}


	public void init(Session session, Object object) throws Exception {
		System.out.println("Session Init");
		
	}


	public void cleanup(Desktop desktop) throws Exception {
		System.out.println("Desktop Cleanup");

		
	}


	public void init(Desktop desktop, Object object) throws Exception {
		System.out.println("Desktop Init");

		
	}

}
