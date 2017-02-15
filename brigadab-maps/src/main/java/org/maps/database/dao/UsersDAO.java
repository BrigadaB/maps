package org.maps.database.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.maps.database.CDatabaseConnection;
import org.maps.database.datamodel.TBLUsers;
import org.zkoss.zul.Messagebox;

import commonlibs.commonclasses.CLanguage;
import commonlibs.extendedlogger.CExtendedLogger;
import commonlibs.utils.BCrypt;


public class UsersDAO {
	
	public static TBLUsers loadData (final CDatabaseConnection databaseConexion, final String strId, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		TBLUsers result = null;
	try {
			
			if (databaseConexion != null && databaseConexion.getDBConnection() != null) {
				Statement statement = databaseConexion.getDBConnection().createStatement();
				ResultSet resultSet = statement.executeQuery("Select * From tblusers Where Id='" + strId + "'");
				if (resultSet.next()){
					
					result = new TBLUsers();
					
					result.setId(resultSet.getString("ID"));
					result.setPassword(resultSet.getString("Password"));
					result.setUserName(resultSet.getString("UserName"));
					result.setFirstName(resultSet.getString("FirstName"));
					result.setLastName(resultSet.getString("LastName"));
					result.setRole(resultSet.getByte("Role"));
					result.setSingleSession(resultSet.getByte("SingleSession"));
					result.setDescription(resultSet.getString("Description"));
					
					// interface
					result.setDisabledAtDate (resultSet.getDate("DisabledAtDate") != null ? resultSet.getDate("DisabledAtDate").toLocalDate() : null);
					result.setDisabledAtTime (resultSet.getTime("DisabledAtTime") != null ? resultSet.getTime("DisabledAtTime").toLocalTime() : null);
					result.setLastLoggedDate (resultSet.getDate("LastLoggedDate").toLocalDate() != null ? resultSet.getDate("LastLoggedDate").toLocalDate() : null);
					result.setLastLoggedTime (resultSet.getTime("LastLoggedTime").toLocalTime() != null ? resultSet.getTime("LastLoggedTime").toLocalTime() : null);

					result.setCreatedAtDate (resultSet.getDate("CreatedAtDate").toLocalDate());
					result.setCreatedAtTime (resultSet.getTime("CreatedAtTime").toLocalTime());
					result.setUpdatedAtDate (resultSet.getDate("UpdatedAtDate").toLocalDate() != null ? resultSet.getDate("UpdatedAtDate").toLocalDate() : null);
					result.setUpdatedAtTime (resultSet.getTime("UpdatedAtTime").toLocalTime() != null ? resultSet.getTime("UpdatedAtTime").toLocalTime() : null);
					
				}
				// una vez termina hay que liberar recursos
				statement.close();
				resultSet.close();
			
				}	
			}
		catch (Exception ex) {
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				try {
					// i hay error hacer rollback a las operaciones anteriores
					databaseConexion.getDBConnection().rollback(); 
				} catch (Exception e) {
					
					if ( localLogger != null )   
						localLogger.logException( "-1021", e.getMessage(), e );        
				}
			}						
			if ( localLogger != null )   
				localLogger.logException( "-1022", ex.getMessage(), ex );        
		}
			
		
		
		return result;
	}
	
	public static boolean insertData (final CDatabaseConnection databaseConnection, final TBLUsers tbluser, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		boolean bResult = false;
		try{
			if (databaseConnection != null && databaseConnection.getDBConnection() != null)
			{
				Statement statement = databaseConnection.getDBConnection().createStatement();
				UUID uuidId = UUID.randomUUID();
				final String strSQL = "insert into tblusers (Id, UserName, FirstName, LastName, Role, Password, Description, CreatedAtDate, CreatedAtTime, UpdatedAtDate, UpdatedAtTime, DisabledAtDate, DisabledAtTime, LastLoggedDate, LastLoggedTime) values ('"+uuidId+"', '"+tbluser.getUserName()+"', '"+tbluser.getFirstName()+"', '"+tbluser.getLastName()+"', "+tbluser.getRole()+", '"+tbluser.getPassword()+"', '"+tbluser.getDescription()+ "', '"+LocalDate.now().toString()+"', '"+LocalTime.now().toString()+"',null,null,null,null,null,null)";  
				statement.executeUpdate(strSQL);
				databaseConnection.getDBConnection().commit(); // importante hacer commit sino no guarda la informaci�n en la base de datos
				statement.close(); //liberar recursos
				bResult = true;
				
		}
			}
		
		catch (Exception ex) {
			if (databaseConnection != null && databaseConnection.getDBConnection() != null)
			{
				try {
					// i hay error hacer rollback a las operaciones anteriores
					databaseConnection.getDBConnection().rollback(); 
				} catch (Exception e) {
					
					if ( localLogger != null )   
						localLogger.logException( "-1021", e.getMessage(), e );        
				}
			}			
			if ( localLogger != null )   
				localLogger.logException( "-1022", ex.getMessage(), ex );        
		} 
		return bResult;	}
	
	
	public static boolean deleteData (final CDatabaseConnection databaseConexion, final String strId, CExtendedLogger localLogger, CLanguage localLanguage) {
		boolean bResult = false;
		try{
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				Statement statement = databaseConexion.getDBConnection().createStatement();
				final String strSQL = "delete from tblusers where ID = '"+strId+"'"; 
				statement.executeUpdate(strSQL);
				databaseConexion.getDBConnection().commit(); // importante hacer commit sino no guarda la informaci�n en la base de datos
				statement.close(); //liberar recursos
				bResult = true;
		}
		}
		catch (Exception ex) {
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				try {
					// i hay error hacer rollback a las operaciones anteriores
					databaseConexion.getDBConnection().rollback(); 
				} catch (Exception e) {
					
					if ( localLogger != null )   
						localLogger.logException( "-1021", e.getMessage(), e );        
				}
			}						
			if ( localLogger != null )   
				localLogger.logException( "-1022", ex.getMessage(), ex );        
		} 
				
		return bResult;
	}
	
	public static boolean updateData (final CDatabaseConnection databaseConexion, final TBLUsers tblusers, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		boolean bResult = false;
		try{
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				Statement statement = databaseConexion.getDBConnection().createStatement();
				
				final String strDisabledAtDate = tblusers.getDisabledAtDate() != null ? "'"+LocalDate.now().toString() +"'":"null";
				final String strDisabledAtTime = tblusers.getDisabledAtTime() != null ? "'"+LocalTime.now().toString() +"'":"null";
				final String strSQL;
				if (strDisabledAtDate==("null")){
					strSQL = "Update tblusers set UserName = '"+tblusers.getUserName()+"', Password = '"+tblusers.getPassword()+"',  FirstName = '"+tblusers.getFirstName()+"', LastName = '"+tblusers.getLastName()+"', Role = '"+tblusers.getRole()+"', SingleSession = '"+tblusers.getSingleSession()+"', Description = '"+tblusers.getDescription()+"', UpdatedAtDate = '"+LocalDate.now().toString()+"', UpdatedAtTime ='"+LocalTime.now().toString()+"' where ID='"+ tblusers.getId()+"'";}				
				else
				{   strSQL = "Update tblusers set UserName = '"+tblusers.getUserName()+"', Password = '"+tblusers.getPassword()+"',  FirstName = '"+tblusers.getFirstName()+"', LastName = '"+tblusers.getLastName()+"', Role = '"+tblusers.getRole()+"', SingleSession = '"+tblusers.getSingleSession()+"', Description = '"+tblusers.getDescription()+"', UpdatedAtDate = '"+LocalDate.now().toString()+"', UpdatedAtTime ='"+LocalTime.now().toString()+"',  DisabledAtDate ='"+strDisabledAtDate+"', DisabledAtTime ='"+strDisabledAtTime+"' where ID='"+ tblusers.getId()+"'";}	
				statement.executeUpdate(strSQL);
				databaseConexion.getDBConnection().commit(); // importante hacer commit sino no guarda la informaci�n en la base de datos
				statement.close(); //liberar recursos
				bResult = true;
				
		}
			}
		
		catch (Exception ex) {
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				try {
					// i hay error hacer rollback a las operaciones anteriores
					databaseConexion.getDBConnection().rollback(); 
				} catch (Exception e) {
					
					if ( localLogger != null )   
						localLogger.logException( "-1021", e.getMessage(), e);        
				}
			}			
			if ( localLogger != null )   
				localLogger.logException( "-1022", ex.getMessage(), ex );        
		} 
		return bResult;
	
	}	
	
	public static boolean updateLastLogin (final CDatabaseConnection databaseConexion, final String strId, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		boolean bResult = false;
		try{
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				Statement statement = databaseConexion.getDBConnection().createStatement();
				
				
				final String strSQL = "Update tblusers set LastLoggedDate='"+LocalDate.now().toString()+"', LastLoggedTime='"+LocalTime.now().toString()+"' where ID='"+ strId+"'"; 
				statement.executeUpdate(strSQL);
				databaseConexion.getDBConnection().commit(); // importante hacer commit sino no guarda la informaci�n en la base de datos
				statement.close(); //liberar recursos
				bResult = true;
				
		}
			}
		
		catch (Exception ex) {
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				try {
					// i hay error hacer rollback a las operaciones anteriores
					databaseConexion.getDBConnection().rollback(); 
				} catch (Exception e) {
					
					if ( localLogger != null )   
						localLogger.logException( "-1021", e.getMessage(), e);        
				}
			}			
			if ( localLogger != null )   
				localLogger.logException( "-1022", ex.getMessage(), ex );        
		} 
		return bResult;
	
	}
	
	
public static List<TBLUsers> searchData (final CDatabaseConnection databaseConexion, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		List<TBLUsers> result = new ArrayList<TBLUsers>();
		
		try {
			
			if (databaseConexion != null && databaseConexion.getDBConnection() != null) {
				Statement statement = databaseConexion.getDBConnection().createStatement();
				ResultSet resultSet = statement.executeQuery("Select * From tblusers");
				while (resultSet.next()){
					
					TBLUsers tblUsers = new TBLUsers();
					
					
					tblUsers.setId(resultSet.getString("ID"));
					tblUsers.setUserName(resultSet.getString("UserName"));
					tblUsers.setFirstName(resultSet.getString("FirstName"));
					tblUsers.setLastName(resultSet.getString("LastName"));
					tblUsers.setRole(resultSet.getByte("Role"));
					tblUsers.setSingleSession(resultSet.getByte("SingleSession"));
					tblUsers.setDescription(resultSet.getString("Description"));
					
					// interface
					tblUsers.setCreatedAtDate (resultSet.getDate("CreatedAtDate").toLocalDate());
					tblUsers.setCreatedAtTime (resultSet.getTime("CreatedAtTime").toLocalTime());
					tblUsers.setUpdatedAtDate (resultSet.getDate("UpdatedAtDate") != null ? resultSet.getDate("UpdatedAtDate").toLocalDate() : null);
					tblUsers.setUpdatedAtTime (resultSet.getTime("UpdatedAtTime") != null ? resultSet.getTime("UpdatedAtTime").toLocalTime() : null);
					
					result.add(tblUsers); // agregar a la lista resultados
				}
				// una vez termina hay que liberar recursos
				statement.close();
				resultSet.close();
			
				}	
			}
		catch (Exception ex) {
			if ( localLogger != null )   
				localLogger.logException( "-1021", ex.getMessage(), ex );        
		}
				
		return result;
	}	
	
	
	
	public static TBLUsers checkvalid (final CDatabaseConnection databaseConexion, final String strName, final String strPassword, CExtendedLogger localLogger, CLanguage localLanguage) {
		
	    TBLUsers result = null;
	try {
			
			if (databaseConexion != null && databaseConexion.getDBConnection() != null) {
				
				Statement statement = databaseConexion.getDBConnection().createStatement();
				
				//aqui se va a necesitar encriptar
				//final String strTest="Select * From tbloperator Where Name='" + strName + "' and Password='"+ strPassword +"' and DisabledBy Is Null and DisabledAtTime Is Null and DisabledAtDate Is Null";
				//ahora no se puede usar el password para buscar en la db, como el name no se repite lo buscamos por alli
				final String strTest="Select * From tblusers Where UserName='" + strName + "' and DisabledAtTime Is Null and DisabledAtDate Is Null";

				ResultSet resultSet = statement.executeQuery(strTest);
				if (resultSet.next()){
					
					//Obtenemos el password escriptado del operador de la db
					String strDBPassword = resultSet.getString("Password");
					
					//lo guardamos
					String strDBPasswordKey = strDBPassword; 
					
					//esxtraemos los 30 primeros caracteres
					strDBPasswordKey = strDBPasswordKey.substring(0,29);
					
					//PHP     ->    Java(BCrypt)
					//$2y$10$ ->    $2a$10$       
					
					//cambiamos el $2y$10$ a $2a$10$, un bug de la libreria 
					strDBPasswordKey = strDBPasswordKey.replace("$2y$10$", "$2a$10$");
					
					//Luego usamos el password key para encriptar el password enviado por el operador
					// en la pantalla de login que viene sin encriptar
					String strPasswordHashed = BCrypt.hashpw(strPassword, strDBPasswordKey);
					
					
					//Java(BCrypt)->    PHP     
					//devolvemos el $2a$10$ a $2y$10$
					strPasswordHashed = strPasswordHashed.replace("$2a$10$", "$2y$10$");
					
					if (strPasswordHashed.equals(strDBPassword)) {
					result = new TBLUsers();
					
					
					result.setId(resultSet.getString("ID"));
					result.setUserName(resultSet.getString("UserName"));
					result.setUserName(resultSet.getString("UserName"));
					result.setFirstName(resultSet.getString("FirstName"));
					result.setFirstName(resultSet.getString("LastName"));
					result.setRole( resultSet.getByte("Role"));
					result.setSingleSession(resultSet.getByte("SingleSession"));
					result.setDescription(resultSet.getString("Description"));
					
					// interface
					result.setDisabledAtDate (resultSet.getDate("DisabledAtDate") != null ?  resultSet.getDate("DisabledAtDate").toLocalDate(): null); //puede ser null de la db
					result.setDisabledAtTime (resultSet.getTime("DisabledAtTime") != null ?  resultSet.getTime("DisabledAtTime").toLocalTime(): null);//puede ser null de la db
					result.setLastLoggedDate (resultSet.getDate("LastLoggedDate") != null ? resultSet.getDate("LastLoggedDate").toLocalDate() : null);//puede ser null de la db
					result.setLastLoggedTime (resultSet.getTime("LastLoggedTime") != null ? resultSet.getTime("LastLoggedTime").toLocalTime() : null);//puede ser null de la db

					result.setCreatedAtDate (resultSet.getDate("CreatedAtDate").toLocalDate());
					result.setCreatedAtTime (resultSet.getTime("CreatedAtTime").toLocalTime());
					result.setUpdatedAtDate (resultSet.getDate("UpdatedAtDate") != null ? resultSet.getDate("UpdatedAtDate").toLocalDate() : null);
					result.setUpdatedAtTime (resultSet.getTime("UpdatedAtTime") != null ? resultSet.getTime("UpdatedAtTime").toLocalTime() : null);
					
				}
					}
				// una vez termina hay que liberar recursos
				statement.close();
				resultSet.close();
			
				}	
			}
		catch (Exception ex) {
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				try {
					//Si hay error hacer rollback a las operaciones anteriores
					databaseConexion.getDBConnection().rollback(); 
				} catch (Exception e) {
					
					if ( localLogger != null )   
						localLogger.logException( "-1021", e.getMessage(), e );        
				}
			}						
			if ( localLogger != null )   
				localLogger.logException( "-1022", ex.getMessage(), ex );        
		}
				
		return result;
		
	}
		
	

public static TBLUsers changePassword(final CDatabaseConnection databaseConexion, final TBLUsers tblusers, String strNewPassword, String strOldPassword, CExtendedLogger localLogger, CLanguage localLanguage) {
		
	TBLUsers result = null;
	try {
			
			if (databaseConexion != null && databaseConexion.getDBConnection() != null) {
				
				Statement statement = databaseConexion.getDBConnection().createStatement();
				
				//aqui se va a necesitar encriptar
				//final String strTest="Select * From tbloperator Where Name='" + strName + "' and Password='"+ strPassword +"' and DisabledBy Is Null and DisabledAtTime Is Null and DisabledAtDate Is Null";
				//ahora no se puede usar el password para buscar en la db, como el name no se repite lo buscamos por alli
				final String strTest="Select * From tbloperator Where Name='" + tblusers.getUserName() + "' and DisabledBy Is Null and DisabledAtTime Is Null and DisabledAtDate Is Null";

				ResultSet resultSet = statement.executeQuery(strTest);
				if (resultSet.next()){
					
					//Obtenemos el password escriptado del operador de la db
					String strDBPassword = resultSet.getString("Password");
					
					//lo guardamos
					String strDBPasswordKey = strDBPassword; 
					
					//esxtraemos los 30 primeros caracteres
					strDBPasswordKey = strDBPasswordKey.substring(0,29);
					
					//PHP     ->    Java(BCrypt)
					//$2y$10$ ->    $2a$10$       
					
					//cambiamos el $2y$10$ a $2a$10$, un bug de la libreria 
					strDBPasswordKey = strDBPasswordKey.replace("$2y$10$", "$2a$10$");
					
					//Luego usamos el password key para encriptar el password enviado por el operador
					// en la pantalla de login que viene sin encriptar
					String strPasswordHashed = BCrypt.hashpw(strOldPassword, strDBPasswordKey);
					
					
					//Java(BCrypt)->    PHP     
					//devolvemos el $2a$10$ a $2y$10$
					strPasswordHashed = strPasswordHashed.replace("$2a$10$", "$2y$10$");
					
					if (strPasswordHashed.equals(strDBPassword)) {
						
						String strPasswordKey = BCrypt.gensalt(10); //establecemos el parametro de inicio para encriptar
						strNewPassword= BCrypt.hashpw(strNewPassword, strPasswordKey); //aqui se realiza la encriptaci�n
						strNewPassword = strNewPassword.replace("$2a$10$", "$2y$10$"); //
						
						tblusers.setPassword(strNewPassword);

						updateData(databaseConexion, tblusers, localLogger, localLanguage);
						Messagebox.show("Password Cambiado exitosamente");
                    }else
				{
					 Messagebox.show("Password Incorrecto");
				}
					}
				// una vez termina hay que liberar recursos
				statement.close();
				resultSet.close();
			
				}	
			}
		catch (Exception ex) {
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				try {
					//Si hay error hacer rollback a las operaciones anteriores
					databaseConexion.getDBConnection().rollback(); 
				} catch (Exception e) {
					
					if ( localLogger != null )   
						localLogger.logException( "-1021", e.getMessage(), e );        
				}
			}						
			if ( localLogger != null )   
				localLogger.logException( "-1022", ex.getMessage(), ex );        
		}
				
		return result;
		
	}
			

}
