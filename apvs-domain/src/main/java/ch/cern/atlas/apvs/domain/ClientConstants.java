package ch.cern.atlas.apvs.domain;

import com.google.gwt.i18n.client.DateTimeFormat;

public interface ClientConstants {
//	public TimeZoneConstants timeZoneConstants = GWT.create(TimeZoneConstants.class);
//	public static final TimeZone timeZone = TimeZone.createTimeZone(timeZoneConstants.europeZurich());
DateTimeFormat simpleDateFormat = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm:ss");
	DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd MMM yyyy HH:mm:ss");
	DateTimeFormat dateFormatNoSeconds = DateTimeFormat.getFormat("dd MMM yyyy HH:mm");
	DateTimeFormat dateFormatShort = DateTimeFormat.getFormat("dd MMM HH:mm");
	DateTimeFormat dateFormatOnly = DateTimeFormat.getFormat("dd-MM-yy");
	DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss");
	DateTimeFormat timeFormatNoSeconds = DateTimeFormat.getFormat("HH:mm");
}
