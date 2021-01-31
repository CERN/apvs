package ch.cern.atlas.apvs.eventbus.client;

import java.util.Iterator;
import java.util.List;

import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class PollEventBus extends RemoteEventBus {

//	private Logger log = LoggerFactory.getLogger(getClass().getName());
	
	private final EventBusServiceAsync eventBusService;
	
	public PollEventBus() {
		this(null);
	}
	
	public PollEventBus(RpcRequestBuilder requestBuilder) {
		eventBusService = EventBusServiceAsync.Util.getInstance();
		((ServiceDefTarget)eventBusService).setRpcRequestBuilder(requestBuilder);
		getQueuedEvents();
	}

	/**
	 * broadcast event
	 * @throws SerializationException 
	 * 
	 */
	@Override
	public void fireEvent(RemoteEvent<?> event) {
		doFire(event);
	}

	/**
	 * broadcast event FIXME source is ignored
	 * @throws SerializationException 
	 */
	@Override
	public void fireEventFromSource(RemoteEvent<?> event, int uuid) {
		doFire(event);
	}

	private void doFire(final RemoteEvent<?> event) {
		// send out locally
		super.fireEvent(event);

		// send out remote
		eventBusService.fireEvent(event, new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
//				log.info("Client: Sent event..." + event);
			}

			@Override
			public void onFailure(Throwable caught) {
//				log.warn("Failed to send event " + event + " "
//						+ caught);
				caught.printStackTrace();
				if (caught.getCause() != null) {
//					log.warn("Caused by...");
					caught.getCause().printStackTrace();
				}
			}
		});
	}

	private void getQueuedEvents() {
		eventBusService.getQueuedEvents(getUUID(),
				new AsyncCallback<List<RemoteEvent<?>>>() {

					@Override
					public void onSuccess(List<RemoteEvent<?>> events) {
//					    log.info(getUUID()+": Received events..." + events.size());
					    
						// forward events locally
						for (Iterator<RemoteEvent<?>> i = events.iterator(); i
								.hasNext();) {

							RemoteEvent<?> event = i.next();
//						    log.info("Client: Received event..." + event);
							// do not fire your own events
							if (event.getEventBusUUID() != getUUID()) {
								PollEventBus.super.fireEvent(event);
							}
						}

						getQueuedEvents();
					}

					@Override
					public void onFailure(Throwable caught) {
//						log.warn("Failed to get next event " + caught);

						getQueuedEvents();
					}
				});
	}
	
	@Override
	public String toString() {
		return "PollEventBus";
	}
}
