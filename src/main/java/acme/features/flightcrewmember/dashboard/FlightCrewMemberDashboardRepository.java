
package acme.features.flightcrewmember.dashboard;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.flightassignment.FlightAssignment;
import acme.realms.flightcrewmember.FlightCrewMember;

@Repository
public interface FlightCrewMemberDashboardRepository extends AbstractRepository {

	@Query("SELECT l.arrivalAirport.name FROM FlightAssignment fa JOIN fa.flightAssignmentLeg l WHERE fa.flightAssignmentCrewMember.id = :flightCrewMemberId ORDER BY fa.lastUpdateMoment DESC")
	List<String> findLastFiveDestinations(int flightCrewMemberId);

	@Query("SELECT COUNT(DISTINCT al.activityLogAssignment.flightAssignmentLeg) FROM ActivityLog al WHERE al.severityLevel BETWEEN :inValue AND :outValue AND al.activityLogAssignment.flightAssignmentCrewMember.id = :flightCrewMemberId")
	Integer findLegsWithSeverityIncident(int inValue, int outValue, int flightCrewMemberId);

	@Query("SELECT fa FROM FlightAssignment fa JOIN fa.flightAssignmentLeg l WHERE fa.flightAssignmentCrewMember.id = :flightCrewMemberId ORDER BY l.arrival ASC")
	List<FlightAssignment> findFlightAssignmentByLegArrival(int flightCrewMemberId);

	@Query("SELECT DISTINCT fa.flightAssignmentCrewMember FROM FlightAssignment fa WHERE fa.flightAssignmentLeg.id = :legId")
	List<FlightCrewMember> findCrewMembersInLastLeg(int legId);

	@Query("SELECT fa.currentStatus, COUNT(fa) FROM FlightAssignment fa WHERE fa.flightAssignmentCrewMember.id = :flightCrewMemberId GROUP BY fa.currentStatus")
	List<Object[]> flightAssignmentsGroupedByCurrentStatus(int flightCrewMemberId);

	@Query("SELECT AVG(COUNT(fa)), MIN(COUNT(fa)), MAX(COUNT(fa)), STDDEV(COUNT(fa)) FROM FlightAssignment fa WHERE fa.lastUpdateMoment BETWEEN :startDate AND :endDate AND fa.flightAssignmentCrewMember.id = :flightCrewMemberId")
	List<Integer> flightAssignmentsStatistics(Date startDate, Date endDate, int flightCrewMemberId);

	@Query("SELECT COUNT(fa) FROM FlightAssignment fa WHERE fa.lastUpdateMoment BETWEEN :startDate AND :endDate AND fa.flightAssignmentCrewMember.id = :flightCrewMemberId GROUP BY FUNCTION('DATE', fa.lastUpdateMoment)")
	List<Long> getDailyFlightAssignments(Date startDate, Date endDate, int flightCrewMemberId);
}
