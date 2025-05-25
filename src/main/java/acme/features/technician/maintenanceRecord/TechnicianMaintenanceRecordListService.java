
package acme.features.technician.maintenanceRecord;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Principal;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.realms.technician.Technician;

@GuiService
public class TechnicianMaintenanceRecordListService extends AbstractGuiService<Technician, MaintenanceRecord> {

	//Internal state ---------------------------------------------

	@Autowired
	private TechnicianMaintenanceRecordRepository repository;

	//AbstractGuiService interface -------------------------------


	@Override
	public void authorise() {
		boolean authorised = false;
		Principal principal = super.getRequest().getPrincipal();
		int userAccountId = principal.getAccountId();

		Technician technician = this.repository.findTechnicianByUserAccoundId(userAccountId);

		if (technician != null)
			authorised = true;

		super.getResponse().setAuthorised(authorised);
	}

	@Override
	public void load() {
		Collection<MaintenanceRecord> maintenanceRecord;
		int technicianId;

		technicianId = super.getRequest().getPrincipal().getActiveRealm().getId();

		maintenanceRecord = this.repository.findAllMaintenanceRecordByTechnicianId(technicianId);

		super.getBuffer().addData(maintenanceRecord);
	}

	@Override
	public void unbind(final MaintenanceRecord maintenanceRecord) {
		Dataset dataset;

		dataset = super.unbindObject(maintenanceRecord, "moment", "status", "nextInspectionDate", "published");

		super.addPayload(dataset, maintenanceRecord, "moment");

		super.getResponse().addData(dataset);
	}

}
