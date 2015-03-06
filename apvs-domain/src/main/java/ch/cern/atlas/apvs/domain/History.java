package ch.cern.atlas.apvs.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class History implements Serializable, IsSerializable {

	private static final long serialVersionUID = 7456811406485472931L;
	private Map<Device, DeviceData> map = new HashMap<Device, DeviceData>();
	private Map<String, String> units = new HashMap<String, String>();
	
	public History() {
	}

	public Data get(Device device, String sensor) {
		DeviceData deviceData = map.get(device);
		if (deviceData == null) {
			return null;
		}
		return deviceData.get(sensor);
	}
	
	public void put(DeviceData deviceData) {
		map.put(deviceData.getDevice(), deviceData);
		for (String sensor : deviceData.getSensors()) {
			units.put(sensor, deviceData.get(sensor).getUnit());
		}
	}

	public void put(Data data) {
		DeviceData deviceData = map.get(data.getPtu());
		if (deviceData == null) {
			deviceData = new DeviceData(data.getPtu());
			map.put(data.getPtu(), deviceData);
		}
		
		String name = data.getName();
		units.put(name, data.getUnit());

		deviceData.put(data);
	}

	public Measurement getCurrentMeasurement(Device device, String sensor) {
		Data data = get(device, sensor);
		if (data == null) {
			return null;
		}
		return data.getCurrentMeasurement();
	}

	public Measurement getLastMeasurement(Device device, String sensor) {
		Data data = get(device, sensor);
		if (data == null) {
			return null;
		}
		return data.getLastMeasurement();
	}

	public List<Measurement> getMeasurements(Device device) {
		DeviceData deviceData = map.get(device);
		if (deviceData == null) {
			return Collections.emptyList();
		}
		
		return deviceData.getMeasurements();
	}
	
	public String getUnit(String name) {
		return units.get(name);
	}

}
