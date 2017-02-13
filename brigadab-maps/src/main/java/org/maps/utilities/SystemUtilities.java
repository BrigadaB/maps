package org.maps.utilities;

import java.io.File;

import org.maps.constant.SystemConstants;
import org.zkoss.zk.ui.Session;

import commonlibs.extendedlogger.CExtendedConfigLogger;
import commonlibs.extendedlogger.CExtendedLogger;

public class SystemUtilities {
	
	public static CExtendedConfigLogger initLoggerConfig( String strRunningPath, Session currentSession ) {
		
		//tratamos de cargar la configuracion de los logger de la sesión
		CExtendedConfigLogger result = (CExtendedConfigLogger) currentSession.getAttribute(SystemConstants._Config_Logger_Session_Key);
		
		if (result == null) {
			
			//En caso de no encontrarse en la sesión lo leemos del archivo de configuración
			result = new CExtendedConfigLogger();
			
			String strConfigPath = strRunningPath + SystemConstants._CONFIG_Dir;
			
			//Localizamos el logger del webApp el cual para este código ya debe estar presente se inicia en CZKSubsystemEvents.
			CExtendedLogger webAppLogger = CExtendedLogger.getLogger(SystemConstants._Webapp_Logger_Name);
			
			//leemos la configuracion del archivo
			if (result.loadConfig(strConfigPath + File.separator + SystemConstants._Logger_Config_File_Name, webAppLogger, null)==false) {
				
				result = null; //en caso de falla no devolvemos nada
			}
			
		} 
		
		return result;
		
	}

}
