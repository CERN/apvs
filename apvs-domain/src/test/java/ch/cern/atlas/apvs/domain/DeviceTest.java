package ch.cern.atlas.apvs.domain;

import java.util.List;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceTest extends AbstractDomainTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testBasicUsage() {
		Device d1 = new Device("PTU01", InetAddress.getByAddress(new byte[] {
				127, 0, 0, 1 }), "Description", new MacAddress(new byte[] {
				0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD }),
				"ptu01.cern.ch", false);
		Device d2 = new Device("PTU02", InetAddress.getByName("localhost"),
				"Some Desc", new MacAddress("23:45:67:89:AB:CD"),
				"ptu02.cern.ch", false);

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(d1);
		session.save(d2);
		session.getTransaction().commit();
		session.close();

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
		session.beginTransaction();
		List result = session.createCriteria(Device.class).list();
		Assert.assertEquals(2, result.size());
		log.info(""+result.get(0));
		Assert.assertEquals(d1, result.get(0));
		log.info(""+result.get(1));
		Assert.assertEquals(d2, result.get(1));
		session.getTransaction().commit();
		session.close();
	}

}
