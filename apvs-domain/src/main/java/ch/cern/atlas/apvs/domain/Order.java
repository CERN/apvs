package ch.cern.atlas.apvs.domain;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

//NOTE: implements IsSerializable in case serialization file cannot be found
public class Order implements Message, Serializable, IsSerializable {

	private static final long serialVersionUID = 2917315876156737998L;

	private volatile Device device;
	private String parameter;
	private String value;
	private final String type = "Order";

	protected Order() {
	}

	public Order(Device device, String parameter, String value) {
		this.device = device;
		this.parameter = parameter;
		this.value = value;
	}

	@Override
	public Device getDevice() {
		return device;
	}

	public String getParameter() {
		return parameter;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return getType() + " " + getDevice().getName() + " " + getParameter() + " "
				+ getValue();
	}
}
