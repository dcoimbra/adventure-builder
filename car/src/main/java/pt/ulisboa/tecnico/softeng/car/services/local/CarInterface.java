package pt.ulisboa.tecnico.softeng.car.services.local;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;
import pt.ulisboa.tecnico.softeng.car.domain.*;
import pt.ulisboa.tecnico.softeng.car.exception.CarException;
import pt.ulisboa.tecnico.softeng.car.services.local.dataobjects.RentACarData;
import pt.ulisboa.tecnico.softeng.car.services.local.dataobjects.RentingData;
import pt.ulisboa.tecnico.softeng.car.services.local.dataobjects.VehicleData;

public class CarInterface {

	public static enum Type {
		CAR, MOTORCYCLE
	}

	@Atomic(mode = TxMode.READ)
	public static List<RentACarData> getRentACars() {
		return FenixFramework.getDomainRoot().getRentACarSet().stream().map(r -> new RentACarData(r))
				.collect(Collectors.toList());
	}

	@Atomic(mode = TxMode.WRITE)
	public static void createRentACar(RentACarData rentacarData) {
		new RentACar(rentacarData.getName(), rentacarData.getNif(), rentacarData.getIban());
	}

	@Atomic(mode = TxMode.READ)
	public static RentACarData getRentACarDataByCode(String code) {
		RentACar rentacar = getRentACarByCode(code);

		if (rentacar != null) {
			return new RentACarData(rentacar);
		}

		return null;
	}
	
	
	@Atomic(mode = TxMode.WRITE)
	public static void createCar(String rentacarCode, VehicleData vehicleData) {
		new Car(vehicleData.getPlate(), vehicleData.getKilometers(), vehicleData.getPrice(), getRentACarByCode(rentacarCode));
				
	}
	
	
	@Atomic(mode = TxMode.WRITE)
	public static void createMotorcycle(String rentacarCode, VehicleData vehicleData) {
		new Motorcycle(vehicleData.getPlate(), vehicleData.getKilometers(), vehicleData.getPrice(), getRentACarByCode(rentacarCode));
				
	}
	
	@Atomic(mode = TxMode.READ)
	public static VehicleData getVehicleDataByPlate(String code, String plate) {
		Vehicle vehicle = getVehicleByPlate(code, plate);
		if (vehicle == null) {
			return null;
		}

		return new VehicleData(vehicle);
	}


	
	private static Vehicle getVehicleByPlate(String code, String plate) {
		RentACar rentacar = getRentACarByCode(code);
		if (rentacar == null) {
			return null;
		}

		Vehicle vehicle = rentacar.getVehicleByPlate(plate);
		if (vehicle == null) {
			return null;
		}
		return vehicle;
	}

	
	private static RentACar getRentACarByCode(String code) {
		return FenixFramework.getDomainRoot().getRentACarSet().stream().filter(r -> r.getCode().equals(code)).findFirst()
				.orElse(null);
	}

	@Atomic(mode = TxMode.WRITE)
	public static void createRenting(String code, String plate, RentingData renting) {
		Vehicle vehicle = getVehicleByPlate(code, plate);
		if (vehicle == null) {
			throw new CarException();
		}

		new Renting(renting.getDrivingLicense(), renting.getBegin(), renting.getEnd(), vehicle, renting.getBuyerNif(), renting.getBuyerIban());
	}

	@Atomic(mode = TxMode.WRITE)
	public static String cancelRenting(String reference) {

		Renting renting = RentACar.getRenting(reference);
			if (renting != null) {
				return renting.cancel();
			}
		throw new CarException();
	}

	@Atomic(mode = TxMode.WRITE)
	public static String rentCar(Type vehicleType, String drivingLicense, String nif, String iban, LocalDate begin,
								 LocalDate end) {

		if (vehicleType == Type.CAR) {

			return RentACar.rent(Car.class, drivingLicense, nif, iban, begin, end);
		}

		else {

			return RentACar.rent(Motorcycle.class, drivingLicense, nif, iban, begin, end);
		}
	}

	@Atomic(mode = TxMode.WRITE)
	public static void checkoutRenting(String code, String plate, String reference, int kilometers) {

		Vehicle vehicle = getVehicleByPlate(code, plate);

		if (vehicle == null) {
			throw new CarException();
		}

		Renting renting = vehicle.getRenting(reference);

		renting.checkout(kilometers);

		renting.delete();
	}

	@Atomic(mode = TxMode.READ)
	public static RentingData getRentingData(String reference) {
		for (RentACar rentACar : FenixFramework.getDomainRoot().getRentACarSet()) {
			for (Vehicle vehicle : rentACar.getVehicleSet()) {
				Renting renting = vehicle.getRenting(reference);
				if (renting != null) {
					return new RentingData(renting);
				}
			}
		}
		throw new CarException();
	}
}
