package ch.cern.atlas.apvs.db;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Device;

public class Devices extends UpdatedEntity {
	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final static int DELAY = 10;
	private final static TimeUnit UNIT = TimeUnit.SECONDS;

	private SortedMap<String, Device> devices = new TreeMap<String, Device>();

	public Devices(Database database) {
		super(database, DELAY, UNIT);
		update();
	}

	public Device get(String name) {
		return devices.get(name);
	}

	public Device put(Device device) {
		return (device != null) ? devices.put(device.getName(), device) : null;
	}

	public List<Device> values() {
		return new ArrayList<Device>(devices.values());
	}

	public int size() {
		return devices.size();
	}

	@Override
	public int hashCode() {
		return devices.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Devices other = (Devices) obj;
		if (devices == null) {
			if (other.devices != null) {
				return false;
			}
		} else if (!devices.entrySet().equals(other.devices.entrySet())) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	protected void update() {
		log.info("Updating Devices");
		SortedMap<String, Device> update = new TreeMap<String, Device>();

		Session session = null;
		Transaction tx = null;
		try {
			session = getDatabase().getSessionFactory().openSession();
			tx = session.beginTransaction();

			for (Device device : (List<Device>) session.createQuery(
					"from Device d order by d.name").list()) {
				update.put(device.getName(), device);
			}
			log.info("Found " + update.size() + " devices");

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
		}

		if (!update.equals(devices)) {
			log.info("NEW... Devices");
			devices = update;
		}
	}

}
