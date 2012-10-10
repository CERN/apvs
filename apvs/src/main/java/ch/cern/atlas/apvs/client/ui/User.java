package ch.cern.atlas.apvs.client.ui;

import java.io.Serializable;

public class User implements Serializable {

	private static final long serialVersionUID = -1423459851493629177L;

	private int id;
	private String firstName;
	private String lastName;
	private String cernId;
	
	public User() {
		// Serializable
	}

	public User(int id, String fname, String lname, String cernId) {
		this.id = id;
		this.firstName = fname;
		this.lastName = lname;
		this.cernId = cernId;
	}
	
	public int getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getCernId() {
		return cernId;
	}
	
	public String getDisplayName() {
		return firstName+" "+lastName;
	}
}
