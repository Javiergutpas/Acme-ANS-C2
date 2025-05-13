
package acme.features.authenticated.technician;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Authenticated;
import acme.client.helpers.PrincipalHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.realms.technician.Technician;

@GuiService
public class AuthenticatedTechnicianUpdateService extends AbstractGuiService<Authenticated, Technician> {

	// Dependency injection
	@Autowired
	private AuthenticatedTechnicianRepository repository;


	// Authorization logic - ensures user is a technician
	@Override
	public void authorise() {
		boolean isTechnician = super.getRequest().getPrincipal().hasRealmOfType(Technician.class);
		super.getResponse().setAuthorised(isTechnician);
	}

	// Data loading - retrieves current technician's profile
	@Override
	public void load() {
		Technician technician;
		int userAccountId = super.getRequest().getPrincipal().getAccountId();
		technician = this.repository.findTechnicianByUserAccountId(userAccountId);
		super.getBuffer().addData(technician);
	}

	// Data binding - maps HTTP request parameters to object properties
	@Override
	public void bind(final Technician object) {
		super.bindObject(object, "licenseNumber", "phoneNumber", "specialisation", "anualHealthTest", "experienceYears", "certifications");
	}

	// Validation rules - enforces business constraints
	@Override
	public void validate(final Technician object) {
		// License number format validation (2-3 letters + 6 digits)
		if (!this.getBuffer().getErrors().hasErrors("licenseNumber") && object.getLicenseNumber() != null) {
			boolean isValidLicense = object.getLicenseNumber().matches("^[A-Z]{2,3}\\d{6}$");
			super.state(isValidLicense, "licenseNumber", "technician.form.error.invalidLicenseNumber");
		}

		// International phone number validation
		if (!this.getBuffer().getErrors().hasErrors("phoneNumber") && object.getPhoneNumber() != null) {
			boolean isValidPhone = object.getPhoneNumber().matches("^\\+?\\d{6,15}$");
			super.state(isValidPhone, "phoneNumber", "technician.form.error.invalidPhoneNumber");
		}

		// Specialisation length constraint
		if (!this.getBuffer().getErrors().hasErrors("specialisation") && object.getSpecialisation() != null) {
			boolean validSpecialisation = object.getSpecialisation().length() <= 50;
			super.state(validSpecialisation, "specialisation", "technician.form.error.invalidSpecialisation");
		}

		// Mandatory health test verification
		if (!this.getBuffer().getErrors().hasErrors("anualHealthTest"))
			super.state(object.getAnualHealthTest() != null, "anualHealthTest", "technician.form.error.invalidPassedHealthTest");

		// Experience range validation (0-120 years)
		if (!this.getBuffer().getErrors().hasErrors("experienceYears") && object.getExperienceYears() != null) {
			boolean validExperience = object.getExperienceYears() >= 0 && object.getExperienceYears() <= 120;
			super.state(validExperience, "experienceYears", "technician.form.error.invalidYearsOfExperience");
		}

		// Certifications length constraint
		if (!this.getBuffer().getErrors().hasErrors("certifications") && object.getCertifications() != null) {
			boolean validCerts = object.getCertifications().length() <= 255;
			super.state(validCerts, "certifications", "technician.form.error.invalidCertifications");
		}
	}

	// Persistence handling - saves updated technician profile
	@Override
	public void perform(final Technician object) {
		this.repository.save(object);
	}

	// Data unbinding - prepares object for view rendering
	@Override
	public void unbind(final Technician object) {
		Dataset dataset = super.unbindObject(object, "licenseNumber", "phoneNumber", "specialisation", "anualHealthTest", "experienceYears", "certifications");
		super.getResponse().addData(dataset);
	}

	// Post-success handling - updates principal information
	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals("POST"))
			PrincipalHelper.handleUpdate();
	}
}
