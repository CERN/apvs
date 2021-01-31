package ch.cern.atlas.apvs.db;

import java.util.HashMap;
import java.util.Map;

import ch.cern.atlas.apvs.domain.Device;

public class SensorMap {

	private final Map<String, Boolean> enabled;
	
	public SensorMap() {
		enabled = new HashMap<String, Boolean>();
	}
	
	public void setEnabled(Device ptu, String sensor, boolean enabled) {
		this.enabled.put(getKey(ptu, sensor), enabled);
	}

	public boolean isEnabled(Device ptu, String sensor) {
		Boolean b = enabled.get(getKey(ptu, sensor));
		return b == null ? true : b;
	}
	
	private String getKey(Device ptu, String sensor) {
		return ptu.getName()+":"+sensor;
	}
}
