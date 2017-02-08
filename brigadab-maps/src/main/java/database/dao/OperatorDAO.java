package database.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import database.CDatabaseConnection;
import database.datamodel.TBLOperator;

import commonlibs.commonclasses.CLanguage;
import commonlibs.extendedlogger.CExtendedLogger;
import commonlibs.utils.BCrypt;


public class OperatorDAO {
	
	public static TBLOperator loadData (final CDatabaseConnection databaseConexion, final String strId, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		TBLOperator result = null;
	try {
			
			if (databaseConexion != null && databaseConexion.getDBConnection() != null) {
				Statement statement = databaseConexion.getDBConnection().createStatement();
				ResultSet resultSet = statement.executeQuery("Select * From tblusers Where Id='" + strId + "'");
				if (resultSet.next()){
					
					result = new TBLOperator();
					
					result.setId(resultSet.getString("ID"));
					result.setFirstName(resultSet.getString("FirstName"));
					result.setLastName(resultSet.getString("LastName"));
					result.setRole(resultSet.getString("NameRole"));
					result.setPassword(resultSet.getString("Password"));
					result.setDescription(resultSet.getString("Description"));
					
					// interface
					result.setDisabledAtDate (resultSet.getDate("DisabledAtDate").toLocalDate());
					result.setDisabledAtTime (resultSet.getTime("DisabledAtTime").toLocalTime());
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
	

	public static boolean deleteData (final CDatabaseConnection databaseConexion, final String strId, CExtendedLogger localLogger, CLanguage localLanguage) {
		boolean bResult = false;
		try{
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				Statement statement = databaseConexion.getDBConnection().createStatement();
				final String strSQL = "delete from tbloperator where ID = '"+strId+"'"; 
				statement.executeUpdate(strSQL);
				databaseConexion.getDBConnection().commit(); // importante hacer commit sino no guarda la información en la base de datos
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
	
	
	public static boolean updateLastLogin (final CDatabaseConnection databaseConexion, final String strId, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		boolean bResult = false;
		try{
			if (databaseConexion != null && databaseConexion.getDBConnection() != null)
			{
				Statement statement = databaseConexion.getDBConnection().createStatement();
				
				
				final String strSQL = "Update tbloperator set LastLoginAtDate='"+LocalDate.now().toString()+"', LastLoginAtTime='"+LocalTime.now().toString()+"' where ID='"+ strId+"'"; 
				statement.executeUpdate(strSQL);
				databaseConexion.getDBConnection().commit(); // importante hacer commit sino no guarda la información en la base de datos
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
	
	public static List<TBLOperator> searchData (final CDatabaseConnection databaseConexion, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		List<TBLOperator> result = new ArrayList<TBLOperator>();
		
		return result;
	}
	
	
	public static TBLOperator checkvalid (final CDatabaseConnection databaseConexion, final String strName, final String strPassword, CExtendedLogger localLogger, CLanguage localLanguage) {
		
	    TBLOperator result = null;
	try {
			
			if (databaseConexion != null && databaseConexion.getDBConnection() != null) {
				
				Statement statement = databaseConexion.getDBConnection().createStatement();
				
				//aqui se va a necesitar encriptar
				//final String strTest="Select * From tbloperator Where Name='" + strName + "' and Password='"+ strPassword +"' and DisabledBy Is Null and DisabledAtTime Is Null and DisabledAtDate Is Null";
				//ahora no se puede usar el password para buscar en la db, como el name no se repite lo buscamos por alli
				final String strTest="Select * From tbloperator Where Name='" + strName + "' and DisabledBy Is Null and DisabledAtTime Is Null and DisabledAtDate Is Null";

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
					result = new TBLOperator();
					
					
					result.setId(resultSet.getString("ID"));
					result.setFirstName(resultSet.getString("FirstName"));
					result.setFirstName(resultSet.getString("LastName"));
					result.setRole(resultSet.getString("Role"));
					result.setPassword(resultSet.getString("Password"));
					result.setDescription(resultSet.getString("setDescription"));
					
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
		
	
	

}
