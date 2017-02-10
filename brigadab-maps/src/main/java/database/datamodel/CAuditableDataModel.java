package database.datamodel;

import java.time.LocalDate;
import java.time.LocalTime;

public class CAuditableDataModel implements IAuditableDataModel {


	private static final long serialVersionUID = 5846118704431670299L;
	
	
	protected String strCreatedBy = null;
	protected LocalDate createdAtDate = null;
	protected LocalTime createdAtTime = null;

	protected String strUpdatedBy = null;
	protected LocalDate updatedAtDate = null;
	protected LocalTime updatedAtTime = null;
	
	public LocalTime getCreatedAtTime() {

		return createdAtTime;
	}

	public LocalTime getUpdatedAtTime() {

		return updatedAtTime;
	}

	public LocalDate getCreatedAtDate() {

		return createdAtDate;
	}

	public void setCreatedAtDate(LocalDate createdAtDate) {

		this.createdAtDate = createdAtDate;
		
	}

	public void setCreatedAtTime(LocalTime createdAtTime) {

		this.createdAtTime = createdAtTime;
		
	}

	public LocalDate getUpdatedAtDate() {

		return updatedAtDate;
	}

	public void setUpdatedAtDate(LocalDate updatedAtDate) {

		this.updatedAtDate = updatedAtDate;
		
	}

	public void setUpdatedAtTime(LocalTime updatedAtTime) {
		
		this.updatedAtTime = updatedAtTime;
		
	}

}
