
package acme.features.customer.passenger;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.passenger.Passenger;
import acme.realms.customer.Customer;

@GuiService
public class CustomerPassengerListService extends AbstractGuiService<Customer, Passenger> {

	//Internal state --------------------------------------------

	@Autowired
	private CustomerPassengerRepository repository;

	//AbstractGuiService interface -------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);

	}

	@Override
	public void load() {
		Collection<Passenger> passengers = new ArrayList<>();
		int customerId;

		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		passengers = this.repository.findAllPassengersByCustomerId(customerId);

		super.getBuffer().addData(passengers);
		super.getResponse().addGlobal("showCreate", true);
	}

	@Override
	public void unbind(final Passenger passenger) {
		Dataset dataset;
		dataset = super.unbindObject(passenger, "fullName", "passportNumber", "dateOfBirth", "publish");

		super.getResponse().addData(dataset);
	}

}
