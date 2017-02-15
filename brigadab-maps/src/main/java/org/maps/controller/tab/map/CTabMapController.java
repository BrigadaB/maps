package org.maps.controller.tab.map;


import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.maps.constant.SystemConstants;
import org.maps.database.CDatabaseConnection;
import org.maps.database.dao.PositionsHystoryDAO;
import org.maps.database.datamodel.TBLUsers;
import org.maps.utilities.SystemUtilities;
import org.zkoss.gmaps.Gmaps;
import org.zkoss.gmaps.Gmarker;
import org.zkoss.gmaps.LatLng;
import org.zkoss.gmaps.event.MapMouseEvent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import java.time.format.DateTimeFormatter;

import commonlibs.commonclasses.CLanguage;
import commonlibs.commonclasses.ConstantsCommonClasses;
import commonlibs.extendedlogger.CExtendedConfigLogger;
import commonlibs.extendedlogger.CExtendedLogger;
import commonlibs.utils.Utilities;



public class CTabMapController extends SelectorComposer<Component> {

	private static final long serialVersionUID = -4054777931730185834L;

	protected CDatabaseConnection databaseConnection = null; 
    
    protected CExtendedLogger controllerLogger = null;
    
    protected CLanguage controllerLanguage = null;
    
    protected List<Gmarker> historyListMarker = new ArrayList<Gmarker>(), newListMarker = new ArrayList<Gmarker>();

    protected Gmarker marker, lastMarker;
    
    @Wire
    private Gmaps gmaps;
     
    @Wire
    private Button buttonCreateMarker, buttonDeleteMarker, buttonLoadLastMarker, buttonChangeInfo, buttonLoadHistoryMarker, buttonSaveMap;
     
    @Wire
    private Doublebox doubleboxLatitude, doubleboxLongitude;
     
    @Wire
    private Intbox intboxZoom;
    
    @Wire
    private Textbox textboxChangeInfo;
   
    public void initcontrollerLoggerAndcontrollerLanguage  ( String strRunningPath, Session currentSession ) {
        
        //Leemos la configuración del logger del archivo o de la sesión
        CExtendedConfigLogger extendedConfigLogger = SystemUtilities.initLoggerConfig( strRunningPath, currentSession );

        //Obtenemos las credenciales del operador las cuales debieron ser guardadas por el CLoginController.java
        TBLUsers operatorCredential = ( TBLUsers ) currentSession.getAttribute( SystemConstants._Operator_Credential_Session_Key );
 
        //Inicializamos los valores de las variables
        String strOperator = SystemConstants._Operator_Unknown; //Esto es un valor por defecto no debería quedar con el pero si lo hacer el algoritmo no falla
        String strLoginDateTime = (String) currentSession.getAttribute( SystemConstants._Login_Date_Time_Session_Key ); //Recuperamos información de fecha y hora del inicio de sesión Login
        String strLogPath = (String) currentSession.getAttribute( SystemConstants._Log_Path_Session_Key ); //Recuperamos el path donde se guardarn los log ya que cambia según el nombre de l operador que inicie sesion  

        if ( operatorCredential != null )
            strOperator = operatorCredential.getUserName();  //Obtenemos el nombre del operador que hizo login

        if ( strLoginDateTime == null ) //En caso de ser null no ha fecha y hora de inicio de sesión colocarle una por defecto
            strLoginDateTime = Utilities.getDateInFormat( ConstantsCommonClasses._Global_Date_Time_Format_File_System_24, null );

        final String strLoggerName = SystemConstants._Tab_Map_Controller_Logger_Name;
        final String strLoggerFileName = SystemConstants._Tab_Map_Controller_File_Log;
        
        //Aqui creamos el logger para el operador que inicio sesión login en el sistem
        controllerLogger = CExtendedLogger.getLogger( strLoggerName + " " + strOperator + " " + strLoginDateTime );

        //strRunningPath = Sessions.getCurrent().getWebApp().getRealPath( SystemConstanst._WEB_INF_Dir ) + File.separator;

        //Esto se ejecuta si es la primera vez que esta creando el logger recuerden lo que pasa 
        //Cuando el usuario hace recargar en el navegador todo el .zul se vuelve a crear de cero, 
        //pero el logger persiste de manera similar a como lo hacen la viriables de session
        if ( controllerLogger.getSetupSet() == false ) {

            //Aquí vemos si es null esa varible del logpath intentamos poner una por defecto
            if ( strLogPath == null )
                strLogPath = strRunningPath + File.separator + SystemConstants._Logs_Dir;

            //Si hay una configucación leida de session o del archivo la aplicamos
            if ( extendedConfigLogger != null )
                controllerLogger.setupLogger( strOperator + " " + strLoginDateTime, false, strLogPath, strLoggerFileName, extendedConfigLogger.getClassNameMethodName(), extendedConfigLogger.getExactMatch(), extendedConfigLogger.getLevel(), extendedConfigLogger.getLogIP(), extendedConfigLogger.getLogPort(), extendedConfigLogger.getHTTPLogURL(), extendedConfigLogger.getHTTPLogUser(), extendedConfigLogger.getHTTPLogPassword(), extendedConfigLogger.getProxyIP(), extendedConfigLogger.getProxyPort(), extendedConfigLogger.getProxyUser(), extendedConfigLogger.getProxyPassword() );
            else    //Si no usamos la por defecto
                controllerLogger.setupLogger( strOperator + " " + strLoginDateTime, false, strLogPath, strLoggerFileName, SystemConstants._Log_Class_Method, SystemConstants._Log_Exact_Match, SystemConstants._Log_Level, "", -1, "", "", "", "", -1, "", "" );

            //Inicializamos el lenguage para ser usado por el logger
            controllerLanguage = CLanguage.getLanguage( controllerLogger, strRunningPath + SystemConstants._Langs_Dir + strLoggerName + "." + SystemConstants._Lang_Ext ); 

            //Protección para el multi hebrado, puede que dos usuarios accedan exactamente al mismo tiempo a la página web, este código en el servidor se ejecuta en dos hebras
            synchronized( currentSession ) { //Aquí entra un asunto de habras y acceso multiple de varias hebras a la misma variable
            
                //Guardamos en la sesisón los logger que se van creando para luego ser destruidos.
                @SuppressWarnings("unchecked")
                LinkedList<String> loggedSessionLoggers = (LinkedList<String>) currentSession.getAttribute( SystemConstants._Logged_Session_Loggers );

                if ( loggedSessionLoggers != null ) {

                    //sessionLoggers = new LinkedList<String>();

                    //El mismo problema de la otra variable
                    synchronized( loggedSessionLoggers ) {

                        //Lo agregamos a la lista
                        loggedSessionLoggers.add( strLoggerName + " " + strOperator + " " + strLoginDateTime );

                    }

                    //Lo retornamos la sesión de este operador
                    currentSession.setAttribute( SystemConstants._Logged_Session_Loggers, loggedSessionLoggers );

                }
            
            }
            
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
            
            Session currentSession = Sessions.getCurrent();
            
            if ( currentSession.getAttribute( SystemConstants._DB_Connection_Session_Key ) instanceof CDatabaseConnection ) {
                
                //recuperamos la sesion
                databaseConnection = ( CDatabaseConnection ) currentSession.getAttribute( SystemConstants._DB_Connection_Session_Key );
                //Buscamos el Usuario
                TBLUsers tblUser = ( TBLUsers ) Sessions.getCurrent().getAttribute(SystemConstants._Operator_Credential_Session_Key);

                
    			if (tblUser != null) {
    				// Gaurdamos en una Lista los marcadores de la Base de Datos 
    	            historyListMarker = PositionsHystoryDAO.loadMarkers( databaseConnection, tblUser.getId(), controllerLogger, controllerLanguage );
    	            // asignamos el ultumo marcador
    				lastMarker = historyListMarker.get( 0 );
    			}
    			
    			// forzamos a mostrar los marcadores historicos y el untimo marcador
    			Events.echoEvent( new Event( "onClick", buttonLoadHistoryMarker ) );
    			Events.echoEvent( new Event( "onClick", buttonLoadLastMarker ) );
    			
    			// ubucamos el mapara segun el ultimo marcado
                gmaps.setLat( lastMarker.getLat() );
    			gmaps.setLng( lastMarker.getLng() );
            
            }
            
           	// llenamos los campos
            doubleboxLatitude.setValue( gmaps.getLat() );
            doubleboxLongitude.setValue( gmaps.getLng() );
            intboxZoom.setValue( gmaps.getZoom() );
                       
        }
        catch ( Exception ex ) {
            
            if ( controllerLogger != null ) controllerLogger.logException( "-1021" , ex.getMessage(), ex );
         
        }
    }
    
    
    
    @Listen("onMapClick = #gmaps")
    public void onMapClick( MapMouseEvent event ) {

		try {

			// lenamos los campos segun donde se alla dado click en el mapa
			doubleboxLatitude.setValue( event.getLatLng().getLatitude() );
			doubleboxLongitude.setValue( event.getLatLng().getLongitude() );

			// se guarda el marcador anterior si existe
			Gmarker markerfocus = marker;
			// se guarda el nuevo marcador si existe
			marker = event.getGmarker();

			if ( marker != null ) {

				
				if ( markerfocus != null ) {

					markerfocus.setIconHeight(3);
					markerfocus.setIconWidth(1);
				}

				marker.setIconHeight(30);
				marker.setIconWidth(40);

				buttonDeleteMarker.setDisabled( false );
				buttonCreateMarker.setDisabled( true );

				// si no esta abierto y no esta vacio se abre el info
				marker.setOpen( !marker.isOpen() && !marker.getContent().isEmpty() );

				if ( marker.isOpen() || marker.getContent().isEmpty() ) {

					textboxChangeInfo.setDisabled( false );
					buttonChangeInfo.setDisabled( false );
					textboxChangeInfo.setValue( marker.getContent() );

				}

			} else {

				buttonCreateMarker.setDisabled( false );
				textboxChangeInfo.setDisabled( true );
				buttonChangeInfo.setDisabled( true );
				buttonDeleteMarker.setDisabled( true );
				textboxChangeInfo.setValue( null );

			}
		} 
		catch (Exception ex) {

			if ( controllerLogger != null )
				controllerLogger.logException( "-1021", ex.getMessage(), ex );

		}
        
    }
    
    @Listen( "onChange = #latitude, #longitude" )
    public void onPositionChange() {
    
    	try {
    	
    		gmaps.panTo( doubleboxLatitude.getValue(), doubleboxLongitude.getValue() );
        
    	}
    	catch ( Exception ex ) {
        
    		if ( controllerLogger != null ) controllerLogger.logException( "-1021" , ex.getMessage(), ex );
     
    	}
    
    }
     
    @Listen( "onChange = #zoom" )
    public void onZoomChange() {
    	
    	try {
        
    		gmaps.setZoom( intboxZoom.getValue() );
    	
    	}
        catch ( Exception ex ) {
            
            if ( controllerLogger != null ) controllerLogger.logException( "-1021" , ex.getMessage(), ex );
         
        }   
    	
    }
 
    
    @Listen( "onClick = #buttonChangeInfo" ) 
    public void onClickButtonChangeInfo() {
        
    	try {
    		

			if ( controllerLogger != null )
				controllerLogger.logMessage( "1", CLanguage.translateIf(controllerLanguage, "Button change info clicked"));
			// se asigna lel texto del texbo al info del marcador seleccionado y si es vacio lo cierra
    		marker.setContent( textboxChangeInfo.getValue() ); 
    		marker.setOpen( !marker.getContent().isEmpty() );
    	}
        catch ( Exception ex ) {
            
            if ( controllerLogger != null ) controllerLogger.logException( "-1021" , ex.getMessage(), ex );
         
        }
    
    }   
    
    @Listen( "onClick = #buttonCreateMarker" )  
    public void onClickButtonCreateMarker( Event event ) {

		try {

			if ( controllerLogger != null )
				controllerLogger.logMessage( "1", CLanguage.translateIf(controllerLanguage, "Button create marker clicked"));

			// se tomam los valores de coordenadas y de elaboracion
			LatLng pointCoord = new LatLng( doubleboxLatitude.getValue(), doubleboxLongitude.getValue() );
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("H:mm:ss" );
			final String strId =  LocalDate.now().toString() + ", " + LocalTime.now().format(dtf).toString();

			Gmarker markerfocus = marker;
			
			// se crea e nuevo marcador se inicializa con los valores correspondiente y se asigna al mapa
			marker = new Gmarker( strId, pointCoord );
			marker.setId( strId );
			marker.setOpen( !marker.getContent().isEmpty() );
			marker.setParent( gmaps );
			
			buttonSaveMap.setDisabled( false );
			buttonCreateMarker.setDisabled( true );

			// se guarda en la lista de nuevos marcades
			newListMarker.add( marker );

			marker.setIconHeight(5);
			marker.setIconWidth(3);

			if (markerfocus != null) {

				markerfocus.setIconHeight(3);
				markerfocus.setIconWidth(1);
			}
			
		} catch ( Exception ex ) {

			if ( controllerLogger != null )
				controllerLogger.logException( "-1021", ex.getMessage(), ex );

		}
    
    }
    
    @Listen("onClick = #buttonDeleteMarker") 
    public void onClickButtonDeleteMarker( Event event ) {
     
		try {

			if ( controllerLogger != null )
				controllerLogger.logMessage( "1", CLanguage.translateIf(controllerLanguage, "Button delete marker clicked" ) );
			
			// elimina solo del mapa el marcador seleccionado
			marker.setParent( null );
			
			buttonDeleteMarker.setDisabled( true );
			buttonSaveMap.setDisabled( false );

		} catch ( Exception ex ) {

			if ( controllerLogger != null )
				controllerLogger.logException( "-1021", ex.getMessage(), ex );

		}
        
    }
    
    @Listen( "onClick = #buttonLoadLastMarker" ) 
    public void onClickButtonLoadLastMarker() {
    	
    	try {
			

			if ( controllerLogger != null )
				controllerLogger.logMessage( "1", CLanguage.translateIf(controllerLanguage, "Button load last marker clicked" ) );
			
			if ( lastMarker != null ){

				// muestra el ultimo marcador de la BD, si ya esta abre el info
				if ( !gmaps.getChildren().contains( lastMarker ) ) {
				
					gmaps.setLat( lastMarker.getLat() );
					gmaps.setLng( lastMarker.getLng() );
					lastMarker.setParent( gmaps );
					lastMarker.setOpen( true );

				}
				else lastMarker.setOpen( true );
			}
		} 
		catch ( Exception ex ) {

			if ( controllerLogger != null )
				controllerLogger.logException( "-1021", ex.getMessage(), ex );

		}
		 
    }
    
    @Listen( "onClick = #buttonLoadHistoryMarker" ) 
    public void onClickButtonLoadHistoryMarker() {

    	
    	try {	
    		

			if ( controllerLogger != null )
				controllerLogger.logMessage( "1", CLanguage.translateIf(controllerLanguage, "Button load history marker clicked"));
    		
			//asigna al mapa los marcadores de la BD
    		if ( historyListMarker != null )   
    			for ( Gmarker marker : historyListMarker ) { 
    	 
    				gmaps.setLat( marker.getLat() );
    				gmaps.setLng( marker.getLng() );
    				marker.setOpen( false );
    				marker.setParent( gmaps );
         
    			}
    		// asina al mapa los marcadores nuevos
    		if ( newListMarker != null )   
    			for ( Gmarker marker : newListMarker ) { 
    	 
    				gmaps.setLat( marker.getLat() );
    				gmaps.setLng( marker.getLng() );
    				marker.setOpen( false );
    				marker.setParent( gmaps );
         
    			}
    	}
    	catch ( Exception ex ) {

    		if ( controllerLogger != null )
    			controllerLogger.logException( "-1021", ex.getMessage(), ex );

    	}
        
    }
    
    @Listen( "onClick = #buttonClearMap" ) 
    public void onClickButtonClearMap() {
        
    	try {
    		

			if ( controllerLogger != null )
				controllerLogger.logMessage( "1", CLanguage.translateIf(controllerLanguage, "Button clear map clicked"));
			
    		// limpia por completo al mapa de marcadores
    		gmaps.getChildren().clear();
    		
    		buttonSaveMap.setDisabled( true );
    		
    	}
   		catch (Exception ex) {

   			if ( controllerLogger != null )
    			controllerLogger.logException( "-1021", ex.getMessage(), ex );

    	}
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen( "onClick = #buttonSaveMap" ) 
    public void onClickButtonSaveMap() {
        
		if ( controllerLogger != null )
			controllerLogger.logMessage("1", CLanguage.translateIf(controllerLanguage, "Button save map clicked"));
	
		Messagebox.show( "You are sure do you want save map?", "Save Map", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
             public void onEvent(Event evt) throws InterruptedException {
                   
                 if ( evt.getName().equals( "onOK" ) ) {
                       
                	 try {
                 		
                 		List<Gmarker> deleteMarker = new ArrayList<Gmarker>(), insertMarker = new ArrayList<Gmarker>();
                 		
             			TBLUsers tblUser = ( TBLUsers ) Sessions.getCurrent().getAttribute(SystemConstants._Operator_Credential_Session_Key);

              			if ( tblUser != null) {
              				
              				//se obitene los marcadores que se van a insertar	
              				if ( newListMarker != null )   
              	    			for ( Gmarker marker : newListMarker )  
              	    				if ( gmaps.getChildren().contains( marker ) ) 
              	    					insertMarker.add( marker); 
              	    		// se obtienen los marcadores que se van a borara	
              				if ( historyListMarker != null )   
              	    			for ( Gmarker marker : historyListMarker )
              	    	 			if ( !gmaps.getChildren().contains( marker ) ) 
              	    	 				deleteMarker.add( marker);
              	 
              				// se inserta y se borra de la base de datos
              				PositionsHystoryDAO.insertMarker( databaseConnection, tblUser.getId(), insertMarker, controllerLogger, controllerLanguage );
              	            PositionsHystoryDAO.deletaMarker( databaseConnection, tblUser.getId(), deleteMarker, controllerLogger, controllerLanguage );
              	            
              	            //se inicializa las variables
              	            newListMarker.clear();
              	            historyListMarker = PositionsHystoryDAO.loadMarkers( databaseConnection, tblUser.getId(), controllerLogger, controllerLanguage );
              	            lastMarker = historyListMarker.get( 0 );
              	            
              			}
             			
                 	}
                	catch ( Exception ex ) {

                			if ( controllerLogger != null )
                 			controllerLogger.logException( "-1021", ex.getMessage(), ex );

                 	}
                	 
                	 
                 }
                 else {
                     
                     if ( controllerLogger != null ) 
                         controllerLogger.logMessage( "1" , CLanguage.translateIf( controllerLanguage, "Not Save, confirm cancel" ) );
                   
                 }
                   
              }
        });
    	
    }
     
    @Listen( "onMapMove = #gmaps" ) 
    public void onMapMove() {
    
    	try {
    		// se actualiza los box de posicion
    		doubleboxLatitude.setValue( gmaps.getLat() );
    		doubleboxLongitude.setValue( gmaps.getLng() );
    	}
    	catch ( Exception ex ) {
			
    			if ( controllerLogger != null )
    				controllerLogger.logException( "-1021", ex.getMessage(), ex );

    	}
    
    }   
 
    @Listen( "onMapZoom = #gmaps" ) 
    public void onMapZoom() {
    
    	try { 
    		// se acualiza el box del xoom
    		intboxZoom.setValue( gmaps.getZoom() );
    
    	}
   		catch ( Exception ex ) {

   			if ( controllerLogger != null )
    			controllerLogger.logException( "-1021", ex.getMessage(), ex );

    	}
    	
    }   

}