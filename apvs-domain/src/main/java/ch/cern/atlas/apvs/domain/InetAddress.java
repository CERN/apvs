package ch.cern.atlas.apvs.domain;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gwt.user.client.rpc.IsSerializable;

public class InetAddress implements Serializable, IsSerializable {

	private static final long serialVersionUID = -8179909394365726414L;
	private static final InetAddress localHost = new InetAddress(new byte[] {127, 0, 0, 1});
	private byte[] address;

	protected InetAddress() {
		// serializable
	}
	
	private InetAddress(byte[] address) {
		this.address = address;
	}

	public static InetAddress getByName(String name) throws IllegalArgumentException {

		if (name == null || "localhost".equals(name)) {
			return localHost;
		}

		String[] parts = name.split("\\.");

		byte[] parsed = new byte[4];

		try {
			if (parts.length != 4) {
				throw new RuntimeException("4 parts expected");
			}
			for (int i = 0; i < 4; i++) {
				parsed[i] = (byte) Integer.parseInt(parts[i]);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("InetAddress parsing issue: " + e+" nnn.nnn.nnn.nnn expected; actual: '" + name + "'");
		}
		return new InetAddress(parsed);
	}
	
	public static InetAddress getByAddress(byte[] address) {
		return new InetAddress(address);
	}
	
	public static InetAddress getLocalHost() {
		return localHost;
	}

	public byte[] getAddress() {
		return address;
	}

	public String getHostAddress() {

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 4; i++) {
			if (i > 0) {
				sb.append('.');
			}
			sb.append(address[i] & 255);
		}
		return sb.toString();
	}

	public String toString() {
		return "/" + getHostAddress();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(address);
		return result;
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
		InetAddress other = (InetAddress) obj;
		if (!Arrays.equals(address, other.address)) {
			return false;
		}
		return true;
	}
}