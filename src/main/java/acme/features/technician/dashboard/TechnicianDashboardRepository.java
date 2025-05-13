
package acme.features.technician.dashboard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.aircraft.Aircraft;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.entities.maintenanceRecord.MaintenanceRecordStatus;
import acme.realms.technician.Technician;

@Repository
public interface TechnicianDashboardRepository extends AbstractRepository {

	// Retrieves a Technician by their user account ID
	@Query("SELECT t FROM Technician t WHERE t.userAccount.id = :id")
	Technician findOneTechnicianByUserAccoundId(int id);

	// Returns the total number of maintenance records for a technician
	@Query("SELECT COUNT(m) FROM MaintenanceRecord m WHERE m.technician.id = :technicianId")
	Integer countMaintenanceRecordsByTechnicianId(int technicianId);

	// Returns the count of maintenance records filtered by status for a technician (Optional result)
	@Query("""
		    SELECT COUNT(m)
		    FROM MaintenanceRecord m
		    WHERE m.technician.id = :technicianId
		    AND m.status = :status
		""")
	Optional<Integer> countMaintenanceRecordsByStatus(int technicianId, MaintenanceRecordStatus status);

	// Retrieves the top 5 aircrafts with the highest number of tasks in maintenance records for a technician
	@Query("""
		    SELECT m.aircraft
		    FROM Involves mrt
		    JOIN mrt.maintenanceRecord m
		    WHERE m.technician.id = :technicianId
		    GROUP BY m.aircraft
		    ORDER BY COUNT(mrt.task) DESC
		""")
	List<Aircraft> findTopFiveAircraftsByTechnicianId(int technicianId);

	// Retrieves upcoming maintenance records ordered by next inspection date for a technician
	@Query("SELECT m FROM MaintenanceRecord m WHERE m.technician.id = :technicianId AND m.nextInspectionDate >= CURRENT_TIMESTAMP ORDER BY m.nextInspectionDate ASC")
	List<MaintenanceRecord> findNearestInspectionRecordsByTechnicianId(int technicianId);

	// Computes the average estimated cost of maintenance records for a technician
	@Query("SELECT AVG(m.estimatedCost.amount) FROM MaintenanceRecord m WHERE m.technician.id = :technicianId")
	Double findAverageEstimatedCost(int technicianId);

	// Computes the standard deviation of estimated costs of maintenance records for a technician
	@Query("SELECT STDDEV(m.estimatedCost.amount) FROM MaintenanceRecord m WHERE m.technician.id = :technicianId")
	Double findDeviationEstimatedCost(int technicianId);

	// Finds the minimum estimated cost among maintenance records for a technician
	@Query("SELECT MIN(m.estimatedCost.amount) FROM MaintenanceRecord m WHERE m.technician.id = :technicianId")
	Double findMinEstimatedCost(int technicianId);

	// Finds the maximum estimated cost among maintenance records for a technician
	@Query("SELECT MAX(m.estimatedCost.amount) FROM MaintenanceRecord m WHERE m.technician.id = :technicianId")
	Double findMaxEstimatedCost(int technicianId);

	// Computes the average estimated duration of tasks for a technician
	@Query("SELECT AVG(t.estimatedDuration) FROM Task t WHERE t.technician.id = :technicianId")
	Double findAverageEstimatedDuration(int technicianId);

	// Computes the standard deviation of estimated durations of tasks for a technician
	@Query("SELECT STDDEV(t.estimatedDuration) FROM Task t WHERE t.technician.id = :technicianId")
	Double findDeviationEstimatedDuration(int technicianId);

	// Finds the minimum estimated duration among tasks for a technician
	@Query("SELECT MIN(t.estimatedDuration) FROM Task t WHERE t.technician.id = :technicianId")
	Double findMinEstimatedDuration(int technicianId);

	// Finds the maximum estimated duration among tasks for a technician
	@Query("SELECT MAX(t.estimatedDuration) FROM Task t WHERE t.technician.id = :technicianId")
	Double findMaxEstimatedDuration(int technicianId);
}
