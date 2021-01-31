package ch.cern.atlas.apvs.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;

import com.google.gwt.user.client.rpc.IsSerializable;

//NOTE: implements IsSerializable in case serialization file cannot be found
@Entity
@Table( name = "TBL_GENERAL_CONFIGURATION" )
public class GeneralConfiguration implements Message, Serializable,
		IsSerializable, Comparable<GeneralConfiguration> {

	private static final long serialVersionUID = 4796032680266987232L;

	private volatile Device device;
	private volatile Long id;
	private String dosimeterId;
	private String bssid;
	private Date time;

	private final String type = "GeneralConfiguration";

	protected GeneralConfiguration() {
	}

	public GeneralConfiguration(Device device, String dosimeterId, String bssid, Date time) {
		setDevice(device);
		setDosimeterId(dosimeterId);
		setBSSID(bssid);
		setTime(time);
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "ID", length=15)
	public Long getId() {
		return id;
	}
	
	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}

	@Override
	@ManyToOne
	@JoinColumn(name="DEVICE_ID", nullable=false)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Device getDevice() {
		return device;
	}
	
	private void setDevice(Device device) {
		this.device = device;
	}

	@Column(name = "DOSIMETER_ID", length=15)
	public String getDosimeterId() {
		return dosimeterId;
	}
	
	private void setDosimeterId(String dosimeterId) {
		this.dosimeterId = dosimeterId;
	}
	
	@Column(name = "BSSID", length=20)
	public String getBSSID() {
		return bssid;
	}
	
	private void setBSSID(String bssid) {
		this.bssid = bssid;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATETIME", nullable=false)
	public Date getTime() {
		return time;
	}
	
	private void setTime(Date time) {
		this.time = time;
	}

	@Override
	@Transient
	public String getType() {
		return type;
	}

	@Override
	public int compareTo(GeneralConfiguration o) {
		return (o != null) && (getDosimeterId() != null) ? getDosimeterId()
				.compareTo(o.getDosimeterId()) : 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bssid == null) ? 0 : bssid.hashCode());
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result
				+ ((dosimeterId == null) ? 0 : dosimeterId.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GeneralConfiguration other = (GeneralConfiguration) obj;
		if (bssid == null) {
			if (other.bssid != null) {
				return false;
			}
		} else if (!bssid.equals(other.bssid)) {
			return false;
		}
		if (device == null) {
			if (other.device != null) {
				return false;
			}
		} else if (!device.equals(other.device)) {
			return false;
		}
		if (dosimeterId == null) {
			if (other.dosimeterId != null) {
				return false;
			}
		} else if (!dosimeterId.equals(other.dosimeterId)) {
			return false;
		}
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		} else if (!time.equals(other.time)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "GeneralConfiguration [device=" + device.getName() + ", dosimeterId="
				+ dosimeterId + ", bssid=" + bssid + ", time=" + time
				+ ", type=" + type + "]";
	}
}
