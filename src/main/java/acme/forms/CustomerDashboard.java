
package acme.forms;

import java.util.Collection;

import acme.client.components.basis.AbstractForm;
import acme.client.components.datatypes.Money;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDashboard extends AbstractForm {

	// Serialisation version --------------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	Collection<String>			lastFiveDestinations;
	Collection<Money>			spentBookingsMoney;
	long						economyBookings;
	long						businessBookings;
	Collection<Money>			bookingsTotalCost;
	Collection<Money>			bookingsAverageCost;
	Collection<Money>			bookingsMinimumCost;
	Collection<Money>			bookingsMaximumCost;
	Collection<Money>			bookingsDeviationCost;
	long						bookingsTotalPassengers;
	Double						bookingsAveragePassengers;
	long						bookingsMinimumPassengers;
	long						bookingsMaximumPassengers;
	Double						bookingsDeviationPassengers;
}
