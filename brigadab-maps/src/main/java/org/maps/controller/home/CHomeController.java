package org.maps.controller.home;


import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.maps.constant.SystemConstants;
import org.maps.database.datamodel.TBLUsers;
import org.maps.utilities.SystemUtilities;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Window;

import commonlibs.commonclasses.CLanguage;
import commonlibs.commonclasses.ConstantsCommonClasses;
import commonlibs.extendedlogger.CExtendedConfigLogger;
import commonlibs.extendedlogger.CExtendedLogger;
import commonlibs.utils.Utilities;
import commonlibs.utils.ZKUtilities;


public class CHomeController extends SelectorComposer<Component> {
       
    private static final long serialVersionUID = 1040032932048818844L;
    
    protected CExtendedLogger controllerLogger = null;
    
    protected CLanguage controllerLanguage = null;
  
    //Para que la variable haga bien el binding se necestan los dos id
    @Wire( "#includeNorthContent #labelHeader" )
    Label labelHeader;
    
    @Wire
    Tabbox tabboxMainContent;
    
    @Wire
    Button buttonLogout;
   
    public void initcontrollerLoggerAndcontrollerLanguage  ( String strRunningPath, Session currentSession ) {
        
        //Leemos la configuraci�n del logger del archivo o de la sesi�n
        CExtendedConfigLogger extendedConfigLogger = SystemUtilities.initLoggerConfig( strRunningPath, currentSession );

        //Obtenemos las credenciales del operador las cuales debieron ser guardadas por el CLoginController.java
        TBLUsers operatorCredential = (TBLUsers) currentSession.getAttribute( SystemConstants._Operator_Credential_Session_Key );
 
        //Inicializamos los valores de las variables
        String strOperator = SystemConstants._Operator_Unknown; //Esto es un valor por defecto no deber�a quedar con el pero si lo hacer el algoritmo no falla
        String strLoginDateTime = (String) currentSession.getAttribute( SystemConstants._Login_Date_Time_Session_Key ); //Recuperamos informaci�n de fecha y hora del inicio de sesi�n Login
        String strLogPath = (String) currentSession.getAttribute( SystemConstants._Log_Path_Session_Key ); //Recuperamos el path donde se guardarn los log ya que cambia seg�n el nombre de l operador que inicie sesion  

        if ( operatorCredential != null )
            strOperator = operatorCredential.getFirstName();  //Obtenemos el nombre del operador que hizo login

        if ( strLoginDateTime == null ) //En caso de ser null no ha fecha y hora de inicio de sesi�n colocarle una por defecto
            strLoginDateTime = Utilities.getDateInFormat( ConstantsCommonClasses._Global_Date_Time_Format_File_System_24, null );

        final String strLoggerName = SystemConstants._Home_Controller_Logger_Name;
        final String strLoggerFileName = SystemConstants._Home_Controller_File_Log;
        
        //Aqui creamos el logger para el operador que inicio sesi�n login en el sistem
        controllerLogger = CExtendedLogger.getLogger( strLoggerName + " " + strOperator + " " + strLoginDateTime );

        //strRunningPath = Sessions.getCurrent().getWebApp().getRealPath( SystemConstanst._WEB_INF_Dir ) + File.separator;

        //Esto se ejecuta si es la primera vez que esta creando el logger recuerden lo que pasa 
        //Cuando el usuario hace recargar en el navegador todo el .zul se vuelve a crear de cero, 
        //pero el logger persiste de manera similar a como lo hacen la viriables de session
        if ( controllerLogger.getSetupSet() == false ) {

            //Aqu� vemos si es null esa varible del logpath intentamos poner una por defecto
            if ( strLogPath == null )
                strLogPath = strRunningPath + File.separator + SystemConstants._Logs_Dir;

            //Si hay una configucaci�n leida de session o del archivo la aplicamos
            if ( extendedConfigLogger != null )
                controllerLogger.setupLogger( strOperator + " " + strLoginDateTime, false, strLogPath, strLoggerFileName, extendedConfigLogger.getClassNameMethodName(), extendedConfigLogger.getExactMatch(), extendedConfigLogger.getLevel(), extendedConfigLogger.getLogIP(), extendedConfigLogger.getLogPort(), extendedConfigLogger.getHTTPLogURL(), extendedConfigLogger.getHTTPLogUser(), extendedConfigLogger.getHTTPLogPassword(), extendedConfigLogger.getProxyIP(), extendedConfigLogger.getProxyPort(), extendedConfigLogger.getProxyUser(), extendedConfigLogger.getProxyPassword() );
            else    //Si no usamos la por defecto
                controllerLogger.setupLogger( strOperator + " " + strLoginDateTime, false, strLogPath, strLoggerFileName, SystemConstants._Log_Class_Method, SystemConstants._Log_Exact_Match, SystemConstants._Log_Level, "", -1, "", "", "", "", -1, "", "" );

            //Inicializamos el lenguage para ser usado por el logger
            controllerLanguage = CLanguage.getLanguage( controllerLogger, strRunningPath + SystemConstants._Langs_Dir + strLoggerName + "." + SystemConstants._Lang_Ext ); 

            //Protecci�n para el multi hebrado, puede que dos usuarios accedan exactamente al mismo tiempo a la p�gina web, este c�digo en el servidor se ejecuta en dos hebras
            synchronized( currentSession ) { //Aqu� entra un asunto de habras y acceso multiple de varias hebras a la misma variable
            
                //Guardamos en la sesis�n los logger que se van creando para luego ser destruidos.
                @SuppressWarnings("unchecked")
                LinkedList<String> loggedSessionLoggers = (LinkedList<String>) currentSession.getAttribute( SystemConstants._Logged_Session_Loggers );

                if ( loggedSessionLoggers != null ) {

                    //sessionLoggers = new LinkedList<String>();

                    //El mismo problema de la otra variable
                    synchronized( loggedSessionLoggers ) {

                        //Lo agregamos a la lista
                        loggedSessionLoggers.add( strLoggerName + " " + strOperator + " " + strLoginDateTime );

                    }

                    //Lo retornamos la sesi�n de este operador
                    currentSession.setAttribute( SystemConstants._Logged_Session_Loggers, loggedSessionLoggers );

                }
            
            }
            
        }
    
    }

    
    public void initView() {
        
        try {
    	
			TBLUsers tblUsers = (TBLUsers) Sessions.getCurrent().getAttribute(SystemConstants._Operator_Credential_Session_Key);

			if (tblUsers != null) {

				if (labelHeader != null) {

					labelHeader.setValue((tblUsers.getRole() == 0 ? "Admin" : "User"));

				}
			}
			// aqui iniciamos los tab de manera dinamica

			// creaos el componente a partir del Zul un arreglo con los dos
			// componentes raiz
			Component[] components;
			Tab tab;

			
			// home se le muestra a todo el mundo
			components = Executions.getCurrent().createComponents("/views/tabs/home/tabhome.zul", null);
			// buscamos el componente de tipo tab esta rutina es un simple ciclo
			// de busqueda
			tab = (Tab) ZKUtilities.getComponent(components, "Tab");
			if (tab != null) {
				// asignamos tab encontrado de modo dinamico al tabbox
				tabboxMainContent.getTabs().appendChild(tab);
				
				  // buscamos el componente de tipo tabpanel Tabpanel tabPanel
				  Tabpanel tabPanel = (Tabpanel) ZKUtilities.getComponent( components, "Tabpanel");
				  
				  if ( tabPanel != null ) {
				  
				    // asignamos el tabpanel encontrado de modo dinamico al
				    tabboxMainContent.getTabpanels().appendChild( tabPanel);
				    
				  }
				
			}
			//if ( tblUsers.getRole() == 0 ) {
	            
	            // creaos el componente a partir del Zul un arreglo con los dos componentes raiz
	            components =  Executions.getCurrent().createComponents( "/views/tabs/admin/tabadmin.zul", null );
	            
	            // buscamos el componente de tipo tab esta rutina es un simple siclo de busqueda 
	            tab = (Tab) ZKUtilities.getComponent( components, "Tab" );
	            
	            if ( tab != null ) {
	                
	                // asignamos tab encontrado de modo dinamico al tabbox
	                tabboxMainContent.getTabs().appendChild( tab );
	                
	                // buscamos el componente de tipo tabpanel 
	                Tabpanel tabPanel = (Tabpanel) ZKUtilities.getComponent( components, "Tabpanel" );
	                
	                if ( tabPanel != null ) {
	                    
	                    // asignamos el tabpanel encontrado de modo dinamico al tabbox
	                    tabboxMainContent.getTabpanels().appendChild( tabPanel );
	            
	                }
	            
	            }
	       // }
	            
			// creaos el componente a partir del Zul un arreglo con los dos
			// componentes raiz
			components = Executions.getCurrent().createComponents("/views/tabs/map/tabmap.zul", null);

			// buscamos el componente de tipo tab esta rutina es un simple ciclo
			// de busqueda
			tab = (Tab) ZKUtilities.getComponent(components, "Tab");

			if (tab != null) {

				// asignamos tab encontrado de modo dinamico al tabbox
				tabboxMainContent.getTabs().appendChild(tab);

				// buscamos el componente de tipo tabpanel
				Tabpanel tabPanel = (Tabpanel) ZKUtilities.getComponent(components, "Tabpanel");

				if (tabPanel != null) {

					// asignamos el tabpanel encontrado de modo dinamico al tabbox
					tabboxMainContent.getTabpanels().appendChild(tabPanel);

				}

			}
        
    	}
    	catch ( Exception ex ) {
        
    		if ( controllerLogger != null ) controllerLogger.logException( "-1021" , ex.getMessage(), ex );
     
    	}
        
    }
    
    @Override
    public void doAfterCompose( Component comp ) {
        
        try {
            
            super.doAfterCompose( comp );
         
         // obtenemos la direccion del archivo de configuracion de los logger
            final String strRunningPath = Sessions.getCurrent().getWebApp().getRealPath( SystemConstants._WEB_INF_Dir ) + File.separator;
            
            //Inicializacmos el Logger y el Lenguaje
            initcontrollerLoggerAndcontrollerLanguage( strRunningPath, Sessions.getCurrent() );
            
            initView();
            
        }
        catch ( Exception ex ) {
            
            if ( controllerLogger != null ) controllerLogger.logException( "-1021" , ex.getMessage(), ex );
         
        }
    }

	@Listen ("onClick= #includeNorthContent #buttonChangePassword")
	public void onClickbuttonChangePassword (Event event) {
		
	        Map<String, Object> arg = new HashMap<String, Object>();
	      
	        
	      Window win = (Window) Executions.createComponents("/views/login/changePassword.zul", null, arg);
	      win.doModal();
	
		if (controllerLogger != null)
			controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Button change password clicked" ) );
		
	}
    
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Listen( "onClick= #includeNorthContent #buttonLogout")
    public void onClickbuttonLogout ( Event event ){
       
        if ( controllerLogger != null ) 
            controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Button logout clicked" ) );
            
        Messagebox.show( "You are sure do you want logout from System ?", "Logout", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
           public void onEvent(Event evt) throws InterruptedException {
                
              if ( evt.getName().equals( "onOK" ) ) {
                    
                  if ( controllerLogger != null ) 
                        controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Logout confirm accepted" ) );
                    
                  Sessions.getCurrent().invalidate();
            
                  Executions.sendRedirect( "/index.zul" ); //Lo enviamos al index.zul
                    
              }
              else {
                  
                  if ( controllerLogger != null ) 
                      controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Logout confirm cancel" ) );
                
              }
                
           }
         });
           
    
    }
  

    @Listen( "onTimer=#includeNorthContent #timerKeepAliveSession")
    public void onTimer( Event event ){
        
        // este evvento se ejecuta cada lapso de tiempo que le indiquemos en milisegundos
        
        // muetra una notificacion
        Clients.showNotification( "Automatic renewal of session successful", "info", null, "before_center", 120000, true);
        
    }
    
}