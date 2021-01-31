package ch.cern.atlas.apvs.event;

import java.io.Serializable;

import ch.cern.atlas.apvs.domain.Ternary;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class ConnectionStatusChangedRemoteEvent extends
		RemoteEvent<ConnectionStatusChangedRemoteEvent.Handler> {

	private static final long serialVersionUID = -2682254554211541413L;

	// NOTE: implements IsSerializable in case serialization file cannot be
	// found
	public enum ConnectionType implements Serializable, IsSerializable {
		server("Server Status"), audio("Audio Status"), video("Video Status"), daq(
				"DAQ Status"), dosimeter("Dosimeter Status"), databaseConnect(
				"DB Connect Status"), databaseUpdate("DB Update Status");

		private final String s;

		ConnectionType(String s) {
			this.s = s;
		}

		public String getString() {
			return s;
		}
	}

	public interface Handler {
		/**
		 * Called when an event is fired.
		 * 
		 * @param event
		 *            an {@link MessageReceivedEvent} instance
		 */
		void onConnectionStatusChanged(ConnectionStatusChangedRemoteEvent event);
	}

	private static final Type<ConnectionStatusChangedRemoteEvent.Handler> TYPE = new Type<ConnectionStatusChangedRemoteEvent.Handler>();

	public static void fire(RemoteEventBus eventBus, ConnectionType type,
			Ternary status, String cause) {
		eventBus.fireEvent(new ConnectionStatusChangedRemoteEvent(type, status,
				cause));
	}

	public static void fire(RemoteEventBus eventBus, ConnectionType type,
			boolean ok, String cause) {
		eventBus.fireEvent(new ConnectionStatusChangedRemoteEvent(type,
				ok ? Ternary.True : Ternary.False, cause));
	}

	/**
	 * Register a handler for events on the eventbus.
	 * 
	 * @param eventBus
	 *            the {@link EventBus}
	 * @param handler
	 *            an Handler instance
	 * @return an {@link HandlerRegistration} instance
	 */
	public static HandlerRegistration register(RemoteEventBus eventBus,
			ConnectionStatusChangedRemoteEvent.Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	}

	public static HandlerRegistration subscribe(Object src,
			RemoteEventBus eventBus,
			ConnectionStatusChangedRemoteEvent.Handler handler) {
		HandlerRegistration registration = register(eventBus, handler);

		eventBus.fireEvent(new RequestRemoteEvent(src,
				ConnectionStatusChangedRemoteEvent.class));

		return registration;
	}

	private ConnectionType connection;
	private Ternary status;
	private String cause;

	public ConnectionStatusChangedRemoteEvent() {
	}

	public ConnectionStatusChangedRemoteEvent(ConnectionType connection,
			Ternary status, String cause) {
		this.connection = connection;
		this.status = status;
		this.cause = cause;
	}

	@Override
	public Type<ConnectionStatusChangedRemoteEvent.Handler> getAssociatedType() {
		return TYPE;
	}

	public ConnectionType getConnection() {
		return connection;
	}

	public Ternary getStatus() {
		return status;
	}

	public String getCause() {
		return cause;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onConnectionStatusChanged(this);
	}

}
