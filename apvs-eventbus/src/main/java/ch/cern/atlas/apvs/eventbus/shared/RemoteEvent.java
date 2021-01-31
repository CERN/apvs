package ch.cern.atlas.apvs.eventbus.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

// NOTE: implements IsSerializable in case serialization file cannot be found
public abstract class RemoteEvent<H> implements Serializable, IsSerializable {

	private static final long serialVersionUID = -7670083062245182625L;
	
	public static class Type<H> {
		private final int uuid;

		public Type() {
			uuid = UUID.uuidInt(8);
		}
		
		public int getUUID() {
			return uuid;
		}

		@Override
		public final int hashCode() {
			return (int)uuid;
		}

		@Override
		public String toString() {
			return "RemoteEvent type of class "+getClass();
		}
	}
	
	private String sourceClassName;
	private Integer uuid;
	private Long eventBusUUID;

	protected RemoteEvent() {
	}

	protected RemoteEvent(Object src) {
		sourceClassName = src.getClass().getName();
	}

	public abstract Type<H> getAssociatedType();

	public Integer getSourceUUID() {
		return uuid;
	}

	public Long getEventBusUUID() {
		return eventBusUUID;
	}
	
	public String toDebugString() {
		String name = this.getClass().getName();
		name = name.substring(name.lastIndexOf(".") + 1);
		return "event: " + name + ":";
	}
	
	public String getSourceClassName() {
		return sourceClassName;
	}

	@Override
	public String toString() {
		return "Remote event of class "+getClass()+" from "+getSourceClassName();
	}

	protected abstract void dispatch(H handler);

	protected void setSourceUUID(int uuid) {
		// only set when it was not set before. Forwarded events keep their original source.
		if (this.uuid == null) {
			this.uuid = uuid;
		}
	}

	protected void setEventBusUUID(long eventBusUUID) {
		// only set when it was not set before. Forwarded events do not get their UUID re-set.
		if (this.eventBusUUID == null) {
			this.eventBusUUID = eventBusUUID;
		}
	}
}
