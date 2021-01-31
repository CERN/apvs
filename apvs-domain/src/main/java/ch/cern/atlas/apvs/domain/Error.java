package ch.cern.atlas.apvs.domain;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

//NOTE: implements IsSerializable in case serialization file cannot be found
public class Error implements Message, Serializable, IsSerializable {

	private static final long serialVersionUID = -7278595154796058932L;

	private volatile Device device;
	private final String type = "Error";
	private Integer errorNo;
	private String description;
	private String criticality;
	private Date time;

	protected Error() {
	}

	public Error(Device device, Integer errorNo, String description,
			String criticality, Date time) {
		this.device = device;
		this.errorNo = errorNo;
		this.description = description;
		this.criticality = criticality;
		this.time = time;
	}

	@Override
	public Device getDevice() {
		return device;
	}

	public Integer getErrorNo() {
		return errorNo;
	}

	public String getDescription() {
		return description;
	}

	public String getCriticality() {
		return criticality;
	}

	public Date getTime() {
		return time;
	}

	@Override
	public String getType() {
		return type;
	}
}
