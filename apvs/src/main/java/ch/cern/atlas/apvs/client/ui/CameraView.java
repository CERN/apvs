package ch.cern.atlas.apvs.client.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.client.ClientFactory;
import ch.cern.atlas.apvs.client.event.PtuSettingsChangedRemoteEvent;
import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
import ch.cern.atlas.apvs.client.event.SwitchWidgetEvent;
import ch.cern.atlas.apvs.client.settings.PtuSettings;
import ch.cern.atlas.apvs.client.widget.IsSwitchableWidget;
import ch.cern.atlas.apvs.client.widget.UpdateScheduler;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;

public class CameraView extends ImageView implements Module,
		IsSwitchableWidget {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	public static final String HELMET = "Helmet";
	public static final String HAND = "Hand";

	/**
	 * Test Stream:
	 * 
	 * There is a properly setup test stream here:
	 * 
	 * http://www.wowzamedia.com/mobile.html
	 * 
	 * the direct RTSP URL is:
	 * 
	 * rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov
	 * 
	 * The test H.264 video on demand file is here:
	 * 
	 * http://www.wowzamedia.com/_h264/BigBuckBunny_175k.mov
	 */

	// FIXME
	// private final String cameraURL = "rtsp://pcatlaswpss02:8554/worker1";
	// private final String cameraURL =
	// "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
	// private final String cameraURL =
	// "http://devimages.apple.com/iphone/samples/bipbop/gear4/prog_index.m3u8";
	// private final String cameraURL =
	// "http://quicktime.tc.columbia.edu/users/lrf10/movies/sixties.mov";
	// private final String cameraURL =
	// "rtsp://quicktime.tc.columbia.edu:554/users/lrf10/movies/sixties.mov";

	private String type;

	private String ptuId;

	private PtuSettings settings;

	private EventBus switchBus;
	private EventBus cmdBus;
	private Element element;

	private String options;
	private boolean switchSource;
	private boolean switchDestination;

	private UpdateScheduler scheduler = new UpdateScheduler(this);

	// FIXME #179 needs to change when we redo the iPad version
	public CameraView(ClientFactory factory, final String type, String width,
			String height) {

		cmdBus = factory.getEventBus("ptu");
		this.type = type;

		init(factory, width, height);
	}

	public CameraView() {
	}

	@Override
	public boolean configure(final Element element,
			ClientFactory clientFactory, Arguments args) {

		this.element = element;
		switchBus = clientFactory.getEventBus("switch");

		cmdBus = clientFactory.getEventBus(args.getArg(0));
		type = args.getArg(1);
		options = args.getArg(2);

		switchSource = options.contains("SwitchSource");
		switchDestination = options.contains("SwitchDestination");

		SwitchWidgetEvent.register(switchBus, new SwitchWidgetEventHandler(
				switchBus, element, this));

		init(clientFactory, "100%", "100%");

		return true;
	}

	private void init(ClientFactory factory, String width, String height) {

		init(width, height);
		
		if (switchSource || switchDestination) {
			image.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					log.info("Single Click Switch");

					String title = element.getParentElement().getChild(1)
							.getChild(0).getNodeValue();

					log.info("Switch Widget: " + title);
					SwitchWidgetEvent.fire(switchBus, title, CameraView.this,
							false);
				}
			});
		}

		PtuSettingsChangedRemoteEvent.subscribe(factory.getRemoteEventBus(),
				new PtuSettingsChangedRemoteEvent.Handler() {

					@Override
					public void onPtuSettingsChanged(
							PtuSettingsChangedRemoteEvent event) {

						settings = event.getPtuSettings();

						scheduler.update();
					}
				});

		SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {

			@Override
			public void onPtuSelected(SelectPtuEvent event) {
				ptuId = event.getPtuId();

				scheduler.update();
			}
		});
	}

	@Override
	public boolean isDestination() {
		return switchDestination;
	}

	@Override
	public void toggleDestination() {
		switchDestination = !switchDestination;
	}

	private String getCameraUrl(String type, String ptuId) {
		if ((settings == null) || (ptuId == null)) {
			return null;
		}

		return settings.getCameraUrl(ptuId, type);
	}

	public boolean update() {
		final String cameraUrl = getCameraUrl(type, ptuId);
//		setUrl(null);
//		Window.alert("CameraURL: '"+ptuId+"' 'null'");
		
		// fix for #465
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
//			@Override
//			public void execute() {
				setUrl(cameraUrl);
//				Window.alert("CameraURL: '"+ptuId+"' '"+cameraUrl+"'");
//			}
//		});
		
			
		return false;
	}
}
