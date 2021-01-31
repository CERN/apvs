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
@Table( name = "TBL_MEASUREMENT_CONFIGURATION" )
public class MeasurementConfiguration implements Message, Serializable, IsSerializable {

	private static final long serialVersionUID = -3852002718117362283L;

	private volatile Device device;
	private volatile Long id;
	private String sensor;
	private Date time;
	private String unit;
	private Integer samplingRate;
	private Double upThreshold;
	private Double downThreshold;
	private Double slope;
	private Double offset;
	
	private final String type = "MeasurementConfiguration";

	protected MeasurementConfiguration() {
	}
	
	public MeasurementConfiguration(Device device, String sensor,
			Double downThreshold, Double upThreshold, String unit,
			Integer samplingRate, Double slope, Double offset, Date time) {
		setDevice(device);
		setSensor(sensor);
		setDownThreshold(downThreshold);
		setUpThreshold(upThreshold);
		setUnit(unit);
		setSamplingRate(samplingRate);
		setSlope(slope);
		setOffset(offset);
		setTime(time);

		// Fix Unit for Body Temperature and Temperature
		if ((sensor != null) && (sensor.equals("Temperature") || sensor.equals("BodyTemperature"))
				&& (unit != null) && unit.equals("C")) {
			setUnit("&deg;C");
		}
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

	@Column(name = "SENSOR", length=50)
	public String getSensor() {
		return sensor;
	}
	
	private void setSensor(String sensor) {
		this.sensor = sensor;
	}

	@Column(name = "DOWN_THRES", length=20)
	@Type(type="double_string")
	public Double getDownThreshold() {
		return downThreshold;
	}
	
	private void setDownThreshold(Double downThreshold) {
		this.downThreshold = downThreshold;
	}

	@Column(name = "UP_THRES", length=20)
	@Type(type="double_string")
	public Double getUpThreshold() {
		return upThreshold;
	}

	private void setUpThreshold(Double upThreshold) {
		this.upThreshold = upThreshold;
	}

	@Column(name = "UNIT", length=20)
	public String getUnit() {
		return unit;
	}
	
	private void setUnit(String unit) {
		this.unit = unit;
	}
	
	@Column(name = "SAMPLINGRATE", length=20)
	@Type(type="integer_string")
	public Integer getSamplingRate() {
		return samplingRate;
	}
	
	private void setSamplingRate(Integer samplingRate) {
		this.samplingRate = samplingRate;
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
	@ManyToOne
	@JoinColumn(name="DEVICE_ID", nullable=false)
	@Cascade({CascadeType.SAVE_UPDATE})
	public Device getDevice() {
		return device;
	}
	
	private void setDevice(Device device) {
		this.device = device;
	}
	
	@Column(name = "SLOPE", length=20)
	@Type(type="double_string")
	public Double getSlope() {
		return slope;
	}
	
	private void setSlope(Double slope) {
		this.slope = slope;
	}
	
	@Column(name = "OFFSET", length=20)
	@Type(type="double_string")
	public Double getOffset() {
		return offset;
	}
	
	private void setOffset(Double offset) {
		this.offset = offset;
	}
	
	@Override
	public String toString() {
		return "MeasurementConfiguration [device=" + device.getName() + ", sensor="
				+ sensor + ", time=" + time + ", unit=" + unit
				+ ", samplingRate=" + samplingRate + ", upThreshold=" + upThreshold
				+ ", downThreshold=" + downThreshold + ", slope=" + slope + ", offset="
				+ offset + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result
				+ ((upThreshold == null) ? 0 : upThreshold.hashCode());
		result = prime * result
				+ ((downThreshold == null) ? 0 : downThreshold.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result
				+ ((samplingRate == null) ? 0 : samplingRate.hashCode());
		result = prime * result + ((sensor == null) ? 0 : sensor.hashCode());
		result = prime * result + ((slope == null) ? 0 : slope.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		MeasurementConfiguration other = (MeasurementConfiguration) obj;
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
		if (upThreshold == null) {
			if (other.upThreshold != null) {
				return false;
			}
		} else if (!upThreshold.equals(other.upThreshold)) {
			return false;
		}
		if (downThreshold == null) {
			if (other.downThreshold != null) {
				return false;
			}
		} else if (!downThreshold.equals(other.downThreshold)) {
			return false;
		}
		if (offset == null) {
			if (other.offset != null) {
				return false;
			}
		} else if (!offset.equals(other.offset)) {
			return false;
		}
		if (samplingRate == null) {
			if (other.samplingRate != null) {
				return false;
			}
		} else if (!samplingRate.equals(other.samplingRate)) {
			return false;
		}
		if (sensor == null) {
			if (other.sensor != null) {
				return false;
			}
		} else if (!sensor.equals(other.sensor)) {
			return false;
		}
		if (slope == null) {
			if (other.slope != null) {
				return false;
			}
		} else if (!slope.equals(other.slope)) {
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
		return true;
	}

}
