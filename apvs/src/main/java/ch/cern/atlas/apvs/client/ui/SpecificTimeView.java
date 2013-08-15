package ch.cern.atlas.apvs.client.ui;

import java.util.Date;

import org.moxieapps.gwt.highcharts.client.Chart;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.domain.Data;
import ch.cern.atlas.apvs.domain.Device;
import ch.cern.atlas.apvs.domain.History;
import ch.cern.atlas.apvs.domain.Intervention;
import ch.cern.atlas.apvs.domain.InterventionMap;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;

import com.google.web.bindery.event.shared.HandlerRegistration;

public class SpecificTimeView extends AbstractTimeView {

	private HandlerRegistration measurementHandler;

	public SpecificTimeView() {
	}

	public Chart createSingleChart(ClientFactory factory, String measurementName, Device device,
			History history, InterventionMap interventions, boolean showLimits) {
		createChart(Measurement.getDisplayName(measurementName) + " (" + device.getName()
				+ ")");
		add(chart);

		addSeries(device, getName(device, interventions), showLimits);
		addHistory(history.get(device, measurementName));

		register(factory, measurementName, device);
		
		return chart;
	}

	protected String getName(Device device, InterventionMap interventions) {
		if (interventions == null) {
			return device.getName();
		}
		Intervention intervention = interventions.get(device);
		return ((intervention != null) && !intervention.getName().equals("") ? intervention
				.getName() + " - "
				: "")
				+ "" + device.getName();
	}

	protected void addHistory(Data history) {
		if (history == null)
			return;

		setData(history.getPtu(), history.getData(), history.getLimits());

		setUnit(history.getPtu(), history.getUnit());
	}

	protected void unregister() {
		if (measurementHandler != null) {
			measurementHandler.removeHandler();
			measurementHandler = null;
		}
	}

	private final static long MINUTE = 60 * 1000; // 1 minute

	protected void register(ClientFactory factory, final String measurementName, final Device device) {
		unregister();

		measurementHandler = MeasurementChangedEvent.register(
				factory.getRemoteEventBus(),
				new MeasurementChangedEvent.Handler() {

					@Override
					public void onMeasurementChanged(
							MeasurementChangedEvent event) {
						Measurement m = event.getMeasurement();
						if (m.getSensor().equals(measurementName)
								&& ((device == null) || m.getDevice().equals(
										device))
								&& (m.getDate().getTime() < new Date()
										.getTime() + MINUTE)) {
							addPoint(m.getDevice(), m.getDate().getTime(),
									m.getValue(), m.getLowLimit(),
									m.getHighLimit());

							setUnit(m.getDevice(), m.getUnit());
						}
					}
				});
	}

}
