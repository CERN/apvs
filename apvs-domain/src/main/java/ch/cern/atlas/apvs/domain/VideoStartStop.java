package ch.cern.atlas.apvs.domain;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

//NOTE: implements IsSerializable in case serialization file cannot be found
public class VideoStartStop implements Message, Serializable, IsSerializable {

	private static final long serialVersionUID = 6751105975861927018L;

	private volatile Device device;
	private String type;
	protected String macAddress;
	protected String hostName;
	protected String fileName;

	protected VideoStartStop() {
	}

	public VideoStartStop(Intervention intervention, boolean start) {
		this.device = intervention.getDevice();
		type = start ? "StartRecording" : "StopRecording";
		macAddress = device.getMacAddress().toString();
		hostName = device.getHostName();
		String impactNumber = intervention.getImpactNumber() != null ? intervention
				.getImpactNumber() : "";
		String name = intervention.getName() != null ? intervention.getName()
				: "";
		fileName = intervention.getId() + "_" + impactNumber
				+ "_" + name;
	}

	@Override
	public Device getDevice() {
		return device;
	}

	@Override
	public String getType() {
		return type;
	}
}
