package ch.cern.atlas.apvs.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedEvent;
import ch.cern.atlas.apvs.client.event.InterventionMapChangedEvent;
import ch.cern.atlas.apvs.client.event.PtuSettingsChangedEvent;
import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
import ch.cern.atlas.apvs.client.service.PtuServiceAsync;
import ch.cern.atlas.apvs.client.settings.InterventionMap;
import ch.cern.atlas.apvs.client.settings.PtuSettings;
import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
import ch.cern.atlas.apvs.client.widget.ClickableTextCell;
import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
import ch.cern.atlas.apvs.client.widget.GlassPanel;
import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
import ch.cern.atlas.apvs.domain.APVSException;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.Ptu;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;

public class PtuView extends GlassPanel implements Module {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	private static NumberFormat format = NumberFormat.getFormat("0.00");

	private RemoteEventBus eventBus;
	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
	private CellTable<String> table = new CellTable<String>();

	private List<String> ptuIds;
	private Measurement last;
	private Map<String, Ptu> ptus;
	private Map<String, String> displayNames;
	private Map<String, String> units;
	private Map<String, ClickableTextColumn<String>> columns;
	private SingleSelectionModel<String> selectionModel;
	private Map<String, String> colorMap = new HashMap<String, String>();

	private PtuSettings settings;
	private InterventionMap interventions;
	private EventBus cmdBus;

	private UpdateScheduler scheduler = new UpdateScheduler(this);

	protected boolean daqOk;

	protected boolean databaseOk;

	private void init() {
		last = new Measurement();
		ptus = new HashMap<String, Ptu>();
		units = new HashMap<String, String>();
		columns = new HashMap<String, ClickableTextColumn<String>>();
		displayNames = new HashMap<String, String>();
	}

	public PtuView() {
	}

	@Override
	public boolean configure(Element element, ClientFactory clientFactory,
			Arguments args) {

		eventBus = clientFactory.getRemoteEventBus();

		init();

		cmdBus = clientFactory.getEventBus(args.getArg(0));

		add(table, CENTER);

		// name column
		ClickableHtmlColumn<String> name = new ClickableHtmlColumn<String>() {
			@Override
			public String getValue(String object) {
				return displayNames.get(object);
			}
		};
		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		name.setSortable(true);
		name.setFieldUpdater(new FieldUpdater<String, String>() {

			@Override
			public void update(int index, String name, String value) {
				selectMeasurementAndPtu(name, null);
			}
		});
		table.addColumn(name, "Name");

		// unit column
		ClickableHtmlColumn<String> unit = new ClickableHtmlColumn<String>() {
			@Override
			public String getValue(String object) {
				return units.get(object);
			}
		};
		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		unit.setFieldUpdater(new FieldUpdater<String, String>() {

			@Override
			public void update(int index, String name, String value) {
				selectMeasurementAndPtu(name, null);
			}
		});
		table.addColumn(unit, "Unit");

		dataProvider.addDataDisplay(table);
		dataProvider.setList(new ArrayList<String>());

		ListHandler<String> columnSortHandler = new ListHandler<String>(
				dataProvider.getList());
		columnSortHandler.setComparator(name, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1 != null ? o1.compareTo(o2) : -1;
			}
		});
		table.addColumnSortHandler(columnSortHandler);
		table.getColumnSortList().push(name);

		selectionModel = new SingleSelectionModel<String>();
		table.setSelectionModel(selectionModel);

		ConnectionStatusChangedEvent.subscribe(
				clientFactory.getRemoteEventBus(),
				new ConnectionStatusChangedEvent.Handler() {

					@Override
					public void onConnectionStatusChanged(
							ConnectionStatusChangedEvent event) {
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

		MeasurementChangedEvent.register(eventBus,
				new MeasurementChangedEvent.Handler() {

					@Override
					public void onMeasurementChanged(
							MeasurementChangedEvent event) {
						Measurement measurement = event.getMeasurement();
						last = addOrReplaceMeasurement(measurement);
						scheduler.update();
					}
				});

		PtuSettingsChangedEvent.subscribe(eventBus,
				new PtuSettingsChangedEvent.Handler() {

					@Override
					public void onPtuSettingsChanged(
							PtuSettingsChangedEvent event) {
						settings = event.getPtuSettings();
						configChanged();
						scheduler.update();
					}
				});

		InterventionMapChangedEvent.subscribe(eventBus,
				new InterventionMapChangedEvent.Handler() {

					@Override
					public void onInterventionMapChanged(
							InterventionMapChangedEvent event) {
						interventions = event.getInterventionMap();

						ptuIds = interventions.getPtuIds();

						for (Iterator<String> i = ptus.keySet().iterator(); i
								.hasNext();) {
							if (!ptuIds.contains(i.next())) {
								i.remove();
							}
						}

						for (String ptuId : ptuIds) {
							if (ptus.get(ptuId) == null) {
								ptus.put(ptuId, new Ptu(ptuId));
							}
						}

						configChanged();
						scheduler.update();
					}
				});

		if (cmdBus != null) {
			ColorMapChangedEvent.subscribe(cmdBus,
					new ColorMapChangedEvent.Handler() {
						@Override
						public void onColorMapChanged(ColorMapChangedEvent event) {
							colorMap = event.getColorMap();
							scheduler.update();
						}

					});
		}

		return true;
	}

	private Measurement addOrReplaceMeasurement(Measurement measurement) {
		String ptuId = measurement.getPtuId();
		if ((ptuIds == null) || !ptuIds.contains(ptuId))
			return null;

		Ptu ptu = ptus.get(ptuId);

		Measurement lastValue = null;

		String name = measurement.getName();
		try {
			if (ptu.getMeasurement(name) == null) {
				units.put(name, measurement.getUnit());
				displayNames.put(name, measurement.getDisplayName());
				ptu.addMeasurement(measurement);
				lastValue = measurement;
			} else {
				lastValue = ptu.addMeasurement(measurement);
			}
		} catch (APVSException e) {
			log.warn("Could not add measurement", e);
		}

		if (!dataProvider.getList().contains(name)) {
			dataProvider.getList().add(name);
		}

		return lastValue;
	}

	private void addColumn(final String ptuId) {
		ClickableTextColumn<String> column = new ClickableTextColumn<String>() {
			@Override
			public String getValue(String object) {
				Ptu ptu = ptus.get(ptuId);
				if (ptu == null) {
					return "";
				}

				Measurement m = ptu.getMeasurement(object);
				if (m == null) {
					return "";
				}
				return format.format(m.getValue());
			}

			@Override
			public void render(Context context, String object,
					SafeHtmlBuilder sb) {
				Ptu ptu = ptus.get(ptuId);
				if (ptu == null) {
					((ClickableTextCell) getCell()).render(context, "", sb);
				} else {
					String color = colorMap.get(ptuId);
					boolean isSelected = selectionModel.isSelected(object);
					if ((color != null) && isSelected) {
						sb.append(SafeHtmlUtils
								.fromSafeConstant("<div style=\"background:"
										+ color + "\">"));
					}

					Measurement m = ptu.getMeasurement(object);
					if (m != null) {
						((ClickableTextCell) getCell()).render(context,
								MeasurementView.decorate(getValue(object), m,
										last), sb);
					}
					if ((color != null) && isSelected) {
						sb.append(SafeHtmlUtils.fromSafeConstant("</div>"));
					}
				}
			}

		};
		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		column.setFieldUpdater(new FieldUpdater<String, String>() {

			@Override
			public void update(int index, String name, String value) {
				selectMeasurementAndPtu(name, ptuId);
			}
		});

		table.addColumn(column, new TextHeader("") {
			@Override
			public String getValue() {
				String name = interventions != null ? interventions.get(ptuId) != null ? interventions
						.get(ptuId).getName() : null
						: null;
				return name != null ? name + "<br/>(" + ptuId.toString() + ")"
						: ptuId.toString();
			}

			@Override
			public void render(Context context, SafeHtmlBuilder sb) {
				((TextCell) getCell()).render(context,
						SafeHtmlUtils.fromSafeConstant(getValue()), sb);
			}
		});

		columns.put(ptuId, column);
	}

	private void configChanged() {

		if (ptuIds != null) {
			// Remove any dead columns
			for (Iterator<Map.Entry<String, ClickableTextColumn<String>>> i = columns
					.entrySet().iterator(); i.hasNext();) {
				Map.Entry<String, ClickableTextColumn<String>> entry = i.next();
				if (ptuIds.contains(entry.getKey()))
					continue;

				ClickableTextColumn<String> column = entry.getValue();
				table.removeColumn(column);
				i.remove();
			}

			// add any new columns...
			boolean newColumns = false;
			for (Iterator<String> i = ptuIds.iterator(); i.hasNext();) {
				String ptuId = i.next();
				ClickableTextColumn<String> column = columns.get(ptuId);

				if ((settings == null) || settings.isEnabled(ptuId)) {
					if (column == null) {
						addColumn(ptuId);
						newColumns = true;

//						PtuServiceAsync.Util.getInstance().getMeasurements(
//								ptuId, new AsyncCallback<List<Measurement>>() {
//
//									@Override
//									public void onSuccess(
//											List<Measurement> result) {
//										for (Iterator<Measurement> i = result
//												.iterator(); i.hasNext();) {
//											last = addOrReplaceMeasurement(i
//													.next());
//										}
//										scheduler.update();
//									}
//
//									@Override
//									public void onFailure(Throwable caught) {
//										log.warn("PtuView getMeasurements failed "
//												+ caught);
//									}
//								});
					}
				} else {
					// remove a column that was disabled
					if (column != null) {
						table.removeColumn(column);
					}
				}
			}
			
			if (newColumns) {
				PtuServiceAsync.Util.getInstance().getMeasurements(null, null, new AsyncCallback<List<Measurement>>() {

							@Override
							public void onSuccess(List<Measurement> result) {
								for (Iterator<Measurement> i = result
										.iterator(); i.hasNext();) {
									last = addOrReplaceMeasurement(i
											.next());
								}
								scheduler.update();
							}

							@Override
							public void onFailure(Throwable caught) {
								log.warn("PtuView getMeasurements failed "
										+ caught);
							}
						});

			}
		} else {
			init();
		}
	}

	@Override
	public boolean update() {
		ColumnSortEvent.fire(table, table.getColumnSortList());
		table.redraw();

		String selection = selectionModel.getSelectedObject();
		if ((selection == null) && (dataProvider.getList().size() > 0)) {
			selection = dataProvider.getList().get(0);

			selectMeasurementAndPtu(selection, null);
		}

		// re-set the selection as the async update may have changed the
		// rendering
		if (selection != null) {
			selectionModel.setSelected(selection, true);
		}
		return false;
	}

	private void selectMeasurementAndPtu(String name, String ptuId) {
		SelectMeasurementEvent.fire(cmdBus, name);
		SelectPtuEvent.fire(cmdBus, ptuId);
	}
}
