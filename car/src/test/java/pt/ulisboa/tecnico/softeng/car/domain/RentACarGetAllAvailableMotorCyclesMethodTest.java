package pt.ulisboa.tecnico.softeng.car.domain;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;


import pt.ulisboa.tecnico.softeng.car.exception.CarException;

public class RentACarGetAllAvailableMotorCyclesMethodTest {
	private static final String PLATE1 = "XX-XX-XX";
	private static final String PLATE2 = "YY-YY-YY";
	private static final String PLATE3 = "WW-WW-WW";
	private static final String PLATE4 = "PP-PP-PP";
	private static final String PLATE5 = "AA-AA-AA";
	private static final String PLATE6 = "ZZ-ZZ-ZZ";
	private static final int KM = 100;
	private static final String DRIVING_LICENSE = "VC1";
	private RentACar _rentCar = new RentACar("Benecar", "01");


	@Test
	public void success() {
		Vehicle m1 = new Motorcycle(PLATE1, KM, this._rentCar);
		Vehicle m2 = new Motorcycle(PLATE2, KM, this._rentCar);
		Vehicle m3 = new Motorcycle(PLATE3, KM, this._rentCar);
		Vehicle m4 = new Motorcycle(PLATE4, KM, this._rentCar);
		Vehicle m5 = new Motorcycle(PLATE5, KM, this._rentCar);
		Vehicle c1 = new Car(PLATE6, KM, this._rentCar);

		List<Vehicle> availableMotors = new ArrayList<>();

		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,1) ,  new LocalDate(2014, 1, 2));
		availableMotors.add(m1);
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,3) ,  new LocalDate(2014, 1, 4));
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,3) ,  new LocalDate(2014, 1, 5));
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,4) ,  new LocalDate(2014, 1, 5));
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,5) ,  new LocalDate(2014, 1, 6));
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,9) ,  new LocalDate(2014, 1,10));
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,9) ,  new LocalDate(2014, 1,11));
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,10),  new LocalDate(2014, 1,11));
		m1.rent(DRIVING_LICENSE, new LocalDate(2014,1,11),  new LocalDate(2014, 1,12));
		m2.rent(DRIVING_LICENSE, new LocalDate(2014,1,1) ,  new LocalDate(2014, 1, 2));
		availableMotors.add(m2);
		m3.rent(DRIVING_LICENSE, new LocalDate(2014,1,11),  new LocalDate(2014, 1, 11));
		availableMotors.add(m3);
		m4.rent(DRIVING_LICENSE, new LocalDate(2014,1,4) ,  new LocalDate(2014, 1, 4));
		m5.rent(DRIVING_LICENSE, new LocalDate(2014,1,10),  new LocalDate(2014, 1, 10));
		c1.rent(DRIVING_LICENSE, new LocalDate(2014,1,1) ,  new LocalDate(2014, 1, 2));

		Assert.assertEquals(availableMotors, this._rentCar.getAllAvailableMotorCycles(new LocalDate(2014,1,4), new LocalDate(2014, 1, 10)));
	}

	@Test(expected = CarException.class)
	public void nullBeginDate() {
		this._rentCar.getAllAvailableMotorCycles(null, new LocalDate(2014,1,8));
	}

	@Test(expected = CarException.class)
	public void nullEndDate() {
		this._rentCar.getAllAvailableMotorCycles(new LocalDate(2014,1,8), null);
	}
	@Test(expected = CarException.class)
	public void invalidPeriod() {
		this._rentCar.getAllAvailableMotorCycles(new LocalDate(2014,1,8), new LocalDate(2014,1,7));
	}

}