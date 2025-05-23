
package acme.forms;

import java.util.List;

import acme.client.components.basis.AbstractForm;
import acme.entities.flightassignment.FlightAssignment;
import acme.realms.flightcrewmember.FlightCrewMember;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlightCrewMemberDashboard extends AbstractForm {

	// Serialisation version --------------------------------------------------
	private static final long		serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------
	private List<String>			lastFiveDestinations;

	private Integer					legsWithLowSeverityIncidents;  // from 0 to 3

	private Integer					legsWithMediumSeverityIncidents; // from 4 to 7

	private Integer					legsWithHighSeverityIncidents;  // from 8 to 10

	private List<FlightCrewMember>	membersAssignedInLastLeg;

	private List<FlightAssignment>	confirmedFlightAssignments;

	private List<FlightAssignment>	pendingFlightAssignments;

	private List<FlightAssignment>	cancelledFlightAssignments;

	private Double					averageFlightAssignmentInLastMonth;

	private Integer					minimumFlightAssignmentInLastMonth;

	private Integer					maximumFlightAssignmentInLastMonth;

	private Double					standardDeviationInLastMonth;
}
