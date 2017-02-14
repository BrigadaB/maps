package org.maps.database.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.maps.database.CDatabaseConnection;
import org.zkoss.gmaps.Gmarker;
import org.zkoss.gmaps.LatLng;

import commonlibs.commonclasses.CLanguage;
import commonlibs.extendedlogger.CExtendedLogger;

public class PositionsHystoryDAO {
	

	public static List<Gmarker> loadMarkers(final CDatabaseConnection databaseConexion, final String strUserId, CExtendedLogger localLogger, CLanguage localLanguage) {
		
		List<Gmarker> result = new ArrayList<Gmarker>();
		Gmarker marker = null;
		
		try {
			
			if (databaseConexion != null && databaseConexion.getDBConnection() != null) {
				
				Statement statement = databaseConexion.getDBConnection().createStatement();
				ResultSet resultSet = statement.executeQuery("Select * From tblpositionshistory Where UserId='" + strUserId + "' order by CreatedAtDate desc, CreatedAtTime desc" );
				
				while ( resultSet.next() ) {
						
					final LatLng pointCoord = new LatLng( new Double( resultSet.getString( "Latitude" ) ), new Double( resultSet.getString( "Longitude" ) ) ); 
					final String strId = resultSet.getDate( "CreatedAtDate" ).toString()  + ", " + resultSet.getTime( "CreatedAtTime" ).toString();  
					
					
			        marker = new  Gmarker( strId, pointCoord );
					marker.setId( strId );
			        marker.setOpen( false );
			        
			        result.add( marker );
			        					
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
	
    final public static boolean insertMarker ( final CDatabaseConnection dataBaseConnection,  String strUserId, List<Gmarker> listMarkers, CExtendedLogger localLogger, CLanguage localLanguage ) {
        
        boolean bresult = false;
        Iterator<Gmarker> iterator = listMarkers.iterator();
        
        try {
        	
        	Statement statement = dataBaseConnection.getDBConnection().createStatement();
        	
            while ( iterator.hasNext() ) {
        	
              final String SQLstr = "Insert Into tblpositionshistory(UserId,Latitude,Longitude,CreatedAtDate,CreatedAtTime)"
                    + " Values('" + strUserId + "','" 
            		+ new Double( iterator.next().getLat() ).toString() + "','" 
                    + new Double( iterator.next().getLat() ).toString()  + "','" 
            		+ iterator.next().getContent().substring( 0, 10 ) + "','" 
            		+ iterator.next().getContent().substring( 0, 10 ) + "')";
            		
               statement.executeUpdate(SQLstr);
            
            }
            
            dataBaseConnection.getDBConnection().commit();
            
            statement.close();
            
            bresult = true;
        }
        catch ( Exception ex ){
            
            if ( dataBaseConnection != null && dataBaseConnection.getDBConnection() != null)
              try {
                
                  dataBaseConnection.getDBConnection().rollback(); // en caso de error vuelve atras
                
              }
              catch ( Exception ex1 ){
              
                  if ( localLogger != null )  localLogger.logException( "-1021" , ex1.getMessage(), ex1 );
              }
            
            if ( localLogger != null )  localLogger.logException( "-1022" , ex.getMessage(), ex );
            
        }
        
        return bresult;
        
    }

}
