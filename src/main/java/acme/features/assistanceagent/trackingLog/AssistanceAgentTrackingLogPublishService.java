
package acme.features.assistanceagent.trackingLog;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.StringHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.claims.Claim;
import acme.entities.trackingLogs.TrackingLog;
import acme.entities.trackingLogs.TrackingLogStatus;
import acme.realms.assistanceagent.AssistanceAgent;

@GuiService
public class AssistanceAgentTrackingLogPublishService extends AbstractGuiService<AssistanceAgent, TrackingLog> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AssistanceAgentTrackingLogRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		TrackingLog trackingLog;
		int trackingLogId;
		int agentId;
		boolean status;

		trackingLogId = super.getRequest().getData("id", int.class);
		trackingLog = this.repository.findTrackingLogById(trackingLogId);
		agentId = super.getRequest().getPrincipal().getActiveRealm().getId();
		status = trackingLog != null && !trackingLog.isPublish() && trackingLog.getClaim().getAssistanceAgent().getId() == agentId;

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		TrackingLog trackingLog;
		int trackingLogId;

		trackingLogId = super.getRequest().getData("id", int.class);
		trackingLog = this.repository.findTrackingLogById(trackingLogId);

		super.getBuffer().addData(trackingLog);
	}

	@Override
	public void bind(final TrackingLog trackingLog) {
		super.bindObject(trackingLog, "step", "resolutionPercentage", "status", "resolution");
	}

	@Override
	public void validate(final TrackingLog trackingLog) {

		Claim claim = trackingLog.getClaim();
		int claimId = claim.getId();
		//Collection<TrackingLog> claimTrackingLogs = this.repository.findAllTrackingLogsByClaimId(claimId);

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

		if (!super.getBuffer().getErrors().hasErrors("*")) {

			Double maxPublishedPercentage = this.repository.findMaxPublishedResolutionPercentageByClaimId(claimId);

			// Si no hay publicados aún, se permite cualquier porcentaje
			double max = maxPublishedPercentage != null ? maxPublishedPercentage : -1.0;

			super.state(trackingLog.getResolutionPercentage() >= max, "resolutionPercentage", "assistanceAgent.tracking-log.form.error.less-than-max-published");
		}

		boolean status;

		status = claim != null && claim.isPublish(); //solo se publican si el claim esta publicado? 

		super.state(status, "*", "acme.validation.trackingLog.unpublished.message");

	}
	@Override
	public void perform(final TrackingLog trackingLog) {
		trackingLog.setPublish(true);
		this.repository.save(trackingLog);
	}

	@Override
	public void unbind(final TrackingLog trackingLog) {
		SelectChoices statusChoices;
		Dataset dataset;

		statusChoices = SelectChoices.from(TrackingLogStatus.class, trackingLog.getStatus());

		dataset = super.unbindObject(trackingLog, "lastUpdateMoment", "step", "resolutionPercentage", "status", "resolution", "publish");
		dataset.put("claimId", trackingLog.getClaim().getId());
		dataset.put("status", statusChoices);

		super.getResponse().addData(dataset);
	}

}
