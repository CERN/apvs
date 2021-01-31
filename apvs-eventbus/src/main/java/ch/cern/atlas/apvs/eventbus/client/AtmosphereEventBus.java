package ch.cern.atlas.apvs.eventbus.client;

import java.util.Iterator;
import java.util.List;

import org.atmosphere.gwt20.client.Atmosphere;
import org.atmosphere.gwt20.client.AtmosphereCloseHandler;
import org.atmosphere.gwt20.client.AtmosphereMessageHandler;
import org.atmosphere.gwt20.client.AtmosphereOpenHandler;
import org.atmosphere.gwt20.client.AtmosphereReopenHandler;
import org.atmosphere.gwt20.client.AtmosphereRequest;
import org.atmosphere.gwt20.client.AtmosphereRequestConfig;
import org.atmosphere.gwt20.client.AtmosphereResponse;
import org.atmosphere.gwt20.client.GwtRpcClientSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.eventbus.shared.RemoteEvent;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.SerializationException;

public class AtmosphereEventBus extends RemoteEventBus {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private Atmosphere client;
	private final AtmosphereRequest request;

	// NOTE serializer can be null, but note the exception in #284, thrown in "compiled" mode
	public AtmosphereEventBus(GwtRpcClientSerializer serializer) {
		
		AtmosphereRequestConfig requestConfig = AtmosphereRequestConfig.create(serializer);
		requestConfig.setUrl(GWT.getModuleBaseURL() + "eventBusComet");
		requestConfig.setTransport(AtmosphereRequestConfig.Transport.STREAMING);
		requestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
		requestConfig.setOpenHandler(new AtmosphereOpenHandler() {
            @Override
            public void onOpen(AtmosphereResponse response) {
            }
        });
		requestConfig.setReopenHandler(new AtmosphereReopenHandler() {
            @Override
            public void onReopen(AtmosphereResponse response) {
            }
        });
		requestConfig.setCloseHandler(new AtmosphereCloseHandler() {
            @Override
            public void onClose(AtmosphereResponse response) {
            }
        });
		requestConfig.setMessageHandler(new AtmosphereMessageHandler() {
            @Override
            public void onMessage(AtmosphereResponse response) {
            	
            	List<Object> messages = response.getMessages();
            	
    			log.trace("EventBusListener Messages "+messages.size());
    			
    			for (Iterator<?> i = messages.iterator(); i.hasNext(); ) {
    				Object message = i.next();
    				if (message instanceof RemoteEvent<?>) {
    					RemoteEvent<?> event = (RemoteEvent<?>)message;
    					
    					// NOTE: also my own needs to be distributed locally
 						AtmosphereEventBus.super.fireEvent(event);
    				}
    			}               
            }
        });
		
	    request = client.subscribe(requestConfig);

		getServerEvent();
	}

	private void getServerEvent() {
		// FIXME do an async call, server implements suspend, and answers eventually when a remote event needs
		// to be sent.
	}

	/**
	 * broadcast event and (receive it locally to distribute, below)
	 * @throws SerializationException 
	 * 
	 */
	@Override
	public void fireEvent(RemoteEvent<?> event) {
		try {
			request.push(event);
		} catch (SerializationException e) {
			Window.alert(""+e);
		}
	}
	
	/**
	 * broadcast event and (receive it locally to distribute, below)
	 * FIXME source is ignored
	 * @throws SerializationException 
	 * 
	 */
	@Override
	public void fireEventFromSource(RemoteEvent<?> event, int uuid) {
		try {
			request.push(event);
		} catch (SerializationException e) {
			Window.alert(""+e);
		}
	}
	
	@Override
	public String toString() {
		return "AtmosphereEventBus";
	}
}
