package database.datamodel;

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
	protected String strRole;
	protected String strSingleSession;
	protected String strDescription;

	protected LocalDate disabledAtDate;
	protected LocalTime disabledAtTime;

	protected LocalDate LastLoggedDate;
	protected LocalTime LastLoggedTime;

	public String getId() {
		
		return strId;
		
	}
	
	public void setId(String strId) {
		
		this.strId = strId;
		
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
	
	public String getStrSingleSession() {
		return strSingleSession;
	}

	public void setStrSingleSession(String strSingleSession) {
		this.strSingleSession = strSingleSession;
	}
	
	public String getRole() {
		
		return strRole;
	}
	
	public void setRole(String strRole) {
		
		this.strRole = strRole;
		
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
