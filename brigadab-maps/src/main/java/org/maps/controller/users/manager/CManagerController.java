package org.maps.controller.users.manager;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import commonlibs.commonclasses.CLanguage;
import commonlibs.commonclasses.ConstantsCommonClasses;
import commonlibs.extendedlogger.CExtendedConfigLogger;
import commonlibs.extendedlogger.CExtendedLogger;
import commonlibs.utils.Utilities;

import org.maps.constant.SystemConstants;
import org.maps.database.CDatabaseConnection;
import org.maps.database.dao.UsersDAO;
import org.maps.database.datamodel.TBLUsers;
import org.maps.utilities.SystemUtilities;

public class CManagerController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 7989626864714327680L;
	
	
	protected ListModelList<TBLUsers> dataModel = null;  //new ListModelList<TBLPerson>();
	
	protected ListModelList<TBLUsers> aux = new ListModelList<TBLUsers>();
	
	public void initcontrollerLoggerAndcontrollerLanguage( String strRunningPath, Session currentSession ){
		
		//Leemos la configuración del logger del archivo o de la sesión
		CExtendedConfigLogger extendedConfigLogger = SystemUtilities.initLoggerConfig( strRunningPath,currentSession );
		
		//Obtenemos las credenciales del operador las cuales debieron ser guardadas por el CLoginController.java
		TBLUsers operatorCredential = (TBLUsers) currentSession.getAttribute(SystemConstants._Operator_Credential_Session_Key);
		
		//Inicializamos los valores de las variables
		//Esto es un valor por defecto no debería quedar con el pero si lo hacer el algoritmo no falla
		String strOperator = SystemConstants._Operator_Unknown; 
		
        //Recuperamos información de fecha y hora del inicio de sesión Login
		String strLoginDateTime = (String) currentSession.getAttribute( SystemConstants._Login_Date_Time_Session_Key ); 
		
        //Recuperamos el path donde se guardarn los log ya que cambia según el nombre de l operador que inicie sesion
		String strLogPath = (String) currentSession.getAttribute( SystemConstants._Log_Path_Session_Key ); 
		
        if (operatorCredential != null){
        	strOperator = operatorCredential.getUserName();//Obtenemos el nombre del operador que hizo login

        }

        if (strLoginDateTime == null) {//En caso de ser null no ha fecha y hora de inicio de sesión colocarle una por defecto
        	strLoginDateTime=Utilities.getDateInFormat( ConstantsCommonClasses._Global_Date_Time_Format_File_System_24, null );
        }

        
        final String strLoggerName = SystemConstants._Users_Manager_Controller_Logger_Name;
        final String strLoggerFileName = SystemConstants._Users_Manager_Controller_File_Log;
        
        //Aqui creamos el logger para el operador que inicio sesión login en el sistema
        controllerLogger = CExtendedLogger.getLogger( strLoggerName + " " + strOperator + " " + strLoginDateTime );
        
        //Esto se ejecuta si es la primera vez que esta creando el logger recuerden lo que pasa 
        //Cuando el usuario hace recargar en el navegador todo el .zul se vuelve a crear de cero, 
        //pero el logger persiste de manera similar a como lo hacen las variables de session
        if (controllerLogger.getSetupSet() == false) {
        	
        	//Aquí vemos si es null esa varible del logpath intentamos poner una por defecto
        	if (strLogPath == null) 
        		strLogPath = strRunningPath+File.separator+SystemConstants._Logs_Dir;
        	
        	//Si hay una configuración leida de session o del archivo la aplicamos
        	if (extendedConfigLogger != null)
        		controllerLogger.setupLogger( strOperator + " " + strLoginDateTime, false, strLogPath, strLoggerFileName, extendedConfigLogger.getClassNameMethodName(), extendedConfigLogger.getExactMatch(), extendedConfigLogger.getLevel(), extendedConfigLogger.getLogIP(), extendedConfigLogger.getLogPort(), extendedConfigLogger.getHTTPLogURL(), extendedConfigLogger.getHTTPLogUser(), extendedConfigLogger.getHTTPLogPassword(), extendedConfigLogger.getProxyIP(), extendedConfigLogger.getProxyPort(), extendedConfigLogger.getProxyUser(), extendedConfigLogger.getProxyPassword() );
        	else  //Si no usamos la por defecto
        		controllerLogger.setupLogger( strOperator + " " + strLoginDateTime, false, strLogPath, strLoggerFileName, SystemConstants._Log_Class_Method, SystemConstants._Log_Exact_Match, SystemConstants._Log_Level, "", -1, "", "", "", "", -1, "", "" );
        	
        	//Inicializamos el lenguage para ser usado por el logger
        	controllerLanguage = CLanguage.getLanguage( controllerLogger, strRunningPath + SystemConstants._Langs_Dir + strLoggerName + "." + SystemConstants._Lang_Ext ); 
        	
        	//Protección para el multi hebrado, puede que dos usuarios accedan exactamente al mismo tiempo a la página web, este código en el servidor se ejecuta en dos hebras
        	synchronized( currentSession ) { //Aquí entra un asunto de hebras y acceso multiple de varias hebras a la misma variable
                
                //Guardamos en la sesión los logger que se van creando para luego ser destruidos.
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
	

	
	public class RendererUser implements ListitemRenderer<TBLUsers>{
	 
		
		public void render(Listitem listitem, TBLUsers users, int IntIndex) throws Exception {
    try{
    	 /* Listcell cell = new Listcell();  no mostrar id
        cell.setLabel(person.getId());
        listitem.appendChild(cell);*/

        Listcell   cell = new Listcell();
        cell.setLabel(users.getUserName());
        listitem.appendChild(cell);

        cell = new Listcell();
        cell.setLabel(users.getFirstName());
        listitem.appendChild(cell);

        cell = new Listcell();
        cell.setLabel(users.getLastName());
        listitem.appendChild(cell);

        cell = new Listcell();
        cell.setLabel(users.getRole() == 0 ? "Admin" : "User" );
        listitem.appendChild(cell);

       
        cell = new Listcell();
        cell.setLabel(users.getDescription());
        listitem.appendChild(cell);

	} catch (Exception ex) {
		ex.printStackTrace();
	}
	}
		
	}

	
	
	@Wire Listbox listboxusers;
	@Wire Button buttonModify;
    @Wire Button buttonAdd;
    // @Wire Button buttonConnectionToDB;
    @Wire Button buttonRefresh;
    @Wire Button buttonClose;
    @Wire Window windowsUsersManager; //la ventana del manager
    
    protected CDatabaseConnection databaseConnection=null;  

    protected CExtendedLogger controllerLogger = null;

    protected CLanguage controllerLanguage = null;
    
    @Override
	public void doAfterCompose(Component comp) {
		try {
			super.doAfterCompose(comp);

			final String strRunningPath = Sessions.getCurrent().getWebApp().getRealPath(SystemConstants._WEB_INF_Dir) + File.separator ;
			//Inicializamos el Logger y el Language
			initcontrollerLoggerAndcontrollerLanguage(strRunningPath, Sessions.getCurrent());			

			
			/*TBLPerson person01 = new TBLPerson("12225798","Edgar","Bastardo",1,LocalDate.parse("1974-10-09"),"Mi persona");
			TBLPerson person02 = new TBLPerson("13670604","Loly","Gómez",0,LocalDate.parse("1980-01-16"),"Mi proxima jefa");
			TBLPerson person03 = new TBLPerson("12674936","Al","Pérez",1,LocalDate.parse("1977-09-24"),"un pana");
			TBLPerson person04 = new TBLPerson("4288565","Leonor","González",0,LocalDate.parse("1956-11-06"),"mi madre");
			TBLPerson person05 = new TBLPerson("9476658","Tibisay","Cedeño",0,LocalDate.parse("1960-10-10"),"una pana"); 
			TBLPerson person06 = new TBLPerson("16547282","Junibeth","Salazar",0,LocalDate.parse("1984-06-06"),"Esposa"); 
			dataModel.add(person01);
			dataModel.add(person02);
			dataModel.add(person03);
			dataModel.add(person04);
			dataModel.add(person05);
			dataModel.add(person06);
			
			dataModel.setMultiple(true);//activas selecion multiple
			listboxperson.setModel(dataModel);
			listboxperson.setItemRenderer(new RendererPerson() );*/
			
		 	Session currentSession = Sessions.getCurrent();
		 	
			//Obtenemos el logger del objeto webApp y guardamoes una referencia en a variable de clase controllerLogger 

		 	//controllerLogger = (CExtendedLogger) Sessions.getCurrent().getWebApp().getAttribute(ConstantsCommonClasses._Webapp_Logger_App_Attribute_Key);

    		
		 	if (currentSession.getAttribute(SystemConstants._DB_Connection_Session_Key) instanceof CDatabaseConnection) {
		 		//recuperamos de la sesion la anterior conexion
		 		databaseConnection= (CDatabaseConnection) currentSession.getAttribute(SystemConstants._DB_Connection_Session_Key);
		 		//buttonConnectionToDB.setLabel("Disconnect");
		        Events.echoEvent( new Event( "onClick", buttonRefresh) ); //forzamos el refresh para visualizar los elementos de la lista    	
		 		
		 	}
			} catch (Exception e) {
				if ( controllerLogger != null )   
					controllerLogger.logException( "-1021", e.getMessage(), e );        
			}
		}

	// ------------------------ CREATE ---------------------
    	
  /*  @Listen("onAgregar=#buttonAdd")
    public void onAgregar(Event evento){
        TBLPerson perso = ( TBLPerson ) evento.getData();
        dataModel.add(perso);
        listboxperson.setModel( dataModel );
        listboxperson.setItemRenderer( new RendererPerson() );
        //TBLPersonDAO.insertData(DatabaseConnection, perso);  // *********************************lo pongo en comentarios porque ya agrega en CDialogController
        
    }

    @Listen ("onClick=#buttonConnectionToDB")
    public void onClickbuttonConnectionToDB (Event event){

    	Session currentSession = Sessions.getCurrent();

    	if (databaseConnection== null)   //( buttonConnectionToDB.getLabel().equalsIgnoreCase( "Connect" ))
    	{
    		databaseConnection = new CDatabaseConexion();

    		CDatabaseConnectionConfig databaseConnectionConfig = new CDatabaseConnectionConfig();

    		//lugar donde esta el archivo de configuracion
    		String strRunningPath = Sessions.getCurrent().getWebApp().getRealPath(SystemConstants._WEB_INF_Dir) + File.separator + SystemConstants._CONFIG_Dir + File.separator;

    		if (databaseConnectionConfig.loadConfig(strRunningPath + SystemConstants._Database_Connection_Config_File_Name, controllerLogger, controllerLanguage)) {

    			if (databaseConnection.makeConnectionToDB(databaseConnectionConfig, controllerLogger, controllerLanguage))
    			{
    				//Salvamos la configuracion en el objeto databaseConnection
    				//databaseConnection.setDBConnectionConfig(databaseConnectionConfig, webAppLogger, null);
    				
    				//salvamos la conexion a la sesion actual del usuario, cada usuario/pestaña tiene su propia sesion
    				currentSession.setAttribute(SystemConstants._DB_Connection_Session_Key, databaseConnection);
    				//buttonConnectionToDB.setLabel("Disconnect");
    				Messagebox.show("Conexión Exitosa");
    			}
    			else {
    				Messagebox.show("Conexión Fallida");
    			}
    		}
    		else {
    			Messagebox.show("Error al leer el archivo de configuración");
    		}

    	} else {
    		if (databaseConnection != null){
    			//Obtenemos el logger del objeto webApp 
        		CExtendedLogger webAppLogger = (CExtendedLogger) Sessions.getCurrent().getWebApp().getAttribute(ConstantsCommonClasses._Webapp_Logger_App_Attribute_Key);

        		if (databaseConnection.closeConnectionToDB(webAppLogger, null)){

    				databaseConnection = null;
    				Messagebox.show("Conexión Cerrada");
    				//buttonConnectionToDB.setLabel("Connect");		

    				//borramos la variable de sesion
    				//currentSession.setAttribute(_DATABASE_CONNECTION_KEY, null);
    				currentSession.removeAttribute(SystemConstants._DB_Connection_Session_Key);

    			}else{
    				Messagebox.show("falla al cerrar conexión");
    			}

    		}else {
    			Messagebox.show("No estas conectado");

    		}

    	}
        Events.echoEvent( new Event( "onClick", buttonRefresh) ); //forzamos el refresh para visualizar los elementos de la lista    	
   }
*/
    
    @Listen ("onClick=#buttonRefresh")
    public void onClickbuttonRefresh (Event event){
    
    	//aqui cargamos los datos de la DB
    	
		listboxusers.setModel((ListModelList<?>) null); //limpiamos el contenido de la lista 
    
		Session currentSession = Sessions.getCurrent();
		if (currentSession.getAttribute(SystemConstants._DB_Connection_Session_Key) instanceof CDatabaseConnection) {
			//Recuperamos la conexion a la DB de la sesión 
			databaseConnection= (CDatabaseConnection) currentSession.getAttribute(SystemConstants._DB_Connection_Session_Key);
			
			List<TBLUsers> listData = UsersDAO.searchData(databaseConnection, controllerLogger, controllerLanguage);
			
			//recreamos el modelo nuevamente
			dataModel = new ListModelList <TBLUsers>(listData);
			
			dataModel.setMultiple(true);//activas selecion multiple
			listboxusers.setModel(dataModel);
			listboxusers.setItemRenderer(new RendererUser() );  //importante la renderización de los datos para que los muestre

			//listboxperson.setModel(dataModel);
		}
    	
    }
    
	@Listen ("onClick=#buttonAdd")
	public void onClickbuttonAdd (Event event){
	
        Map<String, Object> arg = new HashMap<String, Object>();
        arg.put( "callerComponent", listboxusers ); //buttonAdd );
        
      Window win = (Window) Executions.createComponents("/views/users/editor/editor.zul", null, arg); //llama a Dialog
      win.doModal();
      
	}
	
	
	
	// ------------------------ MODIFY ---------------------	
	
	@Listen ("onClick=#buttonModify")
	public void onClickbuttonModify (Event event){

	    Set<TBLUsers> SelectedItems = dataModel.getSelection();
	      
		  if (SelectedItems != null && SelectedItems.size() > 0) {
			  
			  TBLUsers users = SelectedItems.iterator().next();
			  Map <String,Object> parametro = new HashMap<String,Object>();
			 // parametro.put("persontomodify", person);
			  parametro.put( "idPerson", users.getId() );
			  parametro.put( "callerComponent", listboxusers);
			  
              Window win = (Window) Executions.createComponents("/views/users/editor/editor.zul",null, parametro); //attach to page as root if parent is null
		      win.doModal();
		      
		  }
			  else {
				  Messagebox.show("No hay seleción");
		  }

	}
	
    //evento que edita el model en la persona y permite volver a renderizar el model 
    @Listen( "onCambiar=#listboxperson" )
    public void onCambiar( Event evento) {
    	
    	//forzamos refrescar la lista
    	
        Events.echoEvent( new Event( "onClick", buttonRefresh) );    	
    }    
    
    //evento que edita el model en la persona y permite volver a renderizar el model 
    @Listen( "onClick=#buttonClose" )
    public void onClickbuttonClose( Event evento) {
    
    	windowsUsersManager.detach();
    }
    
    
/*    @Listen( "onCambiar=#buttonModify" )
    public void onCambiar( Event evento) {
        
        TBLPerson persona = ( TBLPerson ) evento.getData();
        int i=0;
        //Messagebox.show( dataModel.getSize()+1+"numero" );
        while(i<=dataModel.getSize()){
            if(dataModel.getElementAt(i).getId() == persona.getId()){
                dataModel.getElementAt(i ).setId( persona.getId() );
                dataModel.getElementAt( i ).setFirstName( persona.getFirstName());
                dataModel.getElementAt(i).setLastName( persona.getLastName());
                dataModel.getElementAt( i ).setGender( persona.getGender() );
                dataModel.getElementAt( i ).setBirthDate( persona.getBirthDate());
                dataModel.getElementAt( i ).setComment( persona.getComment() );
            i=dataModel.getSize();
            }
            i=i+1;

        }
        listboxperson.setModel( dataModel );
        listboxperson.setItemRenderer( new RendererPerson() );
        
    }
*/
	
	// ------------------------ DELETE ---------------------
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Listen ("onClick=#buttonDelete")
    public void onClickbuttonDelete (Event event){
    	final Set<TBLUsers> SelectedItems = dataModel.getSelection();
    	if (SelectedItems != null && SelectedItems.size() > 0) {
    		String strBuffers =null;
    		for  (TBLUsers users : SelectedItems) {
    			if (strBuffers == null){
    				strBuffers = users.getId() + " " + users.getFirstName() + " " + users.getLastName() ;
    			}
    			else
    			{ strBuffers = strBuffers + "\n" + users.getId() + " " + users.getFirstName() + " " + users.getLastName() ;}
    		}
     		Messagebox.show("¿Seguro que quiere borrar " + Integer.toString(SelectedItems.size() ) + " registros?\n"+ strBuffers, "Eliminar", 
  			Messagebox.OK |Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() 
     		{public void onEvent(Event evt) throws InterruptedException {
     			
     		if (evt.getName().equals("onOK")) 
     		  {
    			while (SelectedItems.iterator().hasNext()) 
    			 {
    				TBLUsers person = SelectedItems.iterator().next();
    				//SelectedItems.iterator().remove();
    				dataModel.remove(person);
    				UsersDAO.deleteData( databaseConnection, person.getId(), controllerLogger, controllerLanguage);
    			        
                 } 			//Forzamos refrescar la lista 
    				Events.echoEvent( new Event( "onClick", buttonRefresh ) );  //Lanzamos el evento click de zk
    
                 
             }
             
         }});		    
        
     }
     else {
        
         Messagebox.show( "No hay seleccion" );
         
     }
     
 }
}