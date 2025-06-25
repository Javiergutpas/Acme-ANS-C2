
package acme.features.technician.maintenanceRecord;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.aircraft.Aircraft;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.entities.maintenanceRecord.MaintenanceRecordStatus;
import acme.realms.technician.Technician;

@GuiService
public class TechnicianMaintenanceRecordPublishService extends AbstractGuiService<Technician, MaintenanceRecord> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private TechnicianMaintenanceRecordRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		MaintenanceRecord maintenanceRecord;
		int maintenanceRecordId;
		int technicianId;
		boolean status;
		maintenanceRecordId = super.getRequest().getData("id", int.class);
		maintenanceRecord = this.repository.findMaintenanceRecordById(maintenanceRecordId);
		technicianId = super.getRequest().getPrincipal().getActiveRealm().getId();
		status = maintenanceRecord != null && !maintenanceRecord.getPublished() && maintenanceRecord.getTechnician().getId() == technicianId;

		if (status) {
			String method;

			method = super.getRequest().getMethod();
			if (method.equals("GET"))
				status = true;
			else {
				String maintenanceRecordStatus = super.getRequest().getData("status", String.class);
				boolean correctStatus = "0".equals(maintenanceRecordStatus) || Arrays.stream(MaintenanceRecordStatus.values()).map(MaintenanceRecordStatus::name).anyMatch(name -> name.equals(maintenanceRecordStatus));
				int aircraftId = super.getRequest().getData("aircraft", int.class);
				Aircraft aircraft = this.repository.findAircraftById(aircraftId);
				boolean aircraftExists = this.repository.findAllAircrafts().contains(aircraft);
				status = (aircraftId == 0 || aircraftExists) && correctStatus;
			}
		}
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		MaintenanceRecord maintenanceRecord;
		int id;

		id = super.getRequest().getData("id", int.class);
		maintenanceRecord = this.repository.findMaintenanceRecordById(id);

		super.getBuffer().addData(maintenanceRecord);
	}

	@Override
	public void bind(final MaintenanceRecord maintenanceRecord) {
		super.bindObject(maintenanceRecord, "status", "nextInspectionDate", "estimatedCost", "notes", "aircraft");
	}

	@Override
	public void validate(final MaintenanceRecord maintenanceRecord) {
		int id;
		id = super.getRequest().getData("id", int.class);

		if (!this.getBuffer().getErrors().hasErrors("nextInspectionDate"))
			super.state(maintenanceRecord.getNextInspectionDate().compareTo(maintenanceRecord.getMoment()) >= 0, "nextInspectionDate", "acme.validation.technician.maintenance-record.nextInspectionDate.message");

		if (!this.getBuffer().getErrors().hasErrors("published"))
			super.state(this.repository.findNotPublishedTaskOfMaintenanceRecord(id) == 0 && this.repository.countAllRelatedTaskWithMaintenanceRecord(id) != 0, "*", "acme.validation.technician.maintenance-record.published.message");
	}

	@Override
	public void perform(final MaintenanceRecord maintenanceRecord) {
		maintenanceRecord.setPublished(true);
		this.repository.save(maintenanceRecord);
	}

	@Override
	public void unbind(final MaintenanceRecord maintenanceRecord) {
		SelectChoices choices;
		Collection<Aircraft> aircrafts;
		SelectChoices aircraft;

		Dataset dataset;
		aircrafts = this.repository.findAllAircrafts();
		choices = SelectChoices.from(MaintenanceRecordStatus.class, maintenanceRecord.getStatus());
		aircraft = SelectChoices.from(aircrafts, "registrationNumber", maintenanceRecord.getAircraft());

		dataset = super.unbindObject(maintenanceRecord, "moment", "status", "nextInspectionDate", "estimatedCost", "notes", "aircraft", "published");

		dataset.put("status", choices.getSelected().getKey());
		dataset.put("status", choices);
		dataset.put("aircraft", aircraft.getSelected().getKey());
		dataset.put("aircraft", aircraft);

		super.getResponse().addData(dataset);
	}

}
