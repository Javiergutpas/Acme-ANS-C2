
package acme.features.customer.booking;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.booking.TypeTravelClass;
import acme.entities.flight.Flight;
import acme.entities.passenger.Passenger;
import acme.realms.customer.Customer;

@GuiService
public class CustomerBookingDeleteService extends AbstractGuiService<Customer, Booking> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerBookingRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		int customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		int bookingId = super.getRequest().getData("id", int.class);
		Booking booking = this.repository.findBookingById(bookingId);

		boolean status = booking != null && !booking.isPublish() && booking.getCustomer().getId() == customerId;
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		int id = super.getRequest().getData("id", int.class);
		Booking booking = this.repository.findBookingById(id);

		super.getBuffer().addData(booking);
	}

	@Override
	public void bind(final Booking booking) {
		super.bindObject(booking, "flight", "locatorCode", "travelClass", "lastNibble", "flight");
	}

	@Override
	public void validate(final Booking booking) {

	}

	@Override
	public void perform(final Booking booking) {
		Collection<BookingRecord> passengersInBooking;
		passengersInBooking = this.repository.findAllBookingRecordByBookingId(booking.getId());

		this.repository.deleteAll(passengersInBooking);
		this.repository.delete(booking);
	}

	@Override
	public void unbind(final Booking booking) {
		Dataset dataset;
		SelectChoices typeTravelClasses;
		typeTravelClasses = SelectChoices.from(TypeTravelClass.class, booking.getTravelClass());
		Collection<Flight> publishFutureFlights = this.repository.findAllPublishFutureFlights(MomentHelper.getCurrentMoment());
		Collection<Passenger> passengersOnBooking = this.repository.findAllPassengersByBookingId(booking.getId());//añadido

		dataset = super.unbindObject(booking, "flight", "locatorCode", "purchaseMoment", "travelClass", "price", "lastNibble", "publish", "id");
		dataset.put("travelClasses", typeTravelClasses);

		SelectChoices flightChoices = SelectChoices.from(publishFutureFlights, "flightLabel", booking.getFlight());
		dataset.put("flights", flightChoices);

		super.getResponse().addGlobal("showDelete", !passengersOnBooking.isEmpty());//añadido
		super.getResponse().addData(dataset);
	}

}
