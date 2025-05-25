
package acme.features.assistanceagent.trackingLog;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.helpers.StringHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.claims.Claim;
import acme.entities.trackingLogs.TrackingLog;
import acme.entities.trackingLogs.TrackingLogStatus;
import acme.realms.assistanceagent.AssistanceAgent;

@GuiService
public class AssistanceAgentTrackingLogCreateService extends AbstractGuiService<AssistanceAgent, TrackingLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AssistanceAgentTrackingLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		Claim claim;
		int claimId;
		int assistanceAgentId;
		boolean status;

		claimId = super.getRequest().getData("claimId", int.class);
		claim = this.repository.findClaimById(claimId);
		assistanceAgentId = super.getRequest().getPrincipal().getActiveRealm().getId();

		status = claim != null && claim.getAssistanceAgent().getId() == assistanceAgentId;

		if (status) {
			String method;
			method = super.getRequest().getMethod();

			if (method.equals("GET"))
				status = true;
			else {
				String trackingLogStatus;
				boolean correctTrackingLogStatus;
				int version;
				int id;
				trackingLogStatus = super.getRequest().getData("status", String.class);

				version = super.getRequest().getData("version", int.class);
				id = super.getRequest().getData("id", int.class);

				correctTrackingLogStatus = "0".equals(trackingLogStatus) || Arrays.stream(TrackingLogStatus.values()).map(TrackingLogStatus::name).anyMatch(name -> name.equals(trackingLogStatus));

				status = id == 0 && version == 0 && correctTrackingLogStatus;
			}
		}
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		TrackingLog trackingLog;
		int claimId;
		Claim claim;
		Date lastUpdateMoment;

		claimId = super.getRequest().getData("claimId", int.class);
		claim = this.repository.findClaimById(claimId);
		lastUpdateMoment = MomentHelper.getCurrentMoment();

		//Hace falta alguno mas? todos?
		trackingLog = new TrackingLog();
		trackingLog.setLastUpdateMoment(lastUpdateMoment);
		trackingLog.setClaim(claim);
		trackingLog.setPublish(false);

		super.getBuffer().addData(trackingLog);
	}

	@Override
	public void bind(final TrackingLog trackingLog) {
		super.bindObject(trackingLog, "step", "resolutionPercentage", "status", "resolution");
	}

	@Override
	public void validate(final TrackingLog trackingLog) {

		Claim claim;
		int claimId;
		claimId = super.getRequest().getData("claimId", int.class);
		claim = this.repository.findClaimById(claimId);

		//Condicion para que el estado del tracking log sea pending si el porcentage no es 100
		if (!super.getBuffer().getErrors().hasErrors("indicator")) {
			boolean bool1;
			boolean bool2;

			if (!super.getBuffer().getErrors().hasErrors("resolutionPercentage")) {
				bool1 = trackingLog.getStatus() == TrackingLogStatus.PENDING && trackingLog.getResolutionPercentage() < 100;
				bool2 = trackingLog.getStatus() != TrackingLogStatus.PENDING && trackingLog.getResolutionPercentage() == 100;
				super.state(bool1 || bool2, "status", "assistanceAgent.tracking-log.form.error.indicator-pending");
			}

		}

		if (!super.getBuffer().getErrors().hasErrors("lastUpdateMoment"))
			super.state(!claim.getRegistrationMoment().after(trackingLog.getLastUpdateMoment()), "lastUpdateMoment", "assistanceAgent.tracking-log.form.error.date-not-valid");

		// Condicion que si indicator es ACCEPTED o REJECTED, resolution no sea nulo o vacío
		if (!super.getBuffer().getErrors().hasErrors("resolution")) {

			boolean requiresResolutionReason = trackingLog.getStatus() == TrackingLogStatus.ACCEPTED || trackingLog.getStatus() == TrackingLogStatus.REJECTED;
			boolean hasResolutionReason = !StringHelper.isBlank(trackingLog.getResolution());

			if (requiresResolutionReason)
				super.state(hasResolutionReason, "resolution", "assistanceAgent.tracking-log.form.error.resolution-required");
		}

		// Condicion para un tracking log excepcional tras el ultimo al 100
		if (!super.getBuffer().getErrors().hasErrors("resolutionPercentage")) {

			Long countLogsWith100 = this.repository.countTrackingLogsForExceptionalCase(claimId);

			super.state(countLogsWith100 < 2, "*", "assistanceAgent.tracking-log.form.error.message.completed");

		}

	}

	@Override
	public void perform(final TrackingLog trackingLog) {

		this.repository.save(trackingLog);
	}

	@Override
	public void unbind(final TrackingLog trackingLog) {
		SelectChoices statusChoices;
		Dataset dataset;

		statusChoices = SelectChoices.from(TrackingLogStatus.class, trackingLog.getStatus());

		dataset = super.unbindObject(trackingLog, "lastUpdateMoment", "step", "resolutionPercentage", "status", "resolution", "publish");
		dataset.put("readonly", false);
		dataset.put("claimId", super.getRequest().getData("claimId", int.class));
		dataset.put("status", statusChoices);

		super.getResponse().addData(dataset);
	}

}
