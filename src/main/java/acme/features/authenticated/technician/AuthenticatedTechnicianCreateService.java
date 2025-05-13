
package acme.features.authenticated.technician;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.principals.Authenticated;
import acme.client.components.principals.UserAccount;
import acme.client.helpers.PrincipalHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.realms.technician.Technician;

@GuiService
public class AuthenticatedTechnicianCreateService extends AbstractGuiService<Authenticated, Technician> {

	// Dependency injection
	@Autowired
	private AuthenticatedTechnicianRepository repository;


	// Authorization logic - only non-technicians can create technician profiles
	@Override
	public void authorise() {
		boolean isNotTechnician = !super.getRequest().getPrincipal().hasRealmOfType(Technician.class);
		super.getResponse().setAuthorised(isNotTechnician);
	}

	// Initializes a new technician profile linked to user account
	@Override
	public void load() {
		Technician technician = new Technician();
		int accountId = super.getRequest().getPrincipal().getAccountId();
		UserAccount userAccount = this.repository.findUserAccountById(accountId);

		technician.setUserAccount(userAccount);
		super.getBuffer().addData(technician);
	}

	// Data binding from request parameters to object properties
	@Override
	public void bind(final Technician object) {
		assert object != null;

		super.bindObject(object, "licenseNumber", "phoneNumber", "specialisation", "anualHealthTest", "experienceYears", "certifications");
	}

	// Business rule validation for technician profile
	@Override
	public void validate(final Technician object) {
		// License format: 2-3 uppercase letters followed by 6 digits
		if (this.fieldValid("licenseNumber") && object.getLicenseNumber() != null) {
			boolean validFormat = object.getLicenseNumber().matches("^[A-Z]{2,3}\\d{6}$");
			super.state(validFormat, "licenseNumber", "technician.form.error.invalidLicenseNumber");
		}

		// International phone number validation (optional + prefix)
		if (this.fieldValid("phoneNumber") && object.getPhoneNumber() != null) {
			boolean validPhone = object.getPhoneNumber().matches("^\\+?\\d{6,15}$");
			super.state(validPhone, "phoneNumber", "technician.form.error.invalidPhoneNumber");
		}

		// Specialisation length constraint
		if (this.fieldValid("specialisation") && object.getSpecialisation() != null) {
			boolean validLength = object.getSpecialisation().length() <= 50;
			super.state(validLength, "specialisation", "technician.form.error.invalidSpecialisation");
		}

		// Mandatory health test verification
		if (this.fieldValid("anualHealthTest"))
			super.state(object.getAnualHealthTest() != null, "anualHealthTest", "technician.form.error.invalidPassedHealthTest");

		// Experience range validation (0-120 years)
		if (this.fieldValid("experienceYears") && object.getExperienceYears() != null) {
			boolean validExperience = object.getExperienceYears() >= 0 && object.getExperienceYears() <= 120;
			super.state(validExperience, "experienceYears", "technician.form.error.invalidYearsOfExperience");
		}

		// Certifications length constraint
		if (this.fieldValid("certifications") && object.getCertifications() != null) {
			boolean validLength = object.getCertifications().length() <= 255;
			super.state(validLength, "certifications", "technician.form.error.invalidCertifications");
		}
	}

	// Persistence handling - save new technician profile
	@Override
	public void perform(final Technician object) {
		this.repository.save(object);
	}

	// Data preparation for view rendering
	@Override
	public void unbind(final Technician object) {
		Dataset dataset = super.unbindObject(object, "licenseNumber", "phoneNumber", "specialisation", "anualHealthTest", "experienceYears", "certifications");
		super.getResponse().addData(dataset);
	}

	// Post-creation handling - update user principal
	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals("POST"))
			PrincipalHelper.handleUpdate();
	}

	// Helper method for error checking
	private boolean fieldValid(final String field) {
		return !this.getBuffer().getErrors().hasErrors(field);
	}
}
