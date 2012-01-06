package ch.cern.atlas.apvs.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.client.traceplot.DataPoint;
import ch.cern.atlas.apvs.client.traceplot.DataSource;
import ch.cern.atlas.apvs.client.traceplot.TracePlot;

import com.google.gwt.user.client.ui.DockPanel;

public class TracePanel extends DockPanel {

	public TracePanel(ClientFactory clientFactory) {
		
//		add(new TraceView(clientFactory), EAST);

		TracePlot p1 = new TracePlot(600, 300);
		p1.setDataTable(new DataSource<Long, Double>() {

			@Override
			public Set<String> getSeriesNames() {
				return new HashSet<String>(Arrays
						.asList(new String[] { "Test" }));
			}

			@Override
			public Double getMinY() {
				return 0.0;
			}

			@Override
			public Long getMinX() {
				return 0L;
			}

			@Override
			public Double getMaxY() {
				return 1.0;
			}

			@Override
			public Long getMaxX() {
				return 3L;
			}

			@Override
			public List<DataPoint<Long, Double>> get(String series) {
				List<DataPoint<Long, Double>> points = new ArrayList<DataPoint<Long, Double>>();
				points.add(new DataPoint<Long, Double>(0L, 0.0));
				points.add(new DataPoint<Long, Double>(1L, 1.0));
				points.add(new DataPoint<Long, Double>(2L, 0.0));
				points.add(new DataPoint<Long, Double>(3L, 1.0));
				return points;
			}
		});
		
		add(p1, NORTH);

		TracePlot p2 = new TracePlot(600, 300);
		p2.setDataTable(new DataSource<Long, Double>() {

			@Override
			public Set<String> getSeriesNames() {
				return new HashSet<String>(Arrays
						.asList(new String[] { "Test" }));
			}

			@Override
			public Double getMinY() {
				return -1.0;
			}

			@Override
			public Long getMinX() {
				return 0L;
			}

			@Override
			public Double getMaxY() {
				return 1.0;
			}

			@Override
			public Long getMaxX() {
				return 3L;
			}

			@Override
			public List<DataPoint<Long, Double>> get(String series) {
				List<DataPoint<Long, Double>> points = new ArrayList<DataPoint<Long, Double>>();
				points.add(new DataPoint<Long, Double>(0L, 0.0));
				points.add(new DataPoint<Long, Double>(1L, 1.0));
				points.add(new DataPoint<Long, Double>(2L, -1.0));
				points.add(new DataPoint<Long, Double>(3L, 0.0));
				return points;
			}
		});
		
//		add(p2, NORTH);

		TracePlot p3 = new TracePlot(600, 300);
		p3.setDataTable(new DataSource<Long, Double>() {

			@Override
			public Set<String> getSeriesNames() {
				return new HashSet<String>(Arrays
						.asList(new String[] { "Test" }));
			}

			@Override
			public Double getMinY() {
				return -18.0;
			}

			@Override
			public Long getMinX() {
				return 0L;
			}

			@Override
			public Double getMaxY() {
				return 45.0;
			}

			@Override
			public Long getMaxX() {
				return 7L;
			}

			@Override
			public List<DataPoint<Long, Double>> get(String series) {
				List<DataPoint<Long, Double>> points = new ArrayList<DataPoint<Long, Double>>();
				points.add(new DataPoint<Long, Double>(0L, 20.2));
				points.add(new DataPoint<Long, Double>(2L, 45.0));
				points.add(new DataPoint<Long, Double>(5L, -18.0));
				points.add(new DataPoint<Long, Double>(7L, -4.5));
				return points;
			}
		});
		
//		add(p3, NORTH);

	}
}