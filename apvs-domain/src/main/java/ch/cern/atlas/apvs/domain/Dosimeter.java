package ch.cern.atlas.apvs.domain;

import java.io.Serializable;
import java.util.Date;

public class Dosimeter implements Serializable, Comparable<Dosimeter> {

	private static final long serialVersionUID = -9183933693411766044L;

	private int serialNo;
	private double dose;
	private double rate;
	private Date date;

	public Dosimeter() {
	}

	public Dosimeter(int serialNo, double dose, double rate, Date date) {
		this.serialNo = serialNo;
		this.dose = dose;
		this.rate = rate;
		this.date = date;
	}

	public int getSerialNo() {
		return serialNo;
	}

	public double getDose() {
		return dose;
	}

	public double getRate() {
		return rate;
	}
	
	public Date getDate() {
		return date;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(getSerialNo()).hashCode()
				+ Double.valueOf(getDose()).hashCode()
				+ Double.valueOf(getRate()).hashCode()
				+ date.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Dosimeter) {
			Dosimeter d = (Dosimeter) obj;
			return (getSerialNo() == d.getSerialNo())
					&& (getDose() == d.getDose()) && (getRate() == d.getRate()) && (getDate() == d.getDate());
		}
		return super.equals(obj);
	}

	public String toString() {
		return "Dosimeter (" + getSerialNo() + "): dose=" + getDose()
				+ "; rate=" + getRate() + "; date=" + getDate();
	}

	@Override
	public int compareTo(Dosimeter o) {
		if (this == o) {
			return 0;
		}

		return (o != null) ? getSerialNo() < o.getSerialNo() ? -1
				: getSerialNo() == o.getSerialNo() ? 0 : 1 : 1;
	}
}
