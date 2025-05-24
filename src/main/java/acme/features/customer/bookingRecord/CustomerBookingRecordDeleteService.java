
package acme.features.customer.bookingRecord;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.passenger.Passenger;
import acme.realms.customer.Customer;

@GuiService
public class CustomerBookingRecordDeleteService extends AbstractGuiService<Customer, BookingRecord> {

	//Internal state ---------------------------------------------

	@Autowired
	private CustomerBookingRecordRepository repository;

	//AbstractGuiService interface -------------------------------


	@Override
	public void authorise() {
		int bookingId = super.getRequest().getData("bookingId", int.class);
		Booking booking = this.repository.findBookingById(bookingId);

		super.getResponse().setAuthorised(super.getRequest().getPrincipal().hasRealm(booking.getCustomer()));
	}

	@Override
	public void load() {
		int bookingId = super.getRequest().getData("bookingId", int.class);
		Booking booking = this.repository.findBookingById(bookingId);
		BookingRecord bookingRecord = new BookingRecord();
		bookingRecord.setBooking(booking);

		super.getBuffer().addData(bookingRecord);
	}

	@Override
	public void bind(final BookingRecord bookingRecord) {
		super.bindObject(bookingRecord, "passenger");
	}

	@Override
	public void validate(final BookingRecord bookingRecord) {
		boolean notEmptyNullPassenger;
		notEmptyNullPassenger = bookingRecord.getPassenger() != null;
		super.state(notEmptyNullPassenger, "passenger", "javax.validation.constraints.NotNull.message");
	}

	@Override
	public void perform(final BookingRecord bookingRecord) {
		int passengerId = super.getRequest().getData("passenger", Passenger.class).getId();
		int bookingId = super.getRequest().getData("bookingId", int.class);
		this.repository.delete(this.repository.findBookingRecordByBookingAndPassenger(bookingId, passengerId));
	}

	@Override
	public void unbind(final BookingRecord bookingRecord) {
		Dataset dataset;
		dataset = super.unbindObject(bookingRecord, "passenger");
		int bookingId = super.getRequest().getData("bookingId", int.class);
		Collection<Passenger> passengersOnBooking = this.repository.findPassengersOfBooking(bookingId);
		SelectChoices passengerChoices = SelectChoices.from(passengersOnBooking, "id", bookingRecord.getPassenger());

		dataset.put("passengers", passengerChoices);
		dataset.put("bookingId", bookingId);

		super.getResponse().addData(dataset);
	}

}
