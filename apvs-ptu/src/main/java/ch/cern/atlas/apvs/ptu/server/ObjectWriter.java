package ch.cern.atlas.apvs.ptu.server;

import java.io.IOException;

import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Ptu;

public interface ObjectWriter {

	void write(Object obj) throws IOException;
	
	void write(Ptu ptu) throws IOException;

	void write(Message message) throws IOException;

	void flush() throws IOException;

	void close() throws IOException;

	void newLine() throws IOException;
}
