
package acme.features.flightcrewmember.flightassignment;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.activitylog.ActivityLog;
import acme.entities.flightassignment.Duty;
import acme.entities.flightassignment.FlightAssignment;
import acme.entities.leg.Leg;
import acme.realms.flightcrewmember.FlightCrewMember;

@Repository
public interface FlightAssignmentRepository extends AbstractRepository {

	@Query("SELECT fa FROM FlightAssignment fa WHERE fa.flightAssignmentLeg.arrival < CURRENT_TIMESTAMP AND fa.flightAssignmentLeg.publish = true")
	Collection<FlightAssignment> findCompletedFlightAssignments();

	@Query("SELECT fa FROM FlightAssignment fa WHERE fa.flightAssignmentLeg.departure > CURRENT_TIMESTAMP AND fa.flightAssignmentLeg.publish = true")
	Collection<FlightAssignment> findPlannedFlightAssignments();

	@Query("SELECT fa FROM FlightAssignment fa WHERE fa.flightAssignmentCrewMember.id = :flightCrewMemberId AND fa.flightAssignmentLeg.arrival < CURRENT_TIMESTAMP AND fa.flightAssignmentLeg.publish = true")
	Collection<FlightAssignment> findCompletedFlightAssignmentsByMemberId(final int flightCrewMemberId);

	@Query("SELECT fa FROM FlightAssignment fa WHERE fa.flightAssignmentCrewMember.id = :flightCrewMemberId AND fa.flightAssignmentLeg.departure > CURRENT_TIMESTAMP AND fa.flightAssignmentLeg.publish = true")
	Collection<FlightAssignment> findPlannedFlightAssignmentsByMemberId(final int flightCrewMemberId);

	@Query("SELECT fa FROM FlightAssignment fa WHERE fa.id = :id")
	FlightAssignment findFlightAssignmentById(int id);

	@Query("SELECT l FROM Leg l WHERE l.publish = true")
	Collection<Leg> findAllLegs();

	@Query("SELECT fcm FROM FlightCrewMember fcm")
	Collection<FlightCrewMember> findAllFlightCrewMembers();

	@Query("SELECT fcm FROM FlightCrewMember fcm WHERE fcm.id = :flightCrewMemberId")
	FlightCrewMember findFlightCrewMemberById(int flightCrewMemberId);

	@Query("select count(fa) from FlightAssignment fa where fa.flightAssignmentLeg.id = :legId and fa.duty = :duty and fa.id != :id and fa.publish = true")
	int hasDutyAssigned(int legId, Duty duty, int id);

	@Query("select fa  from FlightAssignment fa where fa.flightAssignmentCrewMember.id = :id and fa.flightAssignmentLeg.departure< :arrival and fa.flightAssignmentLeg.arrival> :departure and fa.publish = true")
	Collection<FlightAssignment> findFlightAssignmentsByFlightCrewMemberInRange(int id, Date departure, Date arrival);

	@Query("select al from ActivityLog al where al.activityLogAssignment.id = :activityLogAssignmentId")
	Collection<ActivityLog> findAllLogsByAssignmentId(int activityLogAssignmentId);
}
