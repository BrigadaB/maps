package org.maps.database.datamodel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class TBLPositionsHistory extends CAuditableDataModel implements Serializable {

	private static final long serialVersionUID = -9176828393151574276L;

	
	protected String strId;
	protected String strLatitude;
	protected String strLongitude;
	
	protected LocalDate createdAtDate;
	protected LocalTime createdAtTime;

	public String getId() {
		
		return strId;
		
	}
	
	public void setId(String strId) {
		
		this.strId = strId;
		
	}

	public String getLatitude() {
		
		return strLatitude;
		
	}

	public void setLatitude(String strLatitude) {
		
		this.strLatitude = strLatitude;
	
	}

	public String getLongitude() {
	
		return strLongitude;
	
	}

	public void setLongitude(String strLongitude) {
	
		this.strLongitude = strLongitude;
	
	}

	public LocalDate getCreatedAtDate() {
	
		return createdAtDate;
	
	}

	public void setCreatedAtDate(LocalDate createdAtDate) {
	
		this.createdAtDate = createdAtDate;
	
	}

	public LocalTime getCreatedAtTime() {
	
		return createdAtTime;
	
	}

	public void setCreatedAtTime(LocalTime createdAtTime) {
	
		this.createdAtTime = createdAtTime;
	
	}

}