
package acme.features.technician.dashboard;

// Import statements remain grouped at the top
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Principal;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.entities.maintenanceRecord.MaintenanceRecordStatus;
import acme.forms.TechnicianDashboard;
import acme.realms.technician.Technician;

@Service
@GuiService
public class TechnicianDashboardShowService extends AbstractGuiService<Technician, TechnicianDashboard> {

	// Internal state dependencies injected via Spring
	@Autowired
	private TechnicianDashboardRepository repository;


	// Ensures the current user is authorized as a Technician
	@Override
	public void authorise() {
		boolean authorised = false;
		Principal principal = super.getRequest().getPrincipal();
		int userAccountId = principal.getAccountId();

		Technician technician = this.repository.findOneTechnicianByUserAccoundId(userAccountId);

		authorised = technician != null; // Authorization granted if technician exists
		super.getResponse().setAuthorised(authorised);
	}

	// Populates the TechnicianDashboard with data
	@Override
	public void load() {
		final TechnicianDashboard dashboard = new TechnicianDashboard();
		Principal principal = super.getRequest().getPrincipal();
		int userAccountId = principal.getAccountId();
		final Technician technician = this.repository.findOneTechnicianByUserAccoundId(userAccountId);

		if (technician != null) {
			// Calculate counts of maintenance records by status
			Map<String, Integer> numOfRecordsByStatus = this.calculateMaintenanceRecordCountsByStatus(technician);
			dashboard.setNumberOfRecordsGroupedByStatus(numOfRecordsByStatus);

			// Find the nearest upcoming inspection date
			Date nearestInspection = this.findNearestInspectionDate(technician);
			dashboard.setNearestInspectionMaintenanceRecord(nearestInspection);

			// Retrieve top 5 aircrafts associated with the technician
			List<Aircraft> topAircrafts = this.findTopFiveAircrafts(technician);
			dashboard.setTopFiveAircrafts(topAircrafts);

			// Calculate statistics for estimated costs and durations
			this.calculateCostStatistics(technician, dashboard);
			this.calculateDurationStatistics(technician, dashboard);

			super.getBuffer().addData(dashboard);
		}
	}

	// Prepares the data for display in the view
	@Override
	public void unbind(final TechnicianDashboard dashboard) {
		// Convert maintenance record status counts into SelectChoices for the view
		SelectChoices statusChoices = new SelectChoices();
		dashboard.getNumberOfRecordsGroupedByStatus().forEach((status, count) -> statusChoices.add(status, count.toString(), false));

		// Format top 5 aircrafts into a concatenated string
		String aircraftsFormatted = dashboard.getTopFiveAircrafts().stream().map(Aircraft::getId).map(String::valueOf).reduce((a, b) -> a + " | " + b).orElse("No aircrafts found");

		// Bind all dashboard data to the dataset
		Dataset dataset = super.unbindObject(dashboard, "nearestInspectionMaintenanceRecord", "averageEstimatedCost", "deviationEstimatedCost", "minEstimatedCost", "maxEstimatedCost", "averageEstimatedDuration", "deviationEstimatedDuration",
			"minEstimatedDuration", "maxEstimatedDuration");

		dataset.put("MaintenanceRecordStatus", statusChoices);
		dataset.put("topFiveAircrafts", aircraftsFormatted);
		dataset.put("numberOfRecordsGroupedByStatus", dashboard.getNumberOfRecordsGroupedByStatus());

		super.getResponse().addData(dataset);
	}

	// Helper method to calculate maintenance record counts by status
	private Map<String, Integer> calculateMaintenanceRecordCountsByStatus(final Technician technician) {
		Map<String, Integer> counts = new HashMap<>();
		counts.put("PENDING", this.getRecordCountByStatus(technician, MaintenanceRecordStatus.PENDING));
		counts.put("IN_PROGRESS", this.getRecordCountByStatus(technician, MaintenanceRecordStatus.IN_PROGRESS));
		counts.put("COMPLETED", this.getRecordCountByStatus(technician, MaintenanceRecordStatus.COMPLETED));
		return counts;
	}

	// Helper method to retrieve count of records for a given status
	private Integer getRecordCountByStatus(final Technician technician, final MaintenanceRecordStatus status) {
		return this.repository.countMaintenanceRecordsByStatus(technician.getId(), status).orElse(0);
	}

	// Helper method to find the nearest inspection date
	private Date findNearestInspectionDate(final Technician technician) {
		return this.repository.findNearestInspectionRecordsByTechnicianId(technician.getId()).stream().findFirst().map(MaintenanceRecord::getNextInspectionDate).orElse(null);
	}

	// Helper method to retrieve top 5 aircrafts
	private List<Aircraft> findTopFiveAircrafts(final Technician technician) {
		List<Aircraft> aircrafts = this.repository.findTopFiveAircraftsByTechnicianId(technician.getId());
		return aircrafts != null ? aircrafts.stream().limit(5).toList() : List.of();
	}

	// Helper method to populate cost-related statistics
	private void calculateCostStatistics(final Technician technician, final TechnicianDashboard dashboard) {
		dashboard.setAverageEstimatedCost(this.getDoubleOrZero(this.repository.findAverageEstimatedCost(technician.getId())));
		dashboard.setDeviationEstimatedCost(this.getDoubleOrZero(this.repository.findDeviationEstimatedCost(technician.getId())));
		dashboard.setMinEstimatedCost(this.getDoubleOrZero(this.repository.findMinEstimatedCost(technician.getId())));
		dashboard.setMaxEstimatedCost(this.getDoubleOrZero(this.repository.findMaxEstimatedCost(technician.getId())));
	}

	// Helper method to populate duration-related statistics
	private void calculateDurationStatistics(final Technician technician, final TechnicianDashboard dashboard) {
		dashboard.setAverageEstimatedDuration(this.getDoubleOrZero(this.repository.findAverageEstimatedDuration(technician.getId())));
		dashboard.setDeviationEstimatedDuration(this.getDoubleOrZero(this.repository.findDeviationEstimatedDuration(technician.getId())));
		dashboard.setMinEstimatedDuration(this.getDoubleOrZero(this.repository.findMinEstimatedDuration(technician.getId())));
		dashboard.setMaxEstimatedDuration(this.getDoubleOrZero(this.repository.findMaxEstimatedDuration(technician.getId())));
	}

	// Safely handles null values from repository, defaulting to 0.0
	private Double getDoubleOrZero(final Double value) {
		return value != null ? value : 0.0;
	}
}
