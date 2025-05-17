
package acme.features.assistanceagent.claim;

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

		//no estoy seguro aqui aun, puede ser el comentado
		//super.getResponse().setAuthorised(true);

		boolean status = super.getRequest().getPrincipal().hasRealmOfType(AssistanceAgent.class);
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
		super.bindObject(claim, "registrationMoment", "passengerEmail", "description", "type", "leg");

	}

	@Override
	public void validate(final Claim claim) {

		if (!super.getBuffer().getErrors().hasErrors("registrationMoment"))
			super.state(claim.getLeg().getArrival().before(claim.getRegistrationMoment()), "registrationMoment", "assistanceAgent.claim.form.error.registration-before-leg");
	}

	@Override
	public void perform(final Claim claim) {

		this.repository.save(claim);
	}
	//CUANDO SE ARREGLE EL BUG TEMPORAL DE LAS LEGS SE USARA LA LINEA COMENTADA
	//AQUI FUNCIONA EL ALLPUBLISED LEGS PERO EN LOS OTROS NO
	@Override
	public void unbind(final Claim claim) {

		Dataset dataset;
		SelectChoices typesChoices;
		SelectChoices legsChoices;

		Collection<Leg> legs;

		Date now = MomentHelper.getCurrentMoment();

		typesChoices = SelectChoices.from(ClaimType.class, claim.getType());
		legs = this.repository.findAllPublishedLegsBefore(now);
		//legs = this.repository.findAllLeg();
		//legs = this.repository.findAllPublishedLegs();
		legsChoices = SelectChoices.from(legs, "flightNumber", claim.getLeg());

		dataset = super.unbindObject(claim, "registrationMoment", "passengerEmail", "description", "type", "leg");
		dataset.put("readonly", false);
		dataset.put("types", typesChoices);
		dataset.put("legs", legsChoices);

		super.getResponse().addData(dataset);

	}
}
