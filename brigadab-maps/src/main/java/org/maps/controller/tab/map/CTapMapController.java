package org.maps.controller.tab.map;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


import org.maps.constant.SystemConstants;
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
import org.zkoss.zul.Textbox;

import commonlibs.commonclasses.CLanguage;
import commonlibs.commonclasses.ConstantsCommonClasses;
import commonlibs.extendedlogger.CExtendedConfigLogger;
import commonlibs.extendedlogger.CExtendedLogger;
import commonlibs.utils.Utilities;


@SuppressWarnings( "serial" )
public class CTapMapController extends SelectorComposer<Component> {
        
    protected CExtendedLogger controllerLogger = null;
    
    protected CLanguage controllerLanguage = null;
    
    protected List<Gmarker> listMarker = new ArrayList<Gmarker>();

    protected Gmarker marker;
    
    @Wire
    private Gmaps gmaps;
     
    @Wire
    private Button buttonCreateMarker, buttonDeleteMarker, buttonMarkerInfo, buttonChangeInfo;
     
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
            final String strRunningPath = Sessions.getCurrent().getWebApp().getRealPath( SystemConstants._WEB_INF_Dir ) + File.separator + SystemConstants._CONFIG_Dir + File.separator;
            
            //Inicializacmos el Logger y el Lenguaje
            initcontrollerLoggerAndcontrollerLanguage( strRunningPath, Sessions.getCurrent() );
            
            gmaps.setLat( 10.974586 );
            gmaps.setLng( -63.870997 );

            doubleboxLatitude.setValue(gmaps.getLat());
            doubleboxLongitude.setValue(gmaps.getLng());
            intboxZoom.setValue(gmaps.getZoom());
            
            gmaps = new Gmaps();
            Events.echoEvent( new Event( "onClick", buttonCreateMarker, marker) ); 
         
                       
        }
        catch ( Exception ex ) {
            
            if ( controllerLogger != null ) controllerLogger.logException( "-1021" , ex.getMessage(), ex );
         
        }
    }
    
    
    
    @Listen("onMapClick = #gmaps")
    public void onMapClick( MapMouseEvent event ) {

        doubleboxLatitude.setValue(event.getLatLng().getLatitude());
        doubleboxLongitude.setValue(event.getLatLng().getLongitude());
        
        Gmarker markerfocus = marker; 
        marker = event.getGmarker();
        
        
        if ( marker != null ) {
        	
        	markerfocus.setIconHeight( 3 );
            markerfocus.setIconWidth( 1 );
            
            marker.setIconHeight( 8 );
            marker.setIconWidth( 6 );
        
            buttonDeleteMarker.setDisabled( false );
            buttonCreateMarker.setDisabled( true );
            
            marker.setOpen( !marker.isOpen() && !marker.getContent().isEmpty() );
            
            if ( marker.isOpen() || marker.getContent().isEmpty() ) {            
            	
            	textboxChangeInfo.setDisabled( false );
            	buttonChangeInfo.setDisabled( false );
            	textboxChangeInfo.setValue( marker.getContent() ); 
            
            }
            
        }
        else {
            
            buttonCreateMarker.setDisabled( false );
            textboxChangeInfo.setDisabled( true);
            buttonChangeInfo.setDisabled( true );
            buttonDeleteMarker.setDisabled( true );
            textboxChangeInfo.setValue( null ); 
            
        }
        
    }
    
    @Listen("onChange = #latitude, #longitude" )
    public void onPositionChange() {
       
        gmaps.panTo(doubleboxLatitude.getValue(), doubleboxLongitude.getValue());
    
    }
     
    @Listen("onChange = #zoom" )
    public void onZoomChange() {
    
        gmaps.setZoom(intboxZoom.getValue());
           
    }
 
    
    @Listen("onClick = #buttonChangeInfo") 
    public void onClickButtonChangeInfo() {
        
    	marker.setContent( textboxChangeInfo.getValue() ); 
    	marker.setOpen( !marker.getContent().isEmpty() );
        
    }   
    
    @Listen("onClick = #buttonCreateMarker") 
    public void onClickButtonCreateMarker( Event event) {
        
        LatLng pointCoord = new LatLng(doubleboxLatitude.getValue(), doubleboxLongitude.getValue()); 
        
        final String strId = UUID.randomUUID().toString(); 
     
        Gmarker markerfocus = marker; 
        marker = new  Gmarker( "", pointCoord );
        
        if ( doubleboxLatitude.getValue().toString().substring(0, 6).contains("10.974") && doubleboxLongitude.getValue().toString().substring(0, 7).contains("-63.870") ) 
            marker.setContent("Elysium st operations center.");       	
        
        marker.setId( strId );
        marker.setOpen( !marker.getContent().isEmpty() );
        marker.setParent( gmaps );
        
        buttonCreateMarker.setDisabled( true );

        listMarker.add( marker );
        
        marker.setIconHeight( 5 );
        marker.setIconWidth( 3 );
        
        if (markerfocus != null ) {
        	
        	markerfocus.setIconHeight( 3 );
            markerfocus.setIconWidth( 1 );
        }
        
    }
    
    @Listen("onClick = #buttonDeleteMarker") 
    public void onClickButtonDeleteMarker( Event event ) {
        
      marker.setParent( null );
         
      buttonDeleteMarker.setDisabled( true );
        
    }
    
    @Listen("onClick = #buttonLoadPointer") 
    public void onClickButtonLoadPointer() {
        
        
      for ( Gmarker marker : listMarker ) { 
        
    	  	marker.setOpen( false );
            marker.setParent( gmaps );
       
       }         
        
    }
    
    @Listen("onClick = #buttonClearMAp") 
    public void onClickButtonClearMAp() {
        
        gmaps.getChildren().clear();
        
    }
     
    @Listen("onMapMove = #gmaps") 
    public void onMapMove() {
        
        doubleboxLatitude.setValue(gmaps.getLat());
        doubleboxLongitude.setValue(gmaps.getLng());
    
    }   
 
    @Listen("onMapZoom = #gmaps") 
    public void onMapZoom() {
    
        intboxZoom.setValue(gmaps.getZoom());
    
    }   

}
