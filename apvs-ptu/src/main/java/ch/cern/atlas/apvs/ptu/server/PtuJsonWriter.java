package ch.cern.atlas.apvs.ptu.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;

import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Ptu;

import com.cedarsoftware.util.io.JsonWriter;

public class PtuJsonWriter extends JsonWriter implements ObjectWriter {

	public PtuJsonWriter(OutputStream out) throws IOException {
		super(out);

		addWriter(Date.class, new JsonClassWriter() {

			@Override
			public void writePrimitiveForm(Object o, Writer out)
					throws IOException {
				out.write("\"" + PtuServerConstants.dateFormat.format((Date) o)
						+ "\"");
			}

			@Override
			public void write(Object o, boolean showType, Writer out)
					throws IOException {
			}

			@Override
			public boolean hasPrimitiveForm() {
				return true;
			}
		});

		addWriter(String.class, new JsonClassWriter() {

			@Override
			public void writePrimitiveForm(Object o, Writer out)
					throws IOException {
				out.write("\"" + (String) o + "\"");
			}

			@Override
			public void write(Object o, boolean showType, Writer out)
					throws IOException {
			}

			@Override
			public boolean hasPrimitiveForm() {
				return true;
			}
		});

	}

	@Override
	protected void writePrimitive(Object obj) throws IOException {
		if (obj instanceof Character) {
			writeJsonUtf8String(String.valueOf(obj), _out);
		} else if (obj instanceof Boolean) {
			_out.write("\""
					+ (((Boolean) obj).booleanValue() ? "True" : "False")
					+ "\"");
		} else {
			_out.write("\"" + obj.toString() + "\"");
		}
	}

	@Override
	protected void writeFieldName(String name, Writer out) throws IOException {
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

		switch (name) {
		case "Date":
			name = "Time";
			break;
		case "DosimeterId":
			name = "DosimeterID";
			break;
		case "Bssid":
			name = "bssid";
			break;
		default:
			break;
		}

		super.writeFieldName(name, out);
	}

	@Override
	public void newLine() throws IOException {
		// out.append("\n");
	}

	@Override
	protected void writeImpl(Object obj, boolean showType) throws IOException {
		super.writeImpl(obj, false);
	}

	@Override
	public void write(Object obj) throws IOException {
		// do not trace refs
		writeImpl(obj, false);
		flush();
	}

	// FIXME could be optimized with multi messages in one header
	@Override
	public void write(Ptu ptu) throws IOException {
		for (Iterator<String> i = ptu.getMeasurementNames().iterator(); i
				.hasNext();) {
			String name = i.next();
			write(ptu.getMeasurement(name));
			newLine();
		}
	}

	@Override
	public void write(Message message) throws IOException {
		writeImpl(new JsonHeader(message), false);
	}

	public static String toJson(Object item) {
		try {
			return objectToJson(item);
		} catch (IOException ignored) {
			return null;
		}
	}

	public static String objectToJson(Object item) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PtuJsonWriter writer = new PtuJsonWriter(stream);
		writer.write(item);
		writer.close();
		return new String(stream.toByteArray(), StandardCharsets.UTF_8);
	}
}
