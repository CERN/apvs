package ch.cern.atlas.apvs.eventbus.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;

public class ServerEventBus extends RemoteEventBus {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private static ServerEventBus instance;

	private final static boolean DEBUG = false;

	private EventBusServiceHandler eventBusServiceHandler;

	public static ServerEventBus getInstance() {
		if (instance == null) {
			instance = new ServerEventBus();
		}
		return instance;
	}

	private ServerEventBus() {
	}

	public void setEventBusServiceHandler(
			EventBusServiceHandler eventBusServiceHandler) {
		this.eventBusServiceHandler = eventBusServiceHandler;
	}

	@Override
	public void fireEvent(RemoteEvent<?> event) {
		if (DEBUG) {
			log.info("Fire event " + event.getClass());
		}
		doFire(event);
	}

	@Override
	public void fireEventFromSource(RemoteEvent<?> event, int uuid) {
		if (DEBUG) {
			log.info("Fire event " + event.getClass() + " " + uuid);
		}
		doFire(event);
	}

	private void doFire(RemoteEvent<?> event) {
		// send out locally
		super.fireEvent(event);

		// send to remote
		if (eventBusServiceHandler != null) {
			eventBusServiceHandler.forwardEvent(event);
		}
	}

	public void forwardEvent(RemoteEvent<?> event) {
		// Only forward events that are not from us
		if (event.getEventBusUUID() != getUUID()) {
			super.fireEvent(event);
		}
	}
	
	@Override
	public String toString() {
		return "ServerEventBus";
	}
}
