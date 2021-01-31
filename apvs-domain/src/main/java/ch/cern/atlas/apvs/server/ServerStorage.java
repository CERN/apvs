package ch.cern.atlas.apvs.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.sync.ReadWriteSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStorage {
	private static final String APVS_SERVER_SETTINGS_FILE = "APVS.properties";
	private static final String comment = "APVS Server Settings";
	private static final Logger log = LoggerFactory.getLogger(ServerStorage.class
			.getName());

	private static ServerStorage instance;

	private PropertiesConfiguration config;
	private final File configFile;
	private boolean readOnly;

	private ServerStorage() throws FileNotFoundException, IOException {
		try {
			configFile = new File(APVS_SERVER_SETTINGS_FILE);
			if (!configFile.exists()) {
				log.error("File " + APVS_SERVER_SETTINGS_FILE
						+ " not found, copy the example 'cp APVS-example.properties "+APVS_SERVER_SETTINGS_FILE+"' and edit the entries...");
				System.exit(1);
			}
			config = new PropertiesConfiguration();
			config.setSynchronizer(new ReadWriteSynchronizer());
			FileReader reader = new FileReader(configFile);
			config.read(reader);
			reader.close();
			
			try {
				FileWriter writer = new FileWriter(configFile);
				config.setHeader(comment + "\n" + (new Date()).toString());
				config.write(writer);
				writer.close();
				log.info("Configuration file is read/write");
			} catch (ConfigurationException e) {
				log.warn("Configuration file is READ ONLY");
				config = new PropertiesConfiguration();
				readOnly = true;
			}
		} catch (ConfigurationException e) {
			log.warn("Configuration File Problem", e);
		}
	}

	public static ServerStorage getLocalStorageIfSupported() {
		try {
			if (instance == null) {
				instance = new ServerStorage();
			}
			return instance;
		} catch (IOException e) {
			log.warn("Server Settings Storage problem", e);
		}
		return null;
	}

	public String getString(String name) {
		return config.getString(name);
	}
	
	public String getString(String name, String defaultValue) {
		return config.getString(name, defaultValue);
	}
	
	public boolean getBoolean(String name) {
		return config.getBoolean(name, false);
	}

	public int getInt(String name) {
		return config.getInt(name);
	}

	public int getInt(String name, int defaultValue) {
		return config.getInt(name, defaultValue);
	}

	public void setItem(String name, Object value) {
		config.setProperty(name, value);

		// FIXME-654
		if (readOnly) {
			return;
		}

		try {
			FileWriter writer = new FileWriter(configFile);
			config.write(writer);
			writer.close();
		} catch (ConfigurationException e) {
			log.error("Cannot store settings");
			readOnly = true;
		} catch (IOException e) {
			log.error("Cannot store settings");
			readOnly = true;
		}
	}

	public Set<String> getKeys(final String prefix) {
		Set<String> keys = new HashSet<String>();
		
		for (Iterator<String> i = config.getKeys(prefix); i.hasNext(); ) {
			String key = i.next().substring(prefix.length()+1);
			int dot = key.indexOf('.');
			if (dot >= 0) {
				key = key.substring(0, dot);
			}
			keys.add(key);
		}
		
		return keys;
	}
}
