package ch.cern.atlas.apvs.client;

import java.util.logging.Logger;

import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
import ch.cern.atlas.apvs.client.service.ServerServiceAsync;
import ch.cern.atlas.apvs.client.settings.SettingsPersister;
import ch.cern.atlas.apvs.client.tablet.AppBundle;
import ch.cern.atlas.apvs.client.tablet.HomePlace;
import ch.cern.atlas.apvs.client.tablet.TabletHistoryObserver;
import ch.cern.atlas.apvs.client.tablet.TabletMenuActivityMapper;
import ch.cern.atlas.apvs.client.tablet.TabletMenuAnimationMapper;
import ch.cern.atlas.apvs.client.tablet.TabletPanelActivityMapper;
import ch.cern.atlas.apvs.client.tablet.TabletPanelAnimationMapper;
import ch.cern.atlas.apvs.client.tablet.TabletPlaceHistoryMapper;
import ch.cern.atlas.apvs.client.ui.Arguments;
import ch.cern.atlas.apvs.client.ui.AudioView;
import ch.cern.atlas.apvs.client.ui.CameraView;
import ch.cern.atlas.apvs.client.ui.DosimeterView;
import ch.cern.atlas.apvs.client.ui.EventView;
import ch.cern.atlas.apvs.client.ui.MeasurementView;
import ch.cern.atlas.apvs.client.ui.PlaceView;
import ch.cern.atlas.apvs.client.ui.ProcedureControls;
import ch.cern.atlas.apvs.client.ui.ProcedureView;
import ch.cern.atlas.apvs.client.ui.PtuSettingsView;
import ch.cern.atlas.apvs.client.ui.PtuTabSelector;
import ch.cern.atlas.apvs.client.ui.PtuView;
import ch.cern.atlas.apvs.client.ui.ServerSettingsView;
import ch.cern.atlas.apvs.client.ui.Tabs;
import ch.cern.atlas.apvs.client.ui.TimeView;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.googlecode.mgwt.mvp.client.AnimatableDisplay;
import com.googlecode.mgwt.mvp.client.AnimatingActivityManager;
import com.googlecode.mgwt.mvp.client.AnimationMapper;
import com.googlecode.mgwt.mvp.client.history.MGWTPlaceHistoryHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.MGWTSettings.ViewPort;
import com.googlecode.mgwt.ui.client.MGWTSettings.ViewPort.DENSITY;
import com.googlecode.mgwt.ui.client.dialog.TabletPortraitOverlay;
import com.googlecode.mgwt.ui.client.layout.MasterRegionHandler;
import com.googlecode.mgwt.ui.client.layout.OrientationRegionHandler;

/**
 * @author Mark Donszelmann
 */
public class APVS implements EntryPoint {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(getClass().getName());
	@SuppressWarnings("unused")
	private Window screen;

	private RemoteEventBus remoteEventBus;
	@SuppressWarnings("unused")
	private PlaceController placeController;
	@SuppressWarnings("unused")
	private SettingsPersister settingsPersister;

	@Override
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(new APVSUncaughtExceptionHandler());

		Build build = GWT.create(Build.class);
		System.out.println("Starting APVS Version: " + build.version() + " - "
				+ build.build());

		ServerServiceAsync.Util.getInstance().isReady(
				new AsyncCallback<Boolean>() {

					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							System.err.println("Server ready");
							start();
						} else {
							onFailure(null);
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Server not ready. reload webpage "
								+ caught);
					}
				});
	}

	private void start() {

		ClientFactory clientFactory = GWT.create(ClientFactory.class);

		remoteEventBus = clientFactory.getRemoteEventBus();
		placeController = clientFactory.getPlaceController();

		// Turn off the browser scrollbars.
		Window.enableScrolling(false);

		settingsPersister = new SettingsPersister(remoteEventBus);

		// get first div element
		NodeList<Element> divs = Document.get().getElementsByTagName("div");
		if (divs.getLength() == 0) {
			Window.alert("Please define a <div> element with the class set to your view you want to show.");
			return;
		}

		boolean newCode = false;
		for (int i = 0; i < divs.getLength(); i++) {
			Element element = divs.getItem(i);
			String id = element.getId();

			String[] parts = id.split("\\(", 2);
			if (parts.length != 2) {
				// tab div
				if (element.getClassName().equals("tab")) {
					Tabs.add(id, element);
				}
			} else {

				String className = parts[0];
				if ((parts[1].length() > 0) && !parts[1].endsWith(")")) {
					System.err.println("Missing closing parenthesis on '"+id+"'");
					parts[1] += ")";
				}
				Arguments args = new Arguments(
						parts[1].length() > 0 ? parts[1].substring(0,
								parts[1].length() - 1) : null);

				System.err.println("Creating " + className + " with args ("
						+ args + ")");

				// FIXME handle generically
				if (id.startsWith("MeasurementView")) {
					newCode = true;
					RootPanel.get(id).add(
							new MeasurementView(clientFactory, args));
				} else if (id.startsWith("AudioView")) {
					newCode = true;
					RootPanel.get(id).add(new AudioView(clientFactory, args));
				} else if (id.startsWith("CameraView")) {
					newCode = true;
					RootPanel.get(id).add(new CameraView(clientFactory, args));
				} else if (id.startsWith("PtuView")) {
					newCode = true;
					RootPanel.get(id).add(new PtuView(clientFactory, args));
				} else if (id.startsWith("EventView")) {
					newCode = true;
					RootPanel.get(id).add(new EventView(clientFactory, args));
				} else if (id.startsWith("ProcedureView")) {
					newCode = true;
					RootPanel.get(id).add(
							new ProcedureView(clientFactory, args));
				} else if (id.startsWith("ProcedureControls")) {
					newCode = true;
					RootPanel.get(id).add(
							new ProcedureControls(clientFactory, args));
				} else if (id.startsWith("PlaceView")) {
					newCode = true;
					RootPanel.get(id).add(new PlaceView(clientFactory, args));
				} else if (id.startsWith("PtuTabSelector")) {
					newCode = true;
					RootPanel.get(id).add(
							new PtuTabSelector(clientFactory, args));
				} else if (id.startsWith("PtuSettingsView")) {
					newCode = true;
					RootPanel.get(id).add(
							new PtuSettingsView(clientFactory, args));
				} else if (id.startsWith("ServerSettingsView")) {
					newCode = true;
					RootPanel.get(id).add(
							new ServerSettingsView(clientFactory, args));
				} else if (id.startsWith("DosimeterView")) {
					newCode = true;
					RootPanel.get(id).add(
							new DosimeterView(clientFactory, args));
				} else if (id.startsWith("TimeView")) {
					newCode = true;
					RootPanel.get(id).add(new TimeView(clientFactory, args));
				}
			}
		}

		// FIXME create tab buttons for each, select default one
		String defaultPtuId = "PTU1234";
		clientFactory.getEventBus("local").fireEvent(new SelectPtuEvent(defaultPtuId));
		clientFactory.getEventBus("private").fireEvent(
				new SelectPtuEvent(defaultPtuId));
		clientFactory.getEventBus("private2").fireEvent(
				new SelectPtuEvent(defaultPtuId));

		if (newCode)
			return;

		startWorker();
		return;
	}

	private void startWorker() {

		// MGWTColorScheme.setBaseColor("#56a60D");
		// MGWTColorScheme.setFontColor("#eee");
		//
		// MGWTStyle.setDefaultBundle((MGWTClientBundle)
		// GWT.create(MGWTStandardBundle.class));
		// MGWTStyle.getDefaultClientBundle().getMainCss().ensureInjected();

		ViewPort viewPort = new MGWTSettings.ViewPort();
		viewPort.setTargetDensity(DENSITY.MEDIUM);
		viewPort.setUserScaleAble(false).setMinimumScale(1.0)
				.setMinimumScale(1.0).setMaximumScale(1.0);

		MGWTSettings settings = new MGWTSettings();
		settings.setViewPort(viewPort);
		// settings.setIconUrl("logo.png");
		// settings.setAddGlosToIcon(true);
		settings.setFullscreen(true);
		settings.setPreventScrolling(true);

		MGWT.applySettings(settings);

		final ClientFactory clientFactory = new APVSClientFactory();

		// Start PlaceHistoryHandler with our PlaceHistoryMapper
		TabletPlaceHistoryMapper historyMapper = GWT
				.create(TabletPlaceHistoryMapper.class);

		if (MGWT.getOsDetection().isTablet()) {

			// very nasty workaround because GWT does not corretly support
			// @media
			StyleInjector.inject(AppBundle.INSTANCE.css().getText());

			createTabletDisplay(clientFactory);
		} else {

			createTabletDisplay(clientFactory);
			// createPhoneDisplay(clientFactory);

		}

		TabletHistoryObserver historyObserver = new TabletHistoryObserver();

		MGWTPlaceHistoryHandler historyHandler = new MGWTPlaceHistoryHandler(
				historyMapper, historyObserver);

		historyHandler.register(clientFactory.getPlaceController(),
				clientFactory.getRemoteEventBus(), new HomePlace());
		historyHandler.handleCurrentHistory();
	}

	/*
	 * private void createPhoneDisplay(ClientFactory clientFactory) {
	 * AnimatableDisplay display = GWT.create(AnimatableDisplay.class);
	 * 
	 * PhoneActivityMapper appActivityMapper = new PhoneActivityMapper(
	 * clientFactory);
	 * 
	 * PhoneAnimationMapper appAnimationMapper = new PhoneAnimationMapper();
	 * 
	 * AnimatingActivityManager activityManager = new AnimatingActivityManager(
	 * appActivityMapper, appAnimationMapper, clientFactory.getEventBus());
	 * 
	 * activityManager.setDisplay(display);
	 * 
	 * RootPanel.get().add(display);
	 * 
	 * }
	 */
	private void createTabletDisplay(ClientFactory clientFactory) {
		SimplePanel navContainer = new SimplePanel();
		navContainer.getElement().setId("nav");
		navContainer.getElement().addClassName("landscapeonly");
		AnimatableDisplay navDisplay = GWT.create(AnimatableDisplay.class);

		final TabletPortraitOverlay tabletPortraitOverlay = new TabletPortraitOverlay();

		new OrientationRegionHandler(navContainer, tabletPortraitOverlay,
				navDisplay);
		new MasterRegionHandler(clientFactory.getRemoteEventBus(), "nav",
				tabletPortraitOverlay);

		ActivityMapper navActivityMapper = new TabletMenuActivityMapper(
				clientFactory);

		AnimationMapper navAnimationMapper = new TabletMenuAnimationMapper();

		AnimatingActivityManager navActivityManager = new AnimatingActivityManager(
				navActivityMapper, navAnimationMapper,
				clientFactory.getRemoteEventBus());

		navActivityManager.setDisplay(navDisplay);

		RootPanel.get().add(navContainer);

		SimplePanel mainContainer = new SimplePanel();
		mainContainer.getElement().setId("main");
		AnimatableDisplay mainDisplay = GWT.create(AnimatableDisplay.class);

		TabletPanelActivityMapper tabletMainActivityMapper = new TabletPanelActivityMapper(
				clientFactory);

		AnimationMapper tabletMainAnimationMapper = new TabletPanelAnimationMapper();

		AnimatingActivityManager mainActivityManager = new AnimatingActivityManager(
				tabletMainActivityMapper, tabletMainAnimationMapper,
				clientFactory.getRemoteEventBus());

		mainActivityManager.setDisplay(mainDisplay);
		mainContainer.setWidget(mainDisplay);

		RootPanel.get().add(mainContainer);

	}
}
