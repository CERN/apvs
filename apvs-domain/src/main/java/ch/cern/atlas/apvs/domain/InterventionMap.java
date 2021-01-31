package ch.cern.atlas.apvs.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;

//NOTE: implements IsSerializable in case serialization file cannot be found
public class InterventionMap implements Serializable, IsSerializable {

	private static final long serialVersionUID = 8868971785801918119L;

	private final Map<Device, Intervention> interventions = new HashMap<Device, Intervention>();

	public InterventionMap() {
		// serializable
	}

	// For Audio...
	public Intervention get(String name) {
		for (Device device : interventions.keySet()) {
			if (device.getName().equals(name)) {
				return get(device);
			}
		}
		return null;
	}

	public Intervention get(Device device) {
		return interventions.get(device);
	}

	public Intervention put(Device device, Intervention intervention) {
		return interventions.put(device, intervention);
	}

	public List<Device> getPtus() {
		List<Device> ptus = new ArrayList<Device>(interventions.keySet());
		Collections.sort(ptus);
		return ptus;
	}

	public void clear() {
		interventions.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InterventionMap) {
			return this.equalInterventionMap((InterventionMap) obj);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return interventions.entrySet().hashCode();
	}

	public boolean equalInterventionMap(InterventionMap obj) {
		Iterator<Entry<Device, Intervention>> entries_it = interventions
				.entrySet().iterator();

		List<Intervention> interventionList = new ArrayList<Intervention>();
		List<Intervention> objList = new ArrayList<Intervention>();

		while (entries_it.hasNext()) {
			Entry<Device, Intervention> thisEntry = entries_it.next();
			Intervention value = thisEntry.getValue();
			interventionList.add(value);
		}

		entries_it = obj.interventions.entrySet().iterator();

		while (entries_it.hasNext()) {
			Entry<Device, Intervention> thisEntry = entries_it.next();
			Intervention value = thisEntry.getValue();
			objList.add(value);
		}

		if (objList.size() != interventionList.size()) {
			return false;
		}

		for (int i = 0; i < objList.size(); i++) {

			// Description test
			if ((objList.get(i).getDescription() == null && interventionList
					.get(i).getDescription() == null)) {
			} else if (objList.get(i).getDescription()
					.equals(interventionList.get(i).getDescription())) {
			} else {
				return false;
			}

			// DeviceId test
			if ((objList.get(i).getDevice() == null && interventionList.get(i)
					.getDevice() == null)) {
			} else if (objList.get(i).getDevice()
					.equals(interventionList.get(i).getDevice())) {
			} else {
				return false;
			}

			// End Time test
			if ((objList.get(i).getEndTime() == null && interventionList.get(i)
					.getEndTime() == null)) {
			} else if (objList.get(i).getEndTime()
					.equals(interventionList.get(i).getEndTime())) {
			} else {
				return false;
			}

			// Start Time test
			if ((objList.get(i).getStartTime() == null && interventionList.get(
					i).getStartTime() == null)) {
			} else if (objList.get(i).getStartTime()
					.equals(interventionList.get(i).getStartTime())) {
			} else {
				return false;
			}

			// ID test
			if ((objList.get(i).getId() == interventionList.get(i).getId())) {
			} else {
				return false;
			}

			// Impact Number test
			if ((objList.get(i).getImpactNumber() == null && interventionList
					.get(i).getImpactNumber() == null)) {
			} else if (objList.get(i).getImpactNumber()
					.equals(interventionList.get(i).getImpactNumber())) {
			} else {
				return false;
			}

			// Name test
			if ((objList.get(i).getName() == null && interventionList.get(i)
					.getName() == null)) {
			} else if (objList.get(i).getName()
					.equals(interventionList.get(i).getName())) {
			} else {
				return false;
			}

			// PTU test
			if ((objList.get(i).getPtuId() == null && interventionList.get(i)
					.getPtuId() == null)) {
			} else if (objList.get(i).getPtuId()
					.equals(interventionList.get(i).getPtuId())) {
			} else {
				return false;
			}

			// REC Status test
			if ((objList.get(i).getRecStatus() == null && interventionList.get(
					i).getRecStatus() == null)) {
			} else if (objList.get(i).getRecStatus()
					.equals(interventionList.get(i).getRecStatus())) {
			} else {
				return false;
			}

			// User test
			if ((objList.get(i).getUser() == null && interventionList.get(i)
					.getUser() == null)) {
			} else if (objList.get(i).getUser()
					.equals(interventionList.get(i).getUser())) {
			} else {
				return false;
			}

		}

		return true;
	}

}
