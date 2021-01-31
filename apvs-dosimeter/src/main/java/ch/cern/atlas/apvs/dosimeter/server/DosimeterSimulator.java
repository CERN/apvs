package ch.cern.atlas.apvs.dosimeter.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Dosimeter;

public class DosimeterSimulator extends Thread {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());
	
	private final Channel channel;
	private final Random random = new Random();
	private final int noOfDosimeters = 5;

	public DosimeterSimulator(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void run() {
		try {
			List<Dosimeter> dosimeters = new ArrayList<Dosimeter>(
					noOfDosimeters);
			String[] serialNo = { "265", "4738", "202", "106", "395" };
			Date now = new Date();
			for (int i = 0; i < noOfDosimeters; i++) {
				dosimeters.add(new Dosimeter(serialNo[i],
						random.nextDouble() * 500.0, random.nextDouble() * 5.0,
						now));
				log.info(dosimeters.get(i).getSerialNo());
			}

			ChannelBuffer buffer = ChannelBuffers.buffer(8192);
			ChannelBufferOutputStream cos = new ChannelBufferOutputStream(
					buffer);

			BufferedWriter os = new BufferedWriter(new OutputStreamWriter(cos));

			try {
				while (!isInterrupted()) {
					for (int i = 0; i < dosimeters.size(); i++) {
						Dosimeter dosimeter = dosimeters.get(i);
						os.write(DosimeterCoder.encode(dosimeter));
						os.newLine();
						dosimeters.set(i, next(dosimeter));
					}
					os.flush();

					channel.write(cos.buffer()).awaitUninterruptibly();
					cos.buffer().clear();

					Thread.sleep(10000 + random.nextInt(5000));
				}
			} catch (InterruptedException e) {
				// ignored
			}
			os.close();
		} catch (IOException e) {
		} finally {
			channel.close();
		}
	}

	private Dosimeter next(Dosimeter dosimeter) {
		double newDose = dosimeter.getDose() + dosimeter.getRate();
		double newRate = Math.max(0.0,
				dosimeter.getRate() + random.nextGaussian() * 0.5);
		return new Dosimeter(dosimeter.getSerialNo(), newDose, newRate,
				new Date());
	}
}
