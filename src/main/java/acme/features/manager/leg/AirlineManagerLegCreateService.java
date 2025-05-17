
package acme.features.manager.leg;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.airport.Airport;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.entities.leg.LegStatus;
import acme.realms.manager.AirlineManager;

@GuiService
public class AirlineManagerLegCreateService extends AbstractGuiService<AirlineManager, Leg> {
	// Internal state ---------------------------------------------------------

	@Autowired
	private AirlineManagerLegRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		Flight flight;
		int flightId;
		int managerId;
		boolean status;

		flightId = super.getRequest().getData("flightId", int.class);
		flight = this.repository.findFlightById(flightId);
		managerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		status = flight != null && !flight.isPublish() && flight.getManager().getId() == managerId;

		if (status) { //POST Hacking
			String method;

			method = super.getRequest().getMethod();

			if (method.equals("GET"))
				status = true;
			else {
				int aircraftId;
				int departureAirportId;
				int arrivalAirportId;

				Aircraft aircraft;
				Airport departureAirport;
				Airport arrivalAirport;

				aircraftId = super.getRequest().getData("deployedAircraft", int.class);
				departureAirportId = super.getRequest().getData("departureAirport", int.class);
				arrivalAirportId = super.getRequest().getData("arrivalAirport", int.class);

				aircraft = this.repository.findAircraftById(aircraftId);
				departureAirport = this.repository.findAirportById(departureAirportId);
				arrivalAirport = this.repository.findAirportById(arrivalAirportId);

				status = (aircraftId == 0 || aircraft != null) && (departureAirportId == 0 || departureAirport != null) && (arrivalAirportId == 0 || arrivalAirport != null);
			}

		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Leg leg;
		int flightId;
		Flight flight;

		flightId = super.getRequest().getData("flightId", int.class);
		flight = this.repository.findFlightById(flightId);

		leg = new Leg();
		leg.setFlight(flight);
		leg.setPublish(false);

		super.getBuffer().addData(leg);
	}

	@Override
	public void bind(final Leg leg) {
		LegStatus status;

		status = super.getRequest().getData("status", LegStatus.class);

		super.bindObject(leg, "flightNumber", "departure", "arrival", "status", "departureAirport", "arrivalAirport", "deployedAircraft");
		leg.setStatus(status);
	}

	@Override
	public void validate(final Leg leg) {
		boolean legIsFuture;

		if (leg.getDeparture() != null) {
			legIsFuture = MomentHelper.isPresentOrFuture(leg.getDeparture());
			super.state(legIsFuture, "departure", "acme.validation.leg.past-departure.message");
		}
		if (leg.getArrival() != null) {
			legIsFuture = MomentHelper.isPresentOrFuture(leg.getArrival());
			super.state(legIsFuture, "arrival", "acme.validation.leg.past-arrival.message");
		}
	}

	@Override
	public void perform(final Leg leg) {
		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		SelectChoices choicesStatus;
		SelectChoices choicesAircrafts;
		SelectChoices choicesDepartureAirport;
		SelectChoices choicesArrivalAirport;
		Collection<Aircraft> aircrafts;
		Collection<Airport> airports;
		int flightId;

		Dataset dataset;

		choicesStatus = SelectChoices.from(LegStatus.class, leg.getStatus());

		aircrafts = this.repository.findAllAircrafts();
		choicesAircrafts = SelectChoices.from(aircrafts, "registrationNumber", leg.getDeployedAircraft());

		airports = this.repository.findAllAirports();
		choicesDepartureAirport = SelectChoices.from(airports, "iataCode", leg.getDepartureAirport());
		choicesArrivalAirport = SelectChoices.from(airports, "iataCode", leg.getArrivalAirport());

		flightId = super.getRequest().getData("flightId", int.class);

		dataset = super.unbindObject(leg, "flightNumber", "departure", "arrival");
		dataset.put("statuses", choicesStatus);
		dataset.put("departureAirports", choicesDepartureAirport);
		dataset.put("arrivalAirports", choicesArrivalAirport);
		dataset.put("aircrafts", choicesAircrafts);
		dataset.put("flightId", flightId);

		super.getResponse().addData(dataset);
	}
}
