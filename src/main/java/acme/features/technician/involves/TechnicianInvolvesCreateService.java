
package acme.features.technician.involves;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Principal;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.involves.Involves;
import acme.entities.maintenanceRecord.MaintenanceRecord;
import acme.entities.task.Task;
import acme.realms.technician.Technician;

@GuiService
public class TechnicianInvolvesCreateService extends AbstractGuiService<Technician, Involves> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private TechnicianInvolvesRepository repository;


	// AbstractGuiService interface -------------------------------------------
	@Override
	public void authorise() {

		boolean authorised = false;
		Principal principal = super.getRequest().getPrincipal();
		int userAccountId = principal.getAccountId();

		int maintenanceRecordId = super.getRequest().getData("maintenanceRecordId", int.class);
		MaintenanceRecord maintenanceRecord = this.repository.findMaintenanceRecordById(maintenanceRecordId);
		Technician technician = this.repository.findTechnicianByUserAccoundId(userAccountId);
		if (maintenanceRecord != null) {

			authorised = technician.getId() == maintenanceRecord.getTechnician().getId() && maintenanceRecord.getPublished() == false;

			if (authorised) {
				String method = super.getRequest().getMethod();

				if (method.equals("GET"))
					authorised = true;
				else {
					int id;
					int version;
					int taskId;
					int sameTask;
					Task task;

					id = super.getRequest().getData("id", int.class);
					version = super.getRequest().getData("version", int.class);

					taskId = super.getRequest().getData("task", int.class);
					sameTask = this.repository.countInvolvesByMaintenanceRecordIdAndTask(maintenanceRecordId, taskId);
					task = this.repository.findTaskById(taskId);
					boolean taskExists = this.repository.findAllTaskByTechnicianId(technician.getId()).contains(task);
					authorised = (taskId == 0 || taskExists) && id == 0 && version == 0 && sameTask < 1;
				}
			}
		}
		super.getResponse().setAuthorised(authorised);

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

	}

	@Override
	public void perform(final Involves involves) {
		this.repository.save(involves);
	}

	@Override
	public void unbind(final Involves involves) {
		SelectChoices task;
		Dataset dataset;

		int technicianId = super.getRequest().getPrincipal().getActiveRealm().getId();
		int maintenanceRecordId = super.getRequest().getData("maintenanceRecordId", int.class);

		Collection<Task> taskOnMaintenanceRecord = this.repository.findTaskOfMaintenanceRecord(maintenanceRecordId);

		Collection<Task> tasks = this.repository.findAllTaskByTechnicianId(technicianId).stream().filter(t -> !taskOnMaintenanceRecord.contains(t)).toList();

		task = SelectChoices.from(tasks, "description", involves.getTask());

		dataset = super.unbindObject(involves, "maintenanceRecord", "task");

		dataset.put("task", task);
		dataset.put("maintenanceRecordId", maintenanceRecordId);

		super.getResponse().addData(dataset);
	}

}
