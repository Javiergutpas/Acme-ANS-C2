
package acme.features.technician.maintenanceRecord;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.aircraft.Aircraft;
import acme.entities.involves.Involves;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.entities.task.Task;
import acme.realms.technician.Technician;

@Repository
public interface TechnicianMaintenanceRecordRepository extends AbstractRepository {

	@Query("SELECT m FROM MaintenanceRecord m")
	Collection<MaintenanceRecord> findAllMaintenanceRecords();

	@Query("SELECT m FROM MaintenanceRecord m WHERE m.technician.id = :technicianId ")
	Collection<MaintenanceRecord> findAllMaintenanceRecordByTechnicianId(final int technicianId);

	@Query("SELECT t FROM Technician t WHERE t.userAccount.id = :id")
	Technician findTechnicianByUserAccoundId(int id);

	@Query("SELECT m FROM MaintenanceRecord m WHERE m.id = :id")
	MaintenanceRecord findMaintenanceRecordById(int id);

	@Query("SELECT a FROM Aircraft a")
	Collection<Aircraft> findAllAircrafts();

	@Query("select a from Aircraft a where a.id = :aircraftId")
	Aircraft findAircraftById(int aircraftId);

	@Query("SELECT i.task FROM Involves i WHERE i.maintenanceRecord.id = :id")
	Collection<Task> findAllRelatedTaskWithMaintenanceRecord(int id);

	@Query("SELECT COUNT(i.task) FROM Involves i WHERE i.maintenanceRecord.id = :id")
	Integer countAllRelatedTaskWithMaintenanceRecord(int id);

	@Query("SELECT COUNT(i.task) FROM Involves i WHERE i.maintenanceRecord.id = :id AND i.task.published = FALSE")
	Integer findNotPublishedTaskOfMaintenanceRecord(int id);

	@Query("SELECT i FROM Involves i WHERE i.maintenanceRecord.id = :id")
	Collection<Involves> findInvolvesByMaintenanceRecordId(int id);
}
