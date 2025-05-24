
package acme.features.flightcrewmember.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		List<FlightAssignment> flightAssignments;
		List<FlightCrewMember> membersAssignedInLastLeg;
		List<String> membersInLastLeg;

		List<Object[]> flightAssignmentCurrentStatus;
		Map<CurrentStatus, Integer> flightAssignmentsByStatus;

		Double averageFlightAssignmentInLastMonth;
		Integer minimumFlightAssignmentInLastMonth;
		Integer maximumFlightAssignmentInLastMonth;
		Double standardDeviationInLastMonth;

		int flightCrewMemberId;

		flightCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		lastFiveDestinations = this.repository.findLastFiveDestinations(flightCrewMemberId);
		dashboard.setLastFiveDestinations(lastFiveDestinations);

		legsWithLowSeverityIncidents = this.repository.findLegsWithSeverityIncident(0, 3, flightCrewMemberId);
		legsWithMediumSeverityIncidents = this.repository.findLegsWithSeverityIncident(4, 7, flightCrewMemberId);
		legsWithHighSeverityIncidents = this.repository.findLegsWithSeverityIncident(8, 7, flightCrewMemberId);
		dashboard.setLegsWithLowSeverityIncidents(legsWithLowSeverityIncidents);
		dashboard.setLegsWithMediumSeverityIncidents(legsWithMediumSeverityIncidents);
		dashboard.setLegsWithHighSeverityIncidents(legsWithHighSeverityIncidents);

		flightAssignments = this.repository.findFlightAssignmentByLegArrival(flightCrewMemberId);
		membersInLastLeg = new ArrayList<>();

		if (!flightAssignments.isEmpty()) {
			int legId = flightAssignments.get(0).getFlightAssignmentLeg().getId();
			membersAssignedInLastLeg = this.repository.findCrewMembersInLastLeg(legId);
			membersInLastLeg = membersAssignedInLastLeg.stream().map(x -> x.getIdentity().getFullName()).toList();
		}

		dashboard.setMembersAssignedInLastLeg(membersInLastLeg);

		flightAssignmentCurrentStatus = this.repository.flightAssignmentsGroupedByCurrentStatus(flightCrewMemberId);
		flightAssignmentsByStatus = new HashMap<>();

		for (Object[] result : flightAssignmentCurrentStatus) {
			CurrentStatus type = (CurrentStatus) result[0];
			Integer count = ((Long) result[1]).intValue();
			flightAssignmentsByStatus.put(type, count);
		}

		dashboard.setFlightAssignmentsByStatus(flightAssignmentsByStatus);

		Date startDate = MomentHelper.getCurrentMoment();
		Date endDate = MomentHelper.getCurrentMoment();
		startDate.setMonth(startDate.getMonth() - 1);
		startDate.setDate(1);
		endDate.setMonth(startDate.getMonth() - 1);
		endDate.setDate(30);
		List<Long> counts = this.repository.getDailyFlightAssignments(startDate, endDate, flightCrewMemberId);
		averageFlightAssignmentInLastMonth = counts.stream().mapToLong(Long::longValue).average().orElse(0.0);
		minimumFlightAssignmentInLastMonth = counts.stream().min(Long::compare).orElse(0L).intValue();
		maximumFlightAssignmentInLastMonth = counts.stream().max(Long::compare).orElse(0L).intValue();
		standardDeviationInLastMonth = Math.sqrt(counts.stream().mapToDouble(c -> Math.pow(c - averageFlightAssignmentInLastMonth, 2)).average().orElse(0.0));

		dashboard.setAverageFlightAssignmentInLastMonth(averageFlightAssignmentInLastMonth);
		dashboard.setMinimumFlightAssignmentInLastMonth(minimumFlightAssignmentInLastMonth);
		dashboard.setMaximumFlightAssignmentInLastMonth(maximumFlightAssignmentInLastMonth);
		dashboard.setStandardDeviationInLastMonth(standardDeviationInLastMonth);
	}

	@Override
	public void unbind(final FlightCrewMemberDashboard dashboard) {
		Dataset dataset;

		dataset = super.unbindObject(dashboard, "lastFiveDestinations", "legsWithLowSeverityIncidents", "legsWithMediumSeverityIncidents", "legsWithHighSeverityIncidents", "membersAssignedInLastLeg", "flightAssignmentsByStatus",
			"averageFlightAssignmentInLastMonth", "minimumFlightAssignmentInLastMonth", "maximumFlightAssignmentInLastMonth", "standardDeviationInLastMonth");

		super.getResponse().addData(dataset);

	}
}
