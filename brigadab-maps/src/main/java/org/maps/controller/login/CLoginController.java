package org.maps.controller.login;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.zul.Label;
import org.maps.constant.SystemConstants;
import org.maps.database.CDatabaseConnection;
import org.maps.database.CDatabaseConnectionConfig;
import org.maps.database.dao.UsersDAO;
import org.maps.database.datamodel.TBLUsers;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import commonlibs.commonclasses.CLanguage;
import commonlibs.commonclasses.ConstantsCommonClasses;
import commonlibs.extendedlogger.CExtendedLogger;
import commonlibs.utils.Utilities;
import commonlibs.utils.ZKUtilities;

public class CLoginController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 2607061613647188753L;

    protected CExtendedLogger controllerLogger = null;

    protected CLanguage controllerLanguage = null;
    
    @Wire Textbox textboxOperator;
    
    @Wire Textbox textboxPassword;
    
    @Wire Label labelMessage;
    
    
    @Override	
	public void doAfterCompose(Component comp) {
		try {
			super.doAfterCompose(comp);
			//Obtenemos el logger del objeto webApp y guardamoes una referencia en a variable de clase controllerLogger 
    		controllerLogger = (CExtendedLogger) Sessions.getCurrent().getWebApp().getAttribute(ConstantsCommonClasses._Webapp_Logger_App_Attribute_Key);

		} catch (Exception ex) {
			if ( controllerLogger != null )   
				controllerLogger.logException( "-1021", ex.getMessage(), ex );        
		}
	}

    @Listen ( "onChanging=#textboxOperator; onChanging=#textboxPassword")
	public void onChangetextbox (Event event) {
    
    //de esta manera sabemos quien dispara el evento	
      if ( event.getTarget().equals(textboxOperator)){
    	  System.out.println("Textbox operator");
      } else if ( event.getTarget().equals(textboxPassword)){
    	  System.out.println("Textbox password");
      }
    	labelMessage.setValue("");//si cambia el operator o password
    	
    }
    
    @Listen("onTimer=#timerKeepAliveSession")
    public void onTimer(Event event){
    	
    	//este evento se ejecutara cada 120 segundos
    	Clients.showNotification("renovación automatica de la session","info",null,"before_center",2000,true);

    }
    

    	
    @Listen ( "onClick=#buttonLogin; onOK=#windowLogin")
   	public void onClickbuttonLogin (Event event) {
		try {
			
		//Aquí vamos a conectarnos a la bd y vamos a verificar si los valores introducidos son válidos	

			final String strOperator = ZKUtilities.getTextBoxValue(textboxOperator, controllerLogger);
			final String strPassword = ZKUtilities.getTextBoxValue(textboxPassword, controllerLogger);
		
			if ( strOperator.isEmpty() == false && strPassword.isEmpty() == false) {
				
				CDatabaseConnection databaseConnection = new CDatabaseConnection();

	    		CDatabaseConnectionConfig databaseConnectionConfig = new CDatabaseConnectionConfig();

	    		//lugar donde esta el archivo de configuracion
	    		String strRunningPath = Sessions.getCurrent().getWebApp().getRealPath(SystemConstants._WEB_INF_Dir) + File.separator;
	    		
	    		String test = strRunningPath + SystemConstants._CONFIG_Dir + File.separator + SystemConstants._Database_Connection_Config_File_Name;

	    		if (databaseConnectionConfig.loadConfig(test, controllerLogger, controllerLanguage)) {

	    			if (databaseConnection.makeConnectionToDB(databaseConnectionConfig, controllerLogger, controllerLanguage)) {
	    				
	    				TBLUsers tblusers = UsersDAO.checkvalid(databaseConnection, strOperator, strPassword, controllerLogger, controllerLanguage);
	    			  if ( tblusers != null) {
	   
    				   	Session currentSession = Sessions.getCurrent();
    				   	
    				   	//mensaje de bienvenida
    				   	labelMessage.setSclass(""); //cambia el color rojo de labelmessage por negro 
	    				labelMessage.setValue("Bienvenido "+tblusers.getFirstName()+"!");

     				    //salvamos la conexion a la sesion actual del usuario, cada usuario/pestaña tiene su propia sesion
	      				currentSession.setAttribute(SystemConstants._DB_Connection_Session_Key, databaseConnection);
                        //controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Saved user conection for session for user [%s]", databaseConnection.toString()));
	      				
	      				//salvamos la entidad del operador en la sesion
	      				currentSession.setAttribute(SystemConstants._Operator_Credential_Session_Key, tblusers);
                        controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Saved user credential in session for user [%s]", tblusers.getFirstName()) );


                        //Obtenemos la fecha y la hora en el formato yyyy-MM-dd-HH-mm-ss
                        String strDateTime = Utilities.getDateInFormat( ConstantsCommonClasses._Global_Date_Time_Format_File_System_24, null );
                        
                        //Creamos la variable del logpath
                        String strLogPath = strRunningPath + SystemConstants._Logs_Dir + strOperator + File.separator + strDateTime + File.separator;
                        
                        //La guardamos en la sesión
                        currentSession.setAttribute(SystemConstants._Log_Path_Session_Key, strLogPath);
                        controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Saved user log path [%s] in session for user [%s]", strLogPath, strOperator ) );

                        //salvamos hora y fecha de inicio de sesion
	      				currentSession.setAttribute(SystemConstants._Login_Date_Time_Session_Key, strDateTime );
	      				controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Saved user login date time [%s] in session for user [%s]", strDateTime, strOperator ) );
	      				
	      				//creamos la lista de loggers de esta sesión
	      				List<String> loggedSessionLoggers = new LinkedList<String>();
	      				
	      				//Guardamos la lista vacia en la sesión
	      				currentSession.setAttribute(SystemConstants._Logged_Session_Loggers, loggedSessionLoggers);
	      					      				
	      				//Actualizamos en la bd el ultimo inicio de sesion
	      				UsersDAO.updateLastLogin(databaseConnection, tblusers.getId(), controllerLogger, controllerLanguage);
	      				
	      				//redireccionamos hacia el home.zul
	      				Executions.sendRedirect("/views/home/home.zul");
	      				
	   
	    				  
	    			  }
		    			else {
		    			
		    				labelMessage.setValue("Nombre y password inválidos");
		    				
		    				//Messagebox.show("Nombre y password inválidos");
		    				
		    			}

	    			}
	    			else {
	    				Messagebox.show("Conexión Fallida");
	    			}
			}else {
				Messagebox.show("Error al leer el archivo de configuración");
			}
	    		}
			
		} catch (Exception ex) {
			if ( controllerLogger != null )   
				controllerLogger.logException( "-1021", ex.getMessage(), ex );        
		}

		
	}
		
}

