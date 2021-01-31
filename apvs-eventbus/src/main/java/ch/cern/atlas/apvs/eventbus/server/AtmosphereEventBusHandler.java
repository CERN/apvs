package ch.cern.atlas.apvs.eventbus.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.gwt20.shared.Constants;
import org.atmosphere.handler.ReflectorServletProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.eventbus.shared.ConnectionUUIDsChangedEvent;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;

/**
 * @author Mark Donszelmann
 */
public class AtmosphereEventBusHandler extends ReflectorServletProcessor {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final static boolean DEBUG = true;

	private RemoteEventBus eventBus;

	private ArrayList<String> uuids;

	public void init(ServletConfig servletCopnfig) throws ServletException {
	    log.info("AtmosphereEventBusHandler started...");
		java.util.logging.Logger.getLogger("").setLevel(Level.INFO);
		java.util.logging.Logger.getLogger("org.atmosphere.gwt").setLevel(Level.ALL);
		java.util.logging.Logger.getLogger("ch.cern.atlas.apvs").setLevel(Level.ALL);
		java.util.logging.Logger.getLogger("").getHandlers()[0]
				.setLevel(Level.ALL);
		log.trace("Updated logging levels");

		eventBus = ServerEventBus.getInstance();
		uuids = new ArrayList<String>();

		RequestRemoteEvent.register(this, eventBus, new RequestRemoteEvent.Handler() {

			@Override
			public void onRequestEvent(RequestRemoteEvent event) {
				String type = event.getRequestedClassName();

				if (type.equals(ConnectionUUIDsChangedEvent.class.getName())) {
					ConnectionUUIDsChangedEvent.fire(this, eventBus, uuids, null,
							null);
				}
			}
		});
	}
	
    @Override
    public void onRequest(AtmosphereResource ar) throws IOException {
      if (ar.getRequest().getMethod().equals("GET") ) {
        doGet(ar);
      } else if (ar.getRequest().getMethod().equals("POST") ) {
        doPost(ar);
      }
    }
    
	@Override
	public void onStateChange(AtmosphereResourceEvent event) throws IOException {
		super.onStateChange(event);
		if (DEBUG) {
			log.info("AtmosphereEventBusHandler.Commet stateChanged "
					+ event.getMessage());
			log.info(event.getResource().uuid());
		}
	}
    
    private void doGet(AtmosphereResource resource) {
        
		if (DEBUG) {
//			log.info("AtmosphereEventBusHandler.doComet()..."
//					+ resource.getConnectionUUID());
//			log.info(resource.getAtmosphereResource()
//					.getAtmosphereResourceEvent().toString());
		}
//		String uuid = resource.getConnectionUUID();
//		if (uuid != null) {
//			uuids.add(uuid);
//			ConnectionUUIDsChangedEvent.fire(eventBus, uuids, uuid, null);
//		}

		resource.getBroadcaster().setID("GWT_COMET");
		HttpSession session = resource.getRequest()
				.getSession(false);
		if (session != null) {
			log.debug("Got session with id: " + session.getId());
			log.debug("Time attribute: " + session.getAttribute("time"));
		} else {
			log.warn("No session");
		}
		if (log.isDebugEnabled()) {
			log.debug("Url: "
					+ resource.getRequest()
							.getRequestURL()
					+ "?"
					+ resource.getRequest()
							.getQueryString());
		}

		String agent = resource.getRequest().getHeader("user-agent");
        log.info(agent);

        // gwt20
        resource.suspend();
    }
    
    /**
     * receive push message from client
     **/
    private void doPost(AtmosphereResource resource) {
        Object msg = resource.getRequest().getAttribute(Constants.MESSAGE_OBJECT);
        if (msg != null) {
          log.info("received RPC post: " + msg.toString());
        }    
    }
    	
    @Override
    public void destroy() {
		if (DEBUG) {
//			log.info("AtmosphereEventBusHandler.Comet terminated "
//					+ cometResponse.getConnectionUUID());
		}
			
		if (DEBUG) {
//			log.info("AtmosphereEventBusHandler.Comet disconnected "
//					+ resource.getConnectionUUID());
//			log.info(resource.getAtmosphereResource()
//					.getAtmosphereResourceEvent().toString());
		}
		
//		String uuid = resource.getConnectionUUID();
//		if (uuid != null) {
//			uuids.remove(uuid);
//			ConnectionUUIDsChangedEvent.fire(eventBus, uuids, null, uuid);
//		}
	}	
}
