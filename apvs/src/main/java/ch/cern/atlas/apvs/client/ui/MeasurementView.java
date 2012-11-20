package ch.cern.atlas.apvs.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.client.domain.HistoryMap;
import ch.cern.atlas.apvs.client.domain.InterventionMap;
import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
import ch.cern.atlas.apvs.client.event.HistoryMapChangedEvent;
import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
import ch.cern.atlas.apvs.client.widget.ClickableTextCell;
import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
import ch.cern.atlas.apvs.client.widget.GlassPanel;
import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class MeasurementView extends GlassPanel implements Module {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	private static NumberFormat format = NumberFormat.getFormat("0.00");

	private InterventionMap interventions;
	private Measurement last = new Measurement();
	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
	private CellTable<String> table = new CellTable<String>();
	private ListHandler<String> columnSortHandler;
	private ClickableHtmlColumn<String> name;
	private SingleSelectionModel<String> selectionModel;

	private List<String> show = null;

	private String ptuId = null;

	private RemoteEventBus remoteEventBus;
	private EventBus cmdBus;

	private boolean showHeader = true;
	private boolean showName = true;
	private boolean sortable = true;
	private boolean selectable = true;
	private boolean showDate = false;

	private String options;

	private HandlerRegistration measurementHandler;

	private UpdateScheduler scheduler = new UpdateScheduler(this);

	private boolean daqOk;
	private boolean databaseOk;

	private HistoryMap historyMap;

	public MeasurementView() {
	}

	@Override
	public boolean configure(Element element, ClientFactory clientFactory,
			Arguments args) {

		remoteEventBus = clientFactory.getRemoteEventBus();
		cmdBus = clientFactory.getEventBus(args.getArg(0));
		options = args.getArg(1);
		show = args.getArgs(2);

		showHeader = !options.contains("NoHeader");
		showName = !options.contains("NoName");
		sortable = !options.contains("NoSort");
		selectable = !options.contains("NoSelection");
		showDate = options.contains("Date");

		if (selectable) {
			selectionModel = new SingleSelectionModel<String>();
		}

		add(table, CENTER);

		ConnectionStatusChangedRemoteEvent.subscribe(remoteEventBus,
				new ConnectionStatusChangedRemoteEvent.Handler() {

					@Override
					public void onConnectionStatusChanged(
							ConnectionStatusChangedRemoteEvent event) {
						switch (event.getConnection()) {
						case daq:
							daqOk = event.isOk();
							break;
						case database:
							databaseOk = event.isOk();
							break;
						default:
							break;
						}

						showGlass(!daqOk || !databaseOk);
					}
				});

		InterventionMapChangedRemoteEvent.subscribe(remoteEventBus,
				new InterventionMapChangedRemoteEvent.Handler() {

					@Override
					public void onInterventionMapChanged(
							InterventionMapChangedRemoteEvent event) {
						interventions = event.getInterventionMap();
						changePtuId();
						scheduler.update();
					}
				});

		HistoryMapChangedEvent.subscribe(remoteEventBus,
				new HistoryMapChangedEvent.Handler() {

					@Override
					public void onHistoryMapChanged(HistoryMapChangedEvent event) {
						historyMap = event.getHistoryMap();
						changePtuId();
						scheduler.update();
					}
				});

		SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {

			@Override
			public void onPtuSelected(final SelectPtuEvent event) {
				ptuId = event.getPtuId();

				changePtuId();
				scheduler.update();
			}
		});

		name = new ClickableHtmlColumn<String>() {
			@Override
			public String getValue(String name) {
				return historyMap.getDisplayName(name);
			}

			@Override
			public void render(Context context, String name, SafeHtmlBuilder sb) {
				String s = getValue(name);
				Measurement m = historyMap.getMeasurement(ptuId, name);
				if (m == null) {
					return;
				}
				((ClickableTextCell) getCell()).render(context, decorate(s, m),
						sb);
			}
		};
		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		name.setSortable(sortable);
		if (selectable) {
			name.setFieldUpdater(new FieldUpdater<String, String>() {

				@Override
				public void update(int index, String object, String value) {
					selectMeasurement(object);
				}
			});
		}
		table.addColumn(name, showHeader ? new TextHeader("") {
			@Override
			public String getValue() {
				if (!showName)
					return null;

				return ptuId;
			}

			public void render(Context context, SafeHtmlBuilder sb) {
				String s = getValue();
				if (s != null) {
					s = "PTU Id: " + ptuId;

					if (interventions != null) {
						String realName = interventions.get(ptuId) != null ? interventions
								.get(ptuId).getName() : null;

						if (realName != null) {
							s = "<div title=\"" + s + "\">" + realName
									+ "</div>";
						}
					}

					sb.append(SafeHtmlUtils.fromSafeConstant(s));
				}
			};
		}
				: null);

		ClickableTextColumn<String> value = new ClickableTextColumn<String>() {
			@Override
			public String getValue(String name) {
				if ((name == null) || (historyMap == null) || (ptuId == null)) {
					return "";
				}
				Measurement m = historyMap.getMeasurement(ptuId, name);
				return m != null ? format.format(m.getValue()) : "";
			}

			@Override
			public void render(Context context, String name, SafeHtmlBuilder sb) {
				String s = getValue(name);
				Measurement m = historyMap.getMeasurement(ptuId, name);
				if (m == null) {
					return;
				}
				((ClickableTextCell) getCell()).render(context,
						decorate(s, m, last), sb);
			}

		};
		value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		if (selectable) {
			value.setFieldUpdater(new FieldUpdater<String, String>() {

				@Override
				public void update(int index, String object, String value) {
					selectMeasurement(object);
				}
			});
		}
		table.addColumn(value, showHeader ? new TextHeader("Value")
				: (Header<?>) null);

		ClickableHtmlColumn<String> unit = new ClickableHtmlColumn<String>() {
			@Override
			public String getValue(String name) {
				return historyMap.getUnit(name);
			}

			@Override
			public void render(Context context, String name, SafeHtmlBuilder sb) {
				String s = getValue(name);
				Measurement m = historyMap.getMeasurement(ptuId, name);
				if (m == null) {
					return;
				}
				((ClickableTextCell) getCell()).render(context, decorate(s, m),
						sb);
			}
		};
		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		unit.setSortable(sortable);
		if (selectable) {
			unit.setFieldUpdater(new FieldUpdater<String, String>() {

				@Override
				public void update(int index, String object, String value) {
					selectMeasurement(object);
				}
			});
		}
		table.addColumn(unit, showHeader ? new TextHeader("Unit")
				: (Header<?>) null);

		ClickableHtmlColumn<String> date = new ClickableHtmlColumn<String>() {
			@Override
			public String getValue(String name) {
				return PtuClientConstants.dateFormat.format(historyMap
						.getMeasurement(ptuId, name).getDate());
			}

			@Override
			public void render(Context context, String name, SafeHtmlBuilder sb) {
				String s = getValue(name);
				Measurement m = historyMap.getMeasurement(ptuId, name);
				if (m == null) {
					return;
				}
				((ClickableTextCell) getCell()).render(context, decorate(s, m),
						sb);
			}
		};
		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		unit.setSortable(sortable);
		if (selectable) {
			unit.setFieldUpdater(new FieldUpdater<String, String>() {

				@Override
				public void update(int index, String object, String value) {
					selectMeasurement(object);
				}
			});
		}
		if (showDate) {
			table.addColumn(date, showHeader ? new TextHeader("Date")
					: (Header<?>) null);
		}

		List<String> list = new ArrayList<String>();
		dataProvider.addDataDisplay(table);
		dataProvider.setList(list);

		columnSortHandler = new ListHandler<String>(dataProvider.getList());
		columnSortHandler.setComparator(name, new Comparator<String>() {
			public int compare(String s1, String s2) {
				if (s1 == s2) {
					return 0;
				}

				if (s1 != null) {
					return s1.compareTo(s2);
				}
				return -1;
			}
		});
		columnSortHandler.setComparator(value, new Comparator<String>() {
			public int compare(String s1, String s2) {
				Measurement o1 = historyMap.getMeasurement(ptuId, s1);
				Measurement o2 = historyMap.getMeasurement(ptuId, s2);

				if (o1 == o2) {
					return 0;
				}

				if ((o1 != null) && (o1.getValue() != null)) {
					if ((o2 != null) && (o2.getValue() != null)) {
						double d1 = o1.getValue().doubleValue();
						double d2 = o2.getValue().doubleValue();
						return d1 < d2 ? -1 : d1 == d2 ? 0 : 1;
					}
					return 1;
				}
				return -1;
			}
		});
		columnSortHandler.setComparator(unit, new Comparator<String>() {
			public int compare(String s1, String s2) {
				Measurement o1 = historyMap.getMeasurement(ptuId, s1);
				Measurement o2 = historyMap.getMeasurement(ptuId, s2);

				if (o1 == o2) {
					return 0;
				}

				if (o1 != null) {
					return (o2 != null) ? o1.getUnit().compareTo(o2.getUnit())
							: 1;
				}
				return -1;
			}
		});
		columnSortHandler.setComparator(date, new Comparator<String>() {
			public int compare(String s1, String s2) {
				Measurement o1 = historyMap.getMeasurement(ptuId, s1);
				Measurement o2 = historyMap.getMeasurement(ptuId, s2);

				if (o1 == o2) {
					return 0;
				}

				if (o1 != null) {
					return (o2 != null) ? o1.getDate().compareTo(o2.getDate())
							: 1;
				}
				return -1;
			}
		});
		table.addColumnSortHandler(columnSortHandler);
		table.getColumnSortList().push(name);

		if (selectable) {
			table.setSelectionModel(selectionModel);
			selectionModel
					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

						@Override
						public void onSelectionChange(SelectionChangeEvent event) {
							String s = selectionModel.getSelectedObject();
							log.info(s + " " + event.getSource());
						}
					});
		}

		return true;
	}

	private void changePtuId() {
		dataProvider.getList().clear();
		if (measurementHandler != null) {
			measurementHandler.removeHandler();
			measurementHandler = null;
		}

		if (interventions == null) {
			return;
		}

		if (historyMap == null) {
			return;
		}

		for (String ptuId : interventions.getPtuIds()) {
			for (Measurement measurement : historyMap.getMeasurements(ptuId)) {
				replace(measurement);
			}
		}
		scheduler.update();

		measurementHandler = MeasurementChangedEvent.register(remoteEventBus,
				new MeasurementChangedEvent.Handler() {

					@Override
					public void onMeasurementChanged(
							MeasurementChangedEvent event) {
						Measurement measurement = event.getMeasurement();
						if (measurement.getPtuId().equals(ptuId)) {
							last = replace(measurement);
							scheduler.update();
						}
					}
				});

	}

	/**
	 * Decorate with arrow up, down, left if a value went up, down or stayed the
	 * same. Only applies to last value. Also calls standard decorate method.
	 * 
	 * @param s
	 * @param current
	 * @param last
	 * @return
	 */
	public static SafeHtml decorate(String s, Measurement current,
			Measurement last) {
		if ((current != null) && (last != null) && (current.getPtuId() != null)
				&& (current.getPtuId().equals(last.getPtuId()))
				&& (current.getName() != null)
				&& current.getName().equals(last.getName())) {
			double c = current.getValue().doubleValue();
			double l = last.getValue().doubleValue();
			String a = (c == l) ? "&larr;" : (c > l) ? "&uarr;" : "&darr;";
			s = a + "&nbsp;<b>" + s + "</b>";
		}
		return decorate(s, current);
	}

	/**
	 * Adds date/time as tooltip. Shows future values (beyond 1 minute) in bold,
	 * values older than 5 minutes in italics and makes values older than a day
	 * more transparent.
	 * 
	 * @param s
	 * @param current
	 * @return
	 */
	public static SafeHtml decorate(String s, Measurement current) {
		long now = new Date().getTime();
		long future1min = now + (60 * 1000);
		long past5mins = now - (5 * 60 * 1000);
		long pastDay = now - (24 * 3600 * 1000);
		long time = current.getDate().getTime();
		if (time > future1min) {
			s = "<b>" + s + "</b>";
		} else if (time < past5mins) {
			s = "<i>" + s + "</i>";
		}

		// make text more transparent
		if (time < pastDay) {
			s = "<span style=\"opacity: 0.5;\">" + s + "</span>";
		}
		// Add date in tooltip
		s = "<div title=\""
				+ PtuClientConstants.dateFormat.format(current.getDate())
				+ "\">" + s + "</div>";
		return SafeHtmlUtils.fromSafeConstant(s);
	}

	@Override
	public boolean update() {
		// Re-sort the table
		if (sortable) {
			ColumnSortEvent.fire(table, table.getColumnSortList());
		}

		if (selectable) {
			String selection = selectionModel.getSelectedObject();

			if ((selection == null) && (dataProvider.getList().size() > 0)) {
				selection = dataProvider.getList().get(0);

				selectMeasurement(selection);
			}

			// re-set the selection as the async update may have changed the
			// rendering
			if (selection != null) {
				selectionModel.setSelected(selection, true);
			}
		}

		table.redraw();

		return false;
	}

	private Measurement replace(Measurement measurement) {

		List<String> list = dataProvider.getList();
		Measurement lastValue = historyMap.getMeasurement(
				measurement.getPtuId(), measurement.getName());

		if (!list.contains(measurement.getName())) {
			if ((show == null) || (show.size() == 0)
					|| (show.contains(measurement.getName()))) {
				list.add(measurement.getName());
				lastValue = measurement;
			} else {
				lastValue = null;
			}
		}

		return lastValue;
	}

	private void selectMeasurement(String name) {
		SelectMeasurementEvent.fire(cmdBus, name);
	}

}
