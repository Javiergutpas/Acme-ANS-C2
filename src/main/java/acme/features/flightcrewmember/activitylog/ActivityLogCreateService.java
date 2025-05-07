
package acme.features.flightcrewmember.activitylog;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.activitylog.ActivityLog;
import acme.entities.flightassignment.FlightAssignment;
import acme.realms.flightcrewmember.FlightCrewMember;

@GuiService
public class ActivityLogCreateService extends AbstractGuiService<FlightCrewMember, ActivityLog> {

	// Internal state ---------------------------------------------------------
	@Autowired
	private ActivityLogRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		FlightAssignment flightAssignment;
		int flightAssignmentId;
		int flightCrewMemberId;
		boolean status;

		flightAssignmentId = super.getRequest().getData("flightAssignmentId", int.class);
		flightAssignment = this.repository.findFlightAssignmentById(flightAssignmentId);
		flightCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();

		status = flightAssignment != null && flightAssignment.getFlightAssignmentCrewMember().getId() == flightCrewMemberId;

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		ActivityLog activityLog;
		int flightAssignmentId;
		FlightAssignment flightAssignment;

		flightAssignmentId = super.getRequest().getData("flightAssignmentId", int.class);
		flightAssignment = this.repository.findFlightAssignmentById(flightAssignmentId);

		activityLog = new ActivityLog();
		activityLog.setActivityLogAssignment(flightAssignment);
		activityLog.setPublish(false);

		super.getBuffer().addData(activityLog);
	}

	@Override
	public void bind(final ActivityLog activityLog) {
		super.bindObject(activityLog, "registrationMoment", "incidentType", "description", "severityLevel", "activityLogAssignment");
	}

	@Override
	public void validate(final ActivityLog activityLog) {
		;
	}

	@Override
	public void perform(final ActivityLog activityLog) {
		this.repository.save(activityLog);
	}

	@Override
	public void unbind(final ActivityLog activityLog) {
		Dataset dataset;

		SelectChoices flightAssignmentChoice;
		Collection<FlightAssignment> flightAssignments;

		flightAssignments = this.repository.findAllFlightAssignments();
		flightAssignmentChoice = SelectChoices.from(flightAssignments, "id", activityLog.getActivityLogAssignment());

		dataset = super.unbindObject(activityLog, "registrationMoment", "incidentType", "description", "severityLevel", "activityLogAssignment");
		dataset.put("flightAssignmentChoice", flightAssignmentChoice);
		dataset.put("flightAssignmentId", super.getRequest().getData("flightAssignmentId", int.class));

		super.getResponse().addData(dataset);
	}
}
