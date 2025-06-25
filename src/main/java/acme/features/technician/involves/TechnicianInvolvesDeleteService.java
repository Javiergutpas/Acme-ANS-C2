
package acme.features.technician.involves;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.involves.Involves;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.entities.task.Task;
import acme.realms.technician.Technician;

@GuiService
public class TechnicianInvolvesDeleteService extends AbstractGuiService<Technician, Involves> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private TechnicianInvolvesRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {
		int technicianId = super.getRequest().getPrincipal().getActiveRealm().getId();
		int maintenanceRecordId = super.getRequest().getData("maintenanceRecordId", int.class);
		MaintenanceRecord maintenanceRecord = this.repository.findMaintenanceRecordById(maintenanceRecordId);

		boolean status = maintenanceRecord != null && !maintenanceRecord.getPublished() && maintenanceRecord.getTechnician().getId() == technicianId;

		if (status) {
			String method;

			method = super.getRequest().getMethod();
			if (method.equals("GET"))
				status = true;
			else {
				int taskId = super.getRequest().getData("task", int.class);
				Involves involves = this.repository.findInvolvesByMaintenanceRecordAndTask(maintenanceRecordId, taskId);
				status = taskId == 0 || involves != null;

			}
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int maintenanceRecordId = super.getRequest().getData("maintenanceRecordId", int.class);
		MaintenanceRecord maintenanceRecord = this.repository.findMaintenanceRecordById(maintenanceRecordId);

		Involves involves = new Involves();
		involves.setMaintenanceRecord(maintenanceRecord);

		super.getBuffer().addData(involves);
	}

	@Override
	public void bind(final Involves involves) {
		super.bindObject(involves, "task");
	}

	@Override
	public void validate(final Involves involves) {
		Task task;
		task = super.getRequest().getData("task", Task.class);
		if (!this.getBuffer().getErrors().hasErrors("task"))
			super.state(task != null, "task", "acme.validation.technician.involves.noTask.message");
	}

	@Override
	public void perform(final Involves involves) {
		int taskId = super.getRequest().getData("task", Task.class).getId();
		int maintenanceRecordId = super.getRequest().getData("maintenanceRecordId", int.class);

		this.repository.delete(this.repository.findInvolvesByMaintenanceRecordAndTask(maintenanceRecordId, taskId));
	}

	@Override
	public void unbind(final Involves involves) {
		SelectChoices task;
		Dataset dataset;

		int maintenanceRecordId = super.getRequest().getData("maintenanceRecordId", int.class);

		Collection<Task> tasks = this.repository.findTaskOfMaintenanceRecord(maintenanceRecordId);

		task = SelectChoices.from(tasks, "description", involves.getTask());

		dataset = super.unbindObject(involves, "maintenanceRecord", "task");

		dataset.put("task", task);
		dataset.put("maintenanceRecordId", maintenanceRecordId);

		super.getResponse().addData(dataset);
	}

}
