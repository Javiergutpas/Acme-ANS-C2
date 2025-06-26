
package acme.constraints;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.validation.AbstractValidator;
import acme.entities.passenger.Passenger;
import acme.entities.passenger.PassengerRepository;

public class PassengerValidator extends AbstractValidator<ValidPassenger, Passenger> {

	// Internal state ---------------------------------------------------------

	@Autowired
	PassengerRepository repository;

	// Initialiser ------------------------------------------------------------


	@Override
	public void initialise(final ValidPassenger annotation) {
		assert annotation != null;
	}

	// AbstractValidator interface --------------------------------------------

	@Override
	public boolean isValid(final Passenger passenger, final ConstraintValidatorContext context) {
		// HINT: value can be null
		assert context != null;

		boolean result;

		if (passenger == null || passenger.getPassportNumber() == null)
			super.state(context, false, "*", "javax.validation.constraints.NotNull.message");
		else {

			boolean uniquePassportNumber;
			Passenger existingPassenger;

			existingPassenger = this.repository.findPassengerByPassportNumber(passenger.getPassportNumber(), passenger.getCustomer().getId());
			uniquePassportNumber = existingPassenger == null || existingPassenger.equals(passenger);
			super.state(context, uniquePassportNumber, "passportNumber", "acme.validation.passenger.passportNumber.duplicated.message");

		}

		result = !super.hasErrors(context);
		return result;
	}
}
