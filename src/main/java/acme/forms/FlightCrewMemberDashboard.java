
package acme.forms;

import java.util.List;
import java.util.Map;

import acme.client.components.basis.AbstractForm;
import acme.entities.flightassignment.CurrentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightCrewMemberDashboard extends AbstractForm {

	// Serialisation version --------------------------------------------------
	private static final long			serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------
	private List<String>				lastFiveDestinations;

	private Integer						legsWithLowSeverityIncidents;  // from 0 to 3

	private Integer						legsWithMediumSeverityIncidents; // from 4 to 7

	private Integer						legsWithHighSeverityIncidents;  // from 8 to 10

	private List<String>				membersAssignedInLastLeg;

	private Map<CurrentStatus, Integer>	flightAssignmentsByStatus;

	private Double						averageFlightAssignmentInLastMonth;

	private Integer						minimumFlightAssignmentInLastMonth;

	private Integer						maximumFlightAssignmentInLastMonth;

	private Double						standardDeviationInLastMonth;
}
