
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

	@Query("SELECT l FROM Leg l WHERE l.publish = true AND l.departure > CURRENT_TIMESTAMP")
	Collection<Leg> findAllFutureLegs();

	@Query("SELECT fcm FROM FlightCrewMember fcm")
	Collection<FlightCrewMember> findAllFlightCrewMembers();

	@Query("SELECT fcm FROM FlightCrewMember fcm WHERE fcm.id = :flightCrewMemberId")
	FlightCrewMember findFlightCrewMemberById(int flightCrewMemberId);

	@Query("SELECT COUNT(fa) FROM FlightAssignment fa WHERE fa.flightAssignmentLeg.id = :legId AND fa.duty = :duty AND fa.id != :id AND fa.publish = true")
	int hasDutyAssigned(int legId, Duty duty, int id);

	@Query("SELECT fa  FROM FlightAssignment fa WHERE fa.flightAssignmentCrewMember.id = :id AND fa.flightAssignmentLeg.departure< :arrival AND fa.flightAssignmentLeg.arrival> :departure AND fa.publish = true")
	Collection<FlightAssignment> findFlightAssignmentsByFlightCrewMemberInRange(int id, Date departure, Date arrival);

	@Query("SELECT al FROM ActivityLog al WHERE al.activityLogAssignment.id = :activityLogAssignmentId")
	Collection<ActivityLog> findAllLogsByAssignmentId(int activityLogAssignmentId);

	@Query("SELECT l FROM Leg l WHERE l.id = :legId")
	Leg findLegById(int legId);

	@Query("SELECT l FROM Leg l WHERE l.publish = true AND l.id = :legId")
	Leg findPublishedLegById(int legId);
}
