package ch.cern.atlas.apvs.domain;

import java.util.List;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTest extends AbstractDomainTest {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testBasicUsage() {
		User u1 = new User("Mark", "Donszelmann", "yyyyy");
		User u2 = new User("Olga", "Beltramello", "xxxxx");

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(u1);
		session.save(u2);
		session.getTransaction().commit();
		session.close();

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
		session.beginTransaction();
		List result = session.createCriteria(User.class).list();
		Assert.assertEquals(2, result.size());
		log.info(""+result.get(0));
		Assert.assertEquals(u1, result.get(0));
		log.info(""+result.get(1));
		Assert.assertEquals(u2, result.get(1));
		session.getTransaction().commit();
		session.close();
	}

}
