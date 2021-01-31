package ch.cern.atlas.apvs.db;

import java.util.concurrent.TimeUnit;

public abstract class UpdatedEntity {

	private final Database database;

	public UpdatedEntity(Database database, int delay, TimeUnit unit) {
		this.database = database;
		
//		if (false) {
//			ScheduledExecutorService executor = Executors
//					.newSingleThreadScheduledExecutor();
//			executor.scheduleWithFixedDelay(new Runnable() {
//				@Override
//				public void run() {
//					update();
//				}
//			}, delay, delay, unit);
//		}
	}

	protected Database getDatabase() {
		return database;
	}

	protected abstract void update();

}
