
package acme.features.assistanceagent.claim;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.claims.Claim;
import acme.entities.claims.ClaimType;
import acme.entities.leg.Leg;
import acme.realms.assistanceagent.AssistanceAgent;

@GuiService
public class AssistanceAgentClaimCreateService extends AbstractGuiService<AssistanceAgent, Claim> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AssistanceAgentClaimRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {

		boolean status;

		String method;
		Leg leg;
		int legId;

		method = super.getRequest().getMethod();

		if (method.equals("GET"))
			status = true;
		else {
			String claimType;
			boolean correctClaimType;
			legId = super.getRequest().getData("leg", int.class);
			leg = this.repository.findPublishedLegById(legId);
			claimType = super.getRequest().getData("type", String.class);

			int version;
			int id;

			version = super.getRequest().getData("version", int.class);
			id = super.getRequest().getData("id", int.class);

			correctClaimType = "0".equals(claimType) || Arrays.stream(ClaimType.values()).map(ClaimType::name).anyMatch(name -> name.equals(claimType));
			status = (legId == 0 || leg != null) && correctClaimType && id == 0 && version == 0;
		}
		super.getResponse().setAuthorised(status);

	}

	@Override
	public void load() {
		Claim claim;
		AssistanceAgent agent = (AssistanceAgent) super.getRequest().getPrincipal().getActiveRealm();

		//El momento cogeremos el actual ficticio
		Date registrationMoment = MomentHelper.getCurrentMoment();

		claim = new Claim();
		claim.setRegistrationMoment(registrationMoment);

		claim.setAssistanceAgent(agent);

		claim.setPublish(false);

		super.getBuffer().addData(claim);

	}

	@Override
	public void bind(final Claim claim) {
		super.bindObject(claim, "passengerEmail", "description", "type", "leg");

	}

	@Override
	public void validate(final Claim claim) {
		/*
		 * if (claim.getLeg() != null)
		 * if (!super.getBuffer().getErrors().hasErrors("registrationMoment"))
		 * super.state(claim.getLeg().getArrival().before(claim.getRegistrationMoment()), "registrationMoment", "assistanceAgent.claim.form.error.registration-before-leg");
		 * 
		 * if (!super.getBuffer().getErrors().hasErrors("leg"))
		 * super.state(claim.getLeg() != null && claim.getLeg().isPublish(), "leg", "assistanceAgent.claim.form.error.leg-null");
		 */
		;
	}

	@Override
	public void perform(final Claim claim) {

		this.repository.save(claim);
	}

	@Override
	public void unbind(final Claim claim) {

		Dataset dataset;
		SelectChoices typesChoices;
		SelectChoices legsChoices;

		Collection<Leg> legs;

		Date now = MomentHelper.getCurrentMoment();

		typesChoices = SelectChoices.from(ClaimType.class, claim.getType());
		legs = this.repository.findAllPublishedLegsBefore(now);

		legsChoices = SelectChoices.from(legs, "flightNumber", claim.getLeg());

		dataset = super.unbindObject(claim, "registrationMoment", "passengerEmail", "description", "type", "leg");
		dataset.put("readonly", false);
		dataset.put("types", typesChoices);
		dataset.put("legs", legsChoices);

		super.getResponse().addData(dataset);

	}
}
