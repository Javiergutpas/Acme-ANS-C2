
package acme.entities.flight;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.Valid;

import acme.client.components.basis.AbstractEntity;
import acme.client.components.datatypes.Money;
import acme.client.components.mappings.Automapped;
import acme.client.components.validation.Mandatory;
import acme.client.components.validation.Optional;
import acme.client.components.validation.ValidMoney;
import acme.client.components.validation.ValidString;
import acme.client.helpers.SpringHelper;
import acme.entities.airline.Airline;
import acme.entities.leg.Leg;
import acme.realms.manager.AirlineManager;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Flight extends AbstractEntity {

	// Serialisation version --------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------

	@Mandatory
	@ValidString(min = 1, max = 50)
	@Automapped
	private String				tag;

	@Mandatory
	//@Valid
	@Automapped
	private boolean				requiresSelfTransfer;

	@Mandatory
	@ValidMoney //By default -> (min = 0.00, max = 1000000.00)
	@Automapped
	private Money				cost;

	@Optional
	@ValidString
	@Automapped
	private String				description;

	@Mandatory
	//@Valid
	@Automapped
	private boolean				publish;

	//Derived attributes-------------------------------------------------


	@Transient
	public Date getScheduledDeparture() { // No need to check min and max values, since it's already done in legs
		Date departure;
		List<Leg> legs;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		legs = repository.getLegsByFlightOrderedByDeparture(this.getId());

		departure = legs.isEmpty() ? null : legs.get(0).getDeparture();

		return departure;
	}

	@Transient
	public Date getScheduledArrival() {
		Date arrival;
		List<Leg> legs;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		legs = repository.getLegsByFlightOrderedByArrival(this.getId());

		arrival = legs.isEmpty() ? null : legs.get(legs.size() - 1).getArrival();

		return arrival;
	}

	@Transient
	public String getOriginCity() {
		String city;
		List<Leg> legs;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		legs = repository.getLegsByFlightOrderedByDeparture(this.getId());

		city = legs.isEmpty() ? null : legs.get(0).getDepartureAirport().getCity();

		return city;
	}

	@Transient
	public String getDestinationCity() {
		String city;
		List<Leg> legs;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		legs = repository.getLegsByFlightOrderedByArrival(this.getId());

		city = legs.isEmpty() ? null : legs.get(legs.size() - 1).getArrivalAirport().getCity();

		return city;
	}

	@Transient
	public Integer getNumberOfLayovers() {
		Integer legs;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		legs = repository.getNumberOfLegs(this.getId());

		return legs > 0 ? legs - 1 : 0; //Si el vuelo no tiene tramos, hay 0 escalas
	}

	//Esta propiedad derivada es para los seleccionables de vuelos del estudiante 2 (customers)
	@Transient
	public String getFlightLabel() {
		String label;

		if (this.getDestinationCity() != null && this.getOriginCity() != null) {
			label = "" + this.getOriginCity() + " - " + this.getDestinationCity();
			return label;
		} else //Cuando un vuelo está publicado las ciudades nunca serán null, pero puede pasar si no lo estuviera
			return null;
	}

	// Relationships -----------------------------------------------------


	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private AirlineManager	manager;

	@Mandatory
	@Valid
	@ManyToOne(optional = false)
	private Airline			airline;
}
