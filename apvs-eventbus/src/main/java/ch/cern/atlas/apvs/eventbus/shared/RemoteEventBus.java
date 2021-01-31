package ch.cern.atlas.apvs.eventbus.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.UmbrellaException;

/**
 * EventBus that handles normal Events (local) and RemoteEvents
 * 
 * @author Mark Donszelmann
 */
public class RemoteEventBus extends SimpleEventBus {

//	private Logger log = LoggerFactory.getLogger(getClass().getName());

	private final static boolean DEBUG = false;

	protected static <H> void dispatchEvent(RemoteEvent<H> event, H handler) {
		event.dispatch(handler);
	}

	protected static void setSourceUuidOfEvent(RemoteEvent<?> event, int uuid) {
		event.setSourceUUID(uuid);
	}

	protected static void setEventBusUuidOfEvent(RemoteEvent<?> event,
			long eventBusUUID) {
		event.setEventBusUUID(eventBusUUID);
	}

	private interface Command {
		void execute();
	}

	private final Long uuid = UUID.uuidLong(8);

	private int firingDepth;

	private List<Command> deferredDeltas;

	private final Map<RemoteEvent.Type<?>, Map<Integer, List<?>>> map = new HashMap<RemoteEvent.Type<?>, Map<Integer, List<?>>>();

	public RemoteEventBus() {
	}

	public Long getUUID() {
		return uuid;
	}

	public <H> HandlerRegistration addHandler(RemoteEvent.Type<H> type,
			H handler) {
		return doAdd(type, 0, handler);
	}

	public <H> HandlerRegistration addHandlerToSource(
			final RemoteEvent.Type<H> type, int uuid, final H handler) {
		if (uuid == 0) {
			throw new NullPointerException("Cannot add a handler with a 0 uuid");
		}

		return doAdd(type, uuid, handler);
	}

	public void fireEvent(RemoteEvent<?> event) {
		if (DEBUG) {
//			log.info("Fire event " + event.getClass());
		}
		doFire(event, 0);
	}

	public void fireEventFromSource(RemoteEvent<?> event, int uuid) {
		if (uuid == 0) {
			throw new NullPointerException("Cannot fire from a 0 uuid");
		}
		if (DEBUG) {
//			log.info("Fire event " + event.getClass() + " " + uuid);
		}
		doFire(event, uuid);
	}

	private void defer(Command command) {
		if (deferredDeltas == null) {
			deferredDeltas = new ArrayList<Command>();
		}
		deferredDeltas.add(command);
	}

	private <H> HandlerRegistration doAdd(final RemoteEvent.Type<H> type,
			final int uuid, final H handler) {
		if (type == null) {
			throw new NullPointerException(
					"Cannot add a handler with a null type");
		}
		if (handler == null) {
			throw new NullPointerException("Cannot add a null handler");
		}

		if (firingDepth > 0) {
			enqueueAdd(type, uuid, handler);
		} else {
			doAddNow(type, uuid, handler);
		}

		return new HandlerRegistration() {
			public void removeHandler() {
				if (firingDepth > 0) {
					enqueueRemove(type, uuid, handler);
				} else {
					doRemoveNow(type, uuid, handler);
				}
			}
		};
	}

	private <H> void doAddNow(RemoteEvent.Type<H> type, int uuid, H handler) {
		List<H> l = ensureHandlerList(type, uuid);
		l.add(handler);
	}

	private <H> void doFire(RemoteEvent<H> event, int uuid) {
		if (event == null) {
			throw new NullPointerException("Cannot fire null event");
		}
		try {
			firingDepth++;

			if (uuid != 0) {
				setSourceUuidOfEvent(event, uuid);
			}

			setEventBusUuidOfEvent(event, getUUID());

			List<H> handlers = getDispatchList(event.getAssociatedType(), uuid);
			Set<Throwable> causes = null;

			ListIterator<H> it = handlers.listIterator();
			while (it.hasNext()) {
				H handler = it.next();

				try {
					dispatchEvent(event, handler);
				} catch (Throwable e) {
					if (causes == null) {
						causes = new HashSet<Throwable>();
					}
					causes.add(e);
				}
			}

			if (causes != null) {
				throw new UmbrellaException(causes);
			}
		} finally {
			firingDepth--;
			if (firingDepth == 0) {
				handleQueuedAddsAndRemoves();
			}
		}
	}

	private <H> void doRemoveNow(RemoteEvent.Type<H> type, int uuid, H handler) {
		List<H> l = getHandlerList(type, uuid);

		boolean removed = l.remove(handler);
		assert removed : "redundant remove call";
		if (removed && l.isEmpty()) {
			prune(type, uuid);
		}
	}

	private <H> void enqueueAdd(final RemoteEvent.Type<H> type, final int uuid,
			final H handler) {
		defer(new Command() {
			public void execute() {
				doAddNow(type, uuid, handler);
			}
		});
	}

	private <H> void enqueueRemove(final RemoteEvent.Type<H> type,
			final int uuid, final H handler) {
		defer(new Command() {
			public void execute() {
				doRemoveNow(type, uuid, handler);
			}
		});
	}

	private <H> List<H> ensureHandlerList(RemoteEvent.Type<H> type, int uuid) {
		Map<Integer, List<?>> sourceMap = map.get(type);
		if (sourceMap == null) {
			sourceMap = new HashMap<Integer, List<?>>();
			map.put(type, sourceMap);
		}

		// safe, we control the puts.
		@SuppressWarnings("unchecked")
		List<H> handlers = (List<H>) sourceMap.get(uuid);
		if (handlers == null) {
			handlers = new ArrayList<H>();
			sourceMap.put(uuid, handlers);
		}

		return handlers;
	}

	private <H> List<H> getDispatchList(RemoteEvent.Type<H> type, int uuid) {
		List<H> directHandlers = getHandlerList(type, uuid);
		if (uuid == 0) {
			return directHandlers;
		}

		List<H> globalHandlers = getHandlerList(type, 0);

		List<H> rtn = new ArrayList<H>(directHandlers);
		rtn.addAll(globalHandlers);
		return rtn;
	}

	private <H> List<H> getHandlerList(RemoteEvent.Type<H> type, int uuid) {
		Map<Integer, List<?>> sourceMap = map.get(type);
		if (sourceMap == null) {
			return Collections.emptyList();
		}

		// safe, we control the puts.
		@SuppressWarnings("unchecked")
		List<H> handlers = (List<H>) sourceMap.get(uuid);
		if (handlers == null) {
			return Collections.emptyList();
		}

		return handlers;
	}

	private void handleQueuedAddsAndRemoves() {
		if (deferredDeltas != null) {
			try {
				for (Command c : deferredDeltas) {
					c.execute();
				}
			} finally {
				deferredDeltas = null;
			}
		}
	}

	private void prune(RemoteEvent.Type<?> type, int uuid) {
		Map<Integer, List<?>> sourceMap = map.get(type);

		List<?> pruned = sourceMap.remove(uuid);

		assert pruned != null : "Can't prune what wasn't there";
		assert pruned.isEmpty() : "Pruned unempty list!";

		if (sourceMap.isEmpty()) {
			map.remove(type);
		}
	}
	
	@Override
	public String toString() {
		return "RemoteEventBus";
	}
}
