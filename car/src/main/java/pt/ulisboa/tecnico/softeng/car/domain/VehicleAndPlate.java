package pt.ulisboa.tecnico.softeng.car.domain;

import org.joda.time.LocalDate;

public class VehicleAndPlate extends VehicleAndPlate_Base {
    
    public VehicleAndPlate(RentACar rentACar, String plate) {
    	setRentACar(rentACar);
    	setPlate(plate);
    }
    
    public void delete() {
		setRentACar(null);
		getVehicle().delete();
		deleteDomainObject();
	}
    
}
