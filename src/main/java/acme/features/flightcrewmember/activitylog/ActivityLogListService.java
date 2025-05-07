
package acme.features.flightcrewmember.activitylog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activitylog.ActivityLog;
import acme.entities.flightassignment.FlightAssignment;
import acme.realms.flightcrewmember.FlightCrewMember;

@GuiService
public class ActivityLogListService extends AbstractGuiService<FlightCrewMember, ActivityLog> {

	//Internal state ---------------------------------------------

	@Autowired
	private ActivityLogRepository repository;

	//AbstractGuiService interface -------------------------------


	@Override
	public void authorise() {
		int flightAssignmentId;
		int flightCrewMemberId;
		FlightAssignment flightAssignment;
		boolean status;

		flightAssignmentId = super.getRequest().getData("flightAssignmentId", int.class);
		flightCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		flightAssignment = this.repository.findFlightAssignmentById(flightAssignmentId);

		status = flightAssignment.getFlightAssignmentCrewMember().getId() == flightCrewMemberId;

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<ActivityLog> activityLogs;
		int flightAssignmentId;

		flightAssignmentId = super.getRequest().getData("flightAssignmentId", int.class);

		activityLogs = this.repository.findAllActivityLogsByFlightAssignmentId(flightAssignmentId);

		super.getBuffer().addData(activityLogs);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;

		dataset = super.unbindObject(activityLog, "registrationMoment", "incidentType", "severityLevel", "publish");

		super.addPayload(dataset, activityLog, "publish");

		super.getResponse().addData(dataset);
	}

	@Override
	public void unbind(final Collection<ActivityLog> activityLog) {
		int flightAssignmentId;
		flightAssignmentId = super.getRequest().getData("flightAssignmentId", int.class);
		super.getResponse().addGlobal("flightAssignmentId", flightAssignmentId);
	}
}
