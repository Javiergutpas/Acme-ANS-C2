
package acme.features.flightcrewmember.flightassignment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.flightassignment.CurrentStatus;
import acme.entities.flightassignment.Duty;
import acme.entities.flightassignment.FlightAssignment;
import acme.entities.leg.Leg;
import acme.realms.flightcrewmember.AvailabilityStatus;
import acme.realms.flightcrewmember.FlightCrewMember;

@GuiService
public class FlightAssignmentPublishService extends AbstractGuiService<FlightCrewMember, FlightAssignment> {

	// Internal state ---------------------------------------------------------
	@Autowired
	private FlightAssignmentRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {

		boolean status;
		int flightAssignmentId;
		FlightAssignment flightAssignment;

		flightAssignmentId = super.getRequest().getData("id", int.class);
		flightAssignment = this.repository.findFlightAssignmentById(flightAssignmentId);

		status = !flightAssignment.isPublish();

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		FlightAssignment flightAssignment;

		flightAssignment = new FlightAssignment();

		super.getBuffer().addData(flightAssignment);
	}

	@Override
	public void bind(final FlightAssignment flightAssignment) {
		super.bindObject(flightAssignment, "duty", "lastUpdateMoment", "currentStatus", "remarks", "flightAssignmentCrewMember", "flightAssignmentLeg");
	}

	@Override
	public void validate(final FlightAssignment flightAssignment) {
		int flightCrewMemberId;

		boolean completedLeg;
		boolean availableMember;
		boolean legsOverlap;

		Leg leg;

		Collection<FlightAssignment> OverlappedLegs;

		flightCrewMemberId = super.getRequest().getPrincipal().getActiveRealm().getId();
		leg = flightAssignment.getFlightAssignmentLeg();

		if (leg != null && leg.getArrival() != null) {
			completedLeg = leg.getArrival().before(MomentHelper.getCurrentMoment());
			super.state(!completedLeg, "*", "acme.validation.flightassignment.leg.completed.message");
		}

		availableMember = this.repository.findFlightCrewMemberById(flightCrewMemberId).getAvailabilityStatus().equals(AvailabilityStatus.AVAILABLE);
		super.state(availableMember, "*", "acme.validation.flightassignment.flightcrewmember.available.message");

		if (flightAssignment.getDuty() != null && flightAssignment.getFlightAssignmentLeg() != null)
			if (flightAssignment.getDuty() == Duty.PILOT) {
				int count = this.repository.hasDutyAssigned(flightAssignment.getFlightAssignmentLeg().getId(), flightAssignment.getDuty(), flightAssignment.getId());
				super.state(count == 0, "*", "acme.validation.flightassignment.duty.pilot.message");
			} else if (flightAssignment.getDuty() == Duty.CO_PILOT) {
				int count = this.repository.hasDutyAssigned(flightAssignment.getFlightAssignmentLeg().getId(), flightAssignment.getDuty(), flightAssignment.getId());
				super.state(count == 0, "*", "acme.validation.flightassignment.duty.copilot.message");
			}

		if (leg != null && leg.getArrival() != null) {
			OverlappedLegs = this.repository.findFlightAssignmentsByFlightCrewMemberInRange(flightCrewMemberId, leg.getDeparture(), leg.getArrival());
			legsOverlap = OverlappedLegs.isEmpty();
			super.state(legsOverlap, "*", "acme.validation.flightassignment.leg.overlap.message");
		}
	}

	@Override
	public void perform(final FlightAssignment flightAssignment) {
		flightAssignment.setPublish(true);
		this.repository.save(flightAssignment);
	}

	@Override
	public void unbind(final FlightAssignment flightAssignment) {
		Dataset dataset;
		SelectChoices dutyChoice;
		SelectChoices currentStatusChoice;

		SelectChoices legChoice;
		Collection<Leg> legs;

		SelectChoices flightCrewMemberChoice;
		Collection<FlightCrewMember> flightCrewMembers;

		dutyChoice = SelectChoices.from(Duty.class, flightAssignment.getDuty());
		currentStatusChoice = SelectChoices.from(CurrentStatus.class, flightAssignment.getCurrentStatus());

		legs = this.repository.findAllLegs();
		legChoice = SelectChoices.from(legs, "flightNumber", flightAssignment.getFlightAssignmentLeg());

		flightCrewMembers = this.repository.findAllFlightCrewMembers();
		flightCrewMemberChoice = SelectChoices.from(flightCrewMembers, "employeeCode", flightAssignment.getFlightAssignmentCrewMember());

		dataset = super.unbindObject(flightAssignment, "duty", "lastUpdateMoment", "currentStatus", "remarks", "publish", "flightAssignmentLeg", "flightAssignmentCrewMember");
		dataset.put("dutyChoice", dutyChoice);
		dataset.put("currentStatusChoice", currentStatusChoice);
		dataset.put("legChoice", legChoice);
		dataset.put("flightCrewMemberChoice", flightCrewMemberChoice);

		super.getResponse().addData(dataset);
	}
}
