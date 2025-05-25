
package acme.features.manager.leg;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.airport.Airport;
import acme.entities.leg.Leg;
import acme.entities.leg.LegStatus;
import acme.realms.manager.AirlineManager;

@GuiService
public class AirlineManagerLegUpdateService extends AbstractGuiService<AirlineManager, Leg> {
	// Internal state ---------------------------------------------------------

	@Autowired
	private AirlineManagerLegRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		Leg leg;
		int legId;
		int managerId;
		boolean status;

		legId = super.getRequest().getData("id", int.class);
		leg = this.repository.findLegById(legId);
		managerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		status = leg != null && !leg.isPublish() && leg.getFlight().getManager().getId() == managerId;

		if (status) {
			String method;

			method = super.getRequest().getMethod();

			if (method.equals("GET"))
				status = true;
			else {
				String legStatus;
				boolean correctStatus;
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

				legStatus = super.getRequest().getData("status", String.class);
				correctStatus = "0".equals(legStatus) || Arrays.stream(LegStatus.values()).map(LegStatus::name).anyMatch(name -> name.equals(legStatus));

				status = (aircraftId == 0 || aircraft != null) && (departureAirportId == 0 || departureAirport != null) && (arrivalAirportId == 0 || arrivalAirport != null) && correctStatus;
			}
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Leg leg;
		int id;

		id = super.getRequest().getData("id", int.class);
		leg = this.repository.findLegById(id);

		super.getBuffer().addData(leg);
	}

	@Override
	public void bind(final Leg leg) {
		super.bindObject(leg, "flightNumber", "departure", "arrival", "status", "departureAirport", "arrivalAirport", "deployedAircraft");
	}

	@Override
	public void validate(final Leg leg) {
		boolean legIsFuture;

		if (leg.getDeparture() != null) {
			legIsFuture = MomentHelper.isPresentOrFuture(leg.getDeparture());
			super.state(legIsFuture, "departure", "acme.validation.leg.past-departure.message");
		} else if (leg.getArrival() != null) {
			legIsFuture = MomentHelper.isPresentOrFuture(leg.getArrival());
			super.state(legIsFuture, "arrival", "acme.validation.leg.past-arrival.message");
		}
	}

	@Override
	public void perform(final Leg leg) {
		assert leg != null;

		this.repository.save(leg);
	}

	@Override
	public void unbind(final Leg leg) {
		SelectChoices choicesStatuses;
		SelectChoices choicesAircrafts;
		SelectChoices choicesDepartureAirport;
		SelectChoices choicesArrivalAirport;
		Collection<Aircraft> aircrafts;
		Collection<Airport> airports;
		Dataset dataset;

		choicesStatuses = SelectChoices.from(LegStatus.class, leg.getStatus());
		aircrafts = this.repository.findAllAircrafts();
		choicesAircrafts = SelectChoices.from(aircrafts, "registrationNumber", leg.getDeployedAircraft());

		airports = this.repository.findAllAirports();
		choicesDepartureAirport = SelectChoices.from(airports, "iataCode", leg.getDepartureAirport());
		choicesArrivalAirport = SelectChoices.from(airports, "iataCode", leg.getArrivalAirport());

		dataset = super.unbindObject(leg, "flightNumber", "departure", "arrival", "publish");
		dataset.put("flightId", leg.getFlight().getId());
		dataset.put("durationInHours", leg.getDurationInHours());
		dataset.put("statuses", choicesStatuses);
		dataset.put("departureAirports", choicesDepartureAirport);
		dataset.put("arrivalAirports", choicesArrivalAirport);
		dataset.put("aircrafts", choicesAircrafts);

		super.getResponse().addData(dataset);
	}
}
