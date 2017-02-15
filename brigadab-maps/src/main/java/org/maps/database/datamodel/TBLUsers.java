package org.maps.database.datamodel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import commonlibs.utils.BCrypt;

public class TBLUsers extends CAuditableDataModel implements Serializable {

	private static final long serialVersionUID = -460145854236713435L;

	protected String strId;
	protected String strUserName;
	protected String strFirstName;
	protected String strLastName;
	protected String strPassword;
	protected Byte   byteRole;
	protected Byte   byteSingleSession;
	protected String strDescription;

	protected LocalDate disabledAtDate;
	protected LocalTime disabledAtTime;

	protected LocalDate LastLoggedDate;
	protected LocalTime LastLoggedTime;
	
	
	public TBLUsers ( String strId, String strUserName, String strFirstName, String strLastName, String strPassword, byte byteRole, byte byteSingleSession, String strDescription, String strDisiabledBy, LocalDate disiabledAtDate, LocalTime disiabledAtTime, LocalDate lastLoginAtDate, LocalTime lastLoginAtTime) {
		
	       
        this.strId= strId;
        this.strUserName = strUserName;
        this.strFirstName = strFirstName;
        this.strLastName = strLastName;
        this.strPassword = strPassword;
        this.byteRole = byteRole;
        this.byteSingleSession = byteSingleSession;
        this.strDescription = strDescription;  
        
        this.disabledAtDate = disiabledAtDate;
        this.disabledAtTime = disiabledAtTime;   
        this.LastLoggedDate = lastLoginAtDate;
        this.LastLoggedTime = lastLoginAtTime;
        
    }
     
    public TBLUsers() {
    
    }

	public String getId() {
		
		return strId;
		
	}
	
	public void setId(String strId) {
		
		this.strId = strId;
		
	}
	
	public String getUserName() {
		return strUserName;
		
	}

	public void setUserName(String strUserName) {
		this.strUserName = strUserName;
		
	}

	public String getFirstName() {
		
		return strFirstName;
		
	}
	
	public void setFirstName(String strFirstName) {
		
		this.strFirstName = strFirstName;
		
	}
	
	public String getLastName() {
		
		return strLastName;
	
	}
	
	public void setLastName(String strLastName) {
		
		this.strLastName = strLastName;
		
	}
	
	public Byte getRole() {
	
		return byteRole;
	
	}

	public void setRole(Byte byteRole) {
	
		this.byteRole = byteRole;
	
	}

	public Byte getSingleSession() {
	
		return byteSingleSession;
	
	}

	public void setSingleSession(Byte byteSingleSession) {
	
		this.byteSingleSession = byteSingleSession;
	
	}

	public String getPassword() {
		
		return strPassword;
		
	}
	
	public void setPassword(String strPassword) {
	
		//verificamos si el strPassword ya viene encryptado
		if  ( strPassword.startsWith("$2y$10$") == false) {
		 String strPasswordKey = BCrypt.gensalt(10); //establecemos el parametro de inicio para encriptar
		 strPassword = BCrypt.hashpw(strPassword, strPasswordKey); //aqui se realiza la encriptación
		 strPassword = strPassword.replaceAll("$2a$10$", "$2y$10$"); //
		}
		this.strPassword = strPassword;
		
	}
	
	public String getDescription() {
		
		return strDescription;
		
	}
	
	public void setDescription(String strDescription) {
		
		this.strDescription = strDescription;
		
	}
	
	public LocalDate getDisabledAtDate() {
		
		return disabledAtDate;
		
	}
	
	public void setDisabledAtDate(LocalDate disabledAtDate) {
		
		this.disabledAtDate = disabledAtDate;
		
	}
	
	public LocalTime getDisabledAtTime() {
		
		return disabledAtTime;
		
	}
	
	public void setDisabledAtTime(LocalTime disabledAtTime) {
		
		this.disabledAtTime = disabledAtTime;
		
	}
	
	public LocalDate getLastLoginAtDate() {
		
		return LastLoggedDate;
		
	}
	
	public void setLastLoggedDate(LocalDate lastLoggedDate) {
		
		LastLoggedDate = lastLoggedDate;
		
	}
	
	public LocalTime getLastLoggedTime() {
		
		return LastLoggedTime;
		
	}
	
	public void setLastLoggedTime(LocalTime lastLoggedTime) {
		
		LastLoggedTime = lastLoggedTime;
		
	}
	



}
