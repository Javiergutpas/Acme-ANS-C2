
package acme.forms;

import acme.client.components.basis.AbstractForm;
import acme.client.components.datatypes.Money;
import acme.entities.airport.Airport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirlineManagerDashboard extends AbstractForm {

	// Serialisation version --------------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	Integer						rankingByExperience;
	Integer						yearsToRetire; //Retirement at 65
	Double						ratioOfOnTimeLegs;
	Double						ratioOfDelayedLegs;
	Airport						mostPopularAirport;
	Airport						leastPopularAirport;
	Integer						numberOfOnTimeLegs;
	Integer						numberOfDelayedLegs;
	Integer						numberOfLandedLegs;
	Integer						numberOfCancelledLegs;
	Money						averageFlightCost;
	Money						minFlightCost;
	Money						maxFlightCost;
	Money						flightCostStandardDeviation;

	// Derived attributes -----------------------------------------------------

	// Relationships ----------------------------------------------------------
}
