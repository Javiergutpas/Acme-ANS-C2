
package acme.features.flightcrewmember.dashboard;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.flightassignment.CurrentStatus;
import acme.entities.flightassignment.FlightAssignment;
import acme.realms.flightcrewmember.FlightCrewMember;

@Repository
public interface FlightCrewMemberDashboardRepository extends AbstractRepository {

	@Query("SELECT l.arrivalAirport.name FROM FlightAssignment fa JOIN fa.flightAssignmentLeg l WHERE fa.flightAssignmentCrewMember.id = :flightCrewMemberId ORDER BY fa.lastUpdateMoment DESC")
	List<String> findLastFiveDestinations(int flightCrewMember);

	@Query("SELECT COUNT(DISTINCT CASE WHEN al.severityLevel BETWEEN 0 AND 3 THEN l.id END) FROM ActivityLog al JOIN al.activityLogAssignment.flightAssignmentLeg l")
	Integer findLegsWithLowSeverityIncident();

	@Query("SELECT COUNT(DISTINCT CASE WHEN al.severityLevel BETWEEN 4 AND 7 THEN l.id END) FROM ActivityLog al JOIN al.activityLogAssignment.flightAssignmentLeg l")
	Integer findLegsWithMediumSeverityIncident();

	@Query("SELECT COUNT(DISTINCT CASE WHEN al.severityLevel BETWEEN 8 AND 10 THEN l.id END) FROM ActivityLog al JOIN al.activityLogAssignment.flightAssignmentLeg l")
	Integer findLegsWithHighSeverityIncident();

	@Query("SELECT DISTINCT fa.flightAssignmentCrewMember FROM FlightAssignment fa WHERE fa.flightAssignmentLeg.id = (SELECT MAX(fa2.flightAssignmentLeg.id) FROM FlightAssignment fa2 WHERE fa2.flightAssignmentCrewMember.id = :flightCrewMemberId)")
	List<FlightCrewMember> findCrewMembersInMemberLastLeg(int flightCrewMemberId);

	@Query("SELECT fa FROM FlightAssignment fa WHERE fa.flightAssignmentCrewMember.id =:flightCrewMemberId and fa.currentStatus = :status")
	List<FlightAssignment> findFlightAssignmentsByCrewMember(int flightCrewMemberId, CurrentStatus status);

	@Query("SELECT AVG(COUNT(fa)) FROM FlightAssignment fa WHERE fa.lastUpdateMoment BETWEEN :startDate AND :endDate AND fa.flightAssignmentCrewMember.id = :flightCrewMemberId")
	Double calculateAverageAssignmentsInLastMonth(Date startDate, Date endDate, int flightCrewMemberId);

	@Query("SELECT MIN(COUNT(fa)) FROM FlightAssignment fa WHERE fa.lastUpdateMoment BETWEEN :startDate AND :endDate AND fa.flightAssignmentCrewMember.id = :flightCrewMemberId")
	Integer calculateMinimumAssignmentsInLastMonth(Date startDate, Date endDate, int flightCrewMemberId);

	@Query("SELECT MAX(COUNT(fa)) FROM FlightAssignment fa WHERE fa.lastUpdateMoment BETWEEN :startDate AND :endDate AND fa.flightAssignmentCrewMember.id = :flightCrewMemberId")
	Integer calculateMaximumAssignmentsInLastMonth(Date startDate, Date endDate, int flightCrewMemberId);

	@Query("SELECT STDDEV(COUNT(fa)) FROM FlightAssignment fa WHERE fa.lastUpdateMoment BETWEEN :startDate AND :endDate AND fa.flightAssignmentCrewMember.id = :flightCrewMemberId")
	Double calculateStandardDeviationAssignmentsInLastMonth(Date startDate, Date endDate, int flightCrewMemberId);
}
