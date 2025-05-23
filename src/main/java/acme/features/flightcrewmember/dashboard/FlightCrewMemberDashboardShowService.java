
package acme.features.flightcrewmember.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flightassignment.CurrentStatus;
import acme.entities.flightassignment.FlightAssignment;
import acme.forms.FlightCrewMemberDashboard;
import acme.realms.flightcrewmember.FlightCrewMember;

@GuiService
public class FlightCrewMemberDashboardShowService extends AbstractGuiService<FlightCrewMember, FlightCrewMemberDashboard> {

	//Internal state ---------------------------------------------

	@Autowired
	private FlightCrewMemberDashboardRepository repository;

	//AbstractGuiService interface -------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {

		FlightCrewMemberDashboard dashboard = new FlightCrewMemberDashboard();

		List<String> lastFiveDestinations;
		Integer legsWithLowSeverityIncidents;
		Integer legsWithMediumSeverityIncidents;
		Integer legsWithHighSeverityIncidents;
		List<FlightCrewMember> membersAssignedInLastLeg;
		List<FlightAssignment> confirmedFlightAssignments;
		List<FlightAssignment> pendingFlightAssignments;
		List<FlightAssignment> cancelledFlightAssignments;
		Double averageFlightAssignmentInLastMonth;
		Integer minimumFlightAssignmentInLastMonth;
		Integer maximumFlightAssignmentInLastMonth;
		Double standardDeviationInLastMonth;
		int flightCrewMemberId;

		flightCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		lastFiveDestinations = this.repository.findLastFiveDestinations(flightCrewMemberId);
		if (lastFiveDestinations.isEmpty())
			lastFiveDestinations = new ArrayList<>();
		else if (lastFiveDestinations.size() >= 5)
			lastFiveDestinations = lastFiveDestinations.subList(0, 5);

		legsWithLowSeverityIncidents = this.repository.findLegsWithLowSeverityIncident();
		legsWithMediumSeverityIncidents = this.repository.findLegsWithMediumSeverityIncident();
		legsWithHighSeverityIncidents = this.repository.findLegsWithHighSeverityIncident();

		membersAssignedInLastLeg = this.repository.findCrewMembersInMemberLastLeg(flightCrewMemberId);

		confirmedFlightAssignments = this.repository.findFlightAssignmentsByCrewMember(flightCrewMemberId, CurrentStatus.CONFIRMED);
		pendingFlightAssignments = this.repository.findFlightAssignmentsByCrewMember(flightCrewMemberId, CurrentStatus.CANCELLED);
		cancelledFlightAssignments = this.repository.findFlightAssignmentsByCrewMember(flightCrewMemberId, CurrentStatus.PENDING);

		Date startDate = MomentHelper.getCurrentMoment();
		Date endDate = MomentHelper.getCurrentMoment();

		startDate.setMonth(startDate.getMonth() - 1);
		startDate.setDate(1);
		endDate.setMonth(startDate.getMonth() - 1);
		endDate.setDate(30);

		dashboard.setLastFiveDestinations(lastFiveDestinations);
		dashboard.setLegsWithLowSeverityIncidents(legsWithLowSeverityIncidents);
		dashboard.setLegsWithMediumSeverityIncidents(legsWithMediumSeverityIncidents);
		dashboard.setLegsWithHighSeverityIncidents(legsWithHighSeverityIncidents);
		dashboard.setMembersAssignedInLastLeg(membersAssignedInLastLeg);
		dashboard.setConfirmedFlightAssignments(confirmedFlightAssignments);
		dashboard.setPendingFlightAssignments(pendingFlightAssignments);
		dashboard.setCancelledFlightAssignments(cancelledFlightAssignments);

		super.getBuffer().addData(dashboard);

	}

	@Override
	public void unbind(final FlightCrewMemberDashboard fcmDashboard) {

		Dataset dataset;
		dataset = super.unbindObject(fcmDashboard, "lastFiveDestinations", "legsWithLowSeverityIncidents", "legsWithMediumSeverityIncidents", "legsWithHighSeverityIncidents", "membersAssignedInLastLeg", "confirmedFlightAssignments",
			"pendingFlightAssignments", "cancelledFlightAssignments", "averageFlightAssignmentInLastMonth", "minimumFlightAssignmentInLastMonth", "maximumFlightAssignmentInLastMonth", "standardDeviationInLastMonth");

		super.getResponse().addData(dataset);

	}
}
