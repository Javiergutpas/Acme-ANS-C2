
package acme.features.manager.flight;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.airline.Airline;
import acme.entities.flight.Flight;
import acme.realms.manager.AirlineManager;

@GuiService
public class AirlineManagerFlightCreateService extends AbstractGuiService<AirlineManager, Flight> {
	// Internal state ---------------------------------------------------------

	@Autowired
	private AirlineManagerFlightRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		String method;
		boolean status;

		method = super.getRequest().getMethod();

		//POST Hacking de atributos de navegación
		if (method.equals("GET"))
			status = true;
		else {
			String airlineId;
			Airline airline;

			airlineId = super.getRequest().getData("airline", String.class);
			//Si se introduce por hackeo un string no convertible a integer dará un binding exception(number-format)
			airline = this.repository.findAirlineById(Integer.valueOf(airlineId));

			status = airlineId == "0" || airline != null;
		}
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Flight flight;
		AirlineManager manager = (AirlineManager) super.getRequest().getPrincipal().getActiveRealm();

		flight = new Flight();
		flight.setManager(manager);
		flight.setPublish(false);

		super.getBuffer().addData(flight);
	}

	@Override
	public void bind(final Flight flight) {
		super.bindObject(flight, "tag", "cost", "description", "airline");
	}

	@Override
	public void validate(final Flight flight) {
		;
	}

	@Override
	public void perform(final Flight flight) {
		this.repository.save(flight);
	}

	@Override
	public void unbind(final Flight flight) {
		Dataset dataset;
		SelectChoices choicesAirline;
		Collection<Airline> airlines;

		airlines = this.repository.findAllAirlines();
		choicesAirline = SelectChoices.from(airlines, "iataCode", flight.getAirline());

		dataset = super.unbindObject(flight, "tag", "requiresSelfTransfer", "cost", "description");
		dataset.put("airlines", choicesAirline);

		super.getResponse().addData(dataset);
	}
}
