package ch.cern.atlas.apvs.ptu.server;

import java.util.HashMap;
import java.util.Map;

/**
 * NO LONGER USED
 * 
 * TBR
 * @author duns
 *
 */
public class Limits {

	private static final Map<String, Number> high = new HashMap<String, Number>();
	private static final Map<String, Number> low = new HashMap<String, Number>();
	
	static {
		low.put("BodyTemperature", 35);
		low.put("CO2", 0.1);
		low.put("DoseAccum", null);
		low.put("DoseRate", 0.0);
		low.put("HeartRate", 50);
		low.put("Humidity", 10);
		low.put("O2", 20.5);
		low.put("Temperature", 27);

		high.put("BodyTemperature", 39);
		high.put("CO2", 0.6);
		high.put("DoseAccum", null);
		high.put("DoseRate", 0.0075);
		high.put("HeartRate", 175);
		high.put("Humidity", 20);
		high.put("O2", 21.5);
		high.put("Temperature", 31);
	}
	
	private Limits() {
	}
	
	public static Number getHigh(String name) {
		return high.get(name);
	}

	public static Number getLow(String name) {
		return low.get(name);
	}

}
