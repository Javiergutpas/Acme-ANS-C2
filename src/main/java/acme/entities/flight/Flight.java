
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
	public Date getScheduledDeparture() {
		Date departure;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		departure = repository.findFlightScheduledDeparture(this.getId());

		return departure;
	}

	@Transient
	public Date getScheduledArrival() {
		Date departure;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		departure = repository.findFlightScheduledArrival(this.getId());

		return departure;
	}

	@Transient
	public String getOriginCity() {
		String city;
		List<String> cities;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);

		cities = repository.findFlightOriginCity(this.getId(), repository.findFlightScheduledDeparture(this.getId()));

		city = cities.isEmpty() ? null : cities.get(0);

		return city;
	}

	@Transient
	public String getDestinationCity() {
		String city;
		List<String> cities;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);

		cities = repository.findFlightDestinationCity(this.getId(), repository.findFlightScheduledArrival(this.getId()));

		city = cities.isEmpty() ? null : cities.get(0);

		return city;
	}

	@Transient
	public Integer getNumberOfLayovers() {
		Integer legs;
		FlightRepository repository;

		repository = SpringHelper.getBean(FlightRepository.class);
		legs = repository.findNumberOfLegs(this.getId());

		return legs > 0 ? legs - 1 : 0; //Si el vuelo no tiene tramos, hay 0 escalas
	}

	//Esta propiedad derivada es para los seleccionables de vuelos del estudiante 2 (customers)
	@Transient
	public String getFlightLabel() {
		String label;

		label = "";

		if (this.getDestinationCity() != null && this.getOriginCity() != null) {
			label = "" + this.getOriginCity() + " - " + this.getDestinationCity();
			return label;
		}

		return label;
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
