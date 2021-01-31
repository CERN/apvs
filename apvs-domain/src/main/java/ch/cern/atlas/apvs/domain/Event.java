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
import org.hibernate.annotations.Type;

import com.google.gwt.user.client.rpc.IsSerializable;

//NOTE: implements IsSerializable in case serialization file cannot be found
@Entity
@Table( name = "TBL_EVENTS" )
public class Event implements Message, Serializable, IsSerializable {

	private static final long serialVersionUID = -5549380949230727273L;

	private volatile int id;
	private volatile Device device;
	private final String type = "Event";
	private String sensor;
	private String eventType;
	private Double value;
	private Double threshold;
	private String unit;
	private Date time;

	protected Event() {
	}
	
	public Event(Device device, String name, String eventType, Date time) {
		this(device, name, eventType, 0.0, 0.0, "", time);
	}

	public Event(Device device, String sensor, String eventType, Double value,
			Double threshold, String unit, Date time) {

		setDevice(device);
		setSensor(sensor);
		setEventType(eventType);
		setValue(value);
		setThreshold(threshold);
		setUnit(unit);
		setTime(time);
	}
	
	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}
		
	private void setTime(Date time) {
		this.time = time;
	}

	private void setUnit(String unit) {
		this.unit = unit;
	}

	private void setValue(Double value) {
		this.value = value;
	}

	private void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	private void setEventType(String eventType) {
		this.eventType = eventType;
	}

	private void setSensor(String sensor) {
		this.sensor = sensor;
	}

	private void setDevice(Device device) {
		this.device = device;
	}

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "ID", length=15)
	public int getId() {
		return id;
	}

	@Override
	@ManyToOne
	@JoinColumn(name="DEVICE_ID", nullable=false)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Device getDevice() {
		return device;
	}

	@Column(name = "SENSOR", length=50)
	public String getSensor() {
		return sensor;
	}

	@Column(name = "EVENT_TYPE", length=50, nullable=false)
	public String getEventType() {
		return eventType;
	}

	@Column(name = "VALUE")
	@Type(type="double_string")
	public Double getValue() {
		return value;
	}

	@Column(name = "THRESHOLD")
	@Type(type="double_string")
	public Double getThreshold() {
		return threshold;
	}

	@Column(name = "UNIT", length=20)
	public String getUnit() {
		return unit;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATETIME", nullable=false)
	public Date getTime() {
		return time;
	}

	@Override
	@Transient
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Event(" + getDevice().getName() + "): sensor:" + getSensor() + ", type: "
				+ getEventType() + ", value:" + getValue() + ", unit:"
				+ getUnit() + ", threshold:" + getThreshold() + ", date:"
				+ getTime();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result
				+ ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + id;
		result = prime * result + ((sensor == null) ? 0 : sensor.hashCode());
		result = prime * result
				+ ((threshold == null) ? 0 : threshold.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Event other = (Event) obj;
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		} else if (!time.equals(other.time)) {
			return false;
		}
		if (device == null) {
			if (other.device != null) {
				return false;
			}
		} else if (!device.equals(other.device)) {
			return false;
		}
		if (eventType == null) {
			if (other.eventType != null) {
				return false;
			}
		} else if (!eventType.equals(other.eventType)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		if (sensor == null) {
			if (other.sensor != null) {
				return false;
			}
		} else if (!sensor.equals(other.sensor)) {
			return false;
		}
		if (threshold == null) {
			if (other.threshold != null) {
				return false;
			}
		} else if (!threshold.equals(other.threshold)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (unit == null) {
			if (other.unit != null) {
				return false;
			}
		} else if (!unit.equals(other.unit)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
}
