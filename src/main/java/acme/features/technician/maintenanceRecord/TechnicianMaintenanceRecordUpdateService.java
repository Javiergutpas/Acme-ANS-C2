
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
public class TechnicianMaintenanceRecordUpdateService extends AbstractGuiService<Technician, MaintenanceRecord> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private TechnicianMaintenanceRecordRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		boolean authorised = false;
		boolean exist;
		boolean correctStatus;
		String maintenanceRecordStatus;
		MaintenanceRecord maintenanceRecord;
		Technician technician;
		int id;
		String method;
		method = super.getRequest().getMethod();
		int aircraftId;
		Aircraft aircraft;

		id = super.getRequest().getData("id", int.class);
		maintenanceRecord = this.repository.findMaintenanceRecordById(id);

		exist = maintenanceRecord != null;
		if (method.equals("GET"))
			authorised = true;

		if (exist) {
			technician = (Technician) super.getRequest().getPrincipal().getActiveRealm();
			if (technician.equals(maintenanceRecord.getTechnician())) {
				maintenanceRecordStatus = super.getRequest().getData("status", String.class);
				correctStatus = "0".equals(maintenanceRecordStatus) || Arrays.stream(MaintenanceRecordStatus.values()).map(MaintenanceRecordStatus::name).anyMatch(name -> name.equals(maintenanceRecordStatus));
				aircraftId = super.getRequest().getData("aircraft", int.class);
				aircraft = this.repository.findAircraftById(aircraftId);
				boolean aircraftExists = this.repository.findAllAircrafts().contains(aircraft);
				authorised = (aircraftId == 0 || aircraftExists) && correctStatus && !maintenanceRecord.getPublished();
			}
		}
		super.getResponse().setAuthorised(authorised);

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

		if (!this.getBuffer().getErrors().hasErrors("nextInspectionDate"))
			super.state(maintenanceRecord.getNextInspectionDate().compareTo(maintenanceRecord.getMoment()) > 0, "nextInspectionDate", "acme.validation.technician.maintenance-record.nextInspectionDate.message");
	}

	@Override
	public void perform(final MaintenanceRecord maintenanceRecord) {
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
		aircraft = SelectChoices.from(aircrafts, "id", maintenanceRecord.getAircraft());

		dataset = super.unbindObject(maintenanceRecord, "moment", "status", "nextInspectionDate", "estimatedCost", "notes", "aircraft", "published");

		dataset.put("status", choices.getSelected().getKey());
		dataset.put("status", choices);
		dataset.put("aircraft", aircraft.getSelected().getKey());
		dataset.put("aircraft", aircraft);

		super.getResponse().addData(dataset);
	}

}
