package ch.cern.atlas.apvs.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

//NOTE: implements IsSerializable in case serialization file cannot be found
public class Procedure extends DirectoryEntry implements Serializable, IsSerializable {

	private static final long serialVersionUID = 4875978323097992653L;

	private final List<Step> steps = new ArrayList<Step>();
	
	protected Procedure() {
	}

	public Step getStep(int sequenceNumber) {
		return steps.get(sequenceNumber);
	}
	
	public void addStep(Step step) {
		steps.add(step);
	}
}
