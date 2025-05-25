
package acme.features.assistanceagent.trackingLog;

import java.util.List;

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

		//condicion para que el porcentaje de los publicados sea creciente
		if (!super.getBuffer().getErrors().hasErrors("*") && trackingLog.getResolutionPercentage() != null) {

			Double maxPublishedPercentage = this.repository.findMaxPublishedResolutionPercentageByClaimId(claimId);

			// Si no hay publicados aún, se permite cualquier porcentaje
			double max = maxPublishedPercentage != null ? maxPublishedPercentage : -1.0;

			super.state(trackingLog.getResolutionPercentage() >= max, "resolutionPercentage", "assistanceAgent.tracking-log.form.error.less-than-max-published");
		}
		/*
		 * //Condicion para que el tracking log esxtra tenga el mismo estado
		 * if (!super.getBuffer().getErrors().hasErrors("status"))
		 * // Solo aplica la validación si el porcentaje es 100
		 * if (Double.valueOf(100.0).equals(trackingLog.getResolutionPercentage())) {
		 * 
		 * List<TrackingLog> publishedLogs = this.repository.findPublishedTrackingLogsByClaimId(claimId);
		 * TrackingLog trackingLogPublished = publishedLogs.isEmpty() ? null : publishedLogs.get(0);
		 * 
		 * if (trackingLogPublished != null) {
		 * boolean sameStatus = trackingLogPublished.getStatus().equals(trackingLog.getStatus());
		 * super.state(sameStatus, "status", "assistanceAgent.tracking-log.form.error.different-status");
		 * }
		 * }
		 */
		//Condicion para que el tracking log esxtra tenga el mismo estado
		if (!super.getBuffer().getErrors().hasErrors("status"))
			if (Double.valueOf(100.0).equals(trackingLog.getResolutionPercentage())) {

				List<TrackingLog> publishedLogsAt100 = this.repository.findPublishedLogsAt100ByClaimId(claimId);

				TrackingLog firstPublished = publishedLogsAt100.isEmpty() ? null : publishedLogsAt100.get(0);

				if (firstPublished != null) {
					boolean sameStatus = firstPublished.getStatus().equals(trackingLog.getStatus());
					super.state(sameStatus, "status", "assistanceAgent.tracking-log.form.error.different-status");
				}
			}

		//condicion para publicar solo si el claim sesta publicado
		boolean status;

		status = claim != null && claim.isPublish();

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
