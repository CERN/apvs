package ch.cern.atlas.apvs.ptu.server;

import java.util.Date;

import ch.cern.atlas.apvs.domain.Measurement;

@SuppressWarnings("serial")
public class CO2 extends Measurement<Double> {

	public CO2(int ptuId, double value) {
		super(ptuId, "CO<sub>2</sub> Sensor", value, "ppm", new Date());
	}
	
}
