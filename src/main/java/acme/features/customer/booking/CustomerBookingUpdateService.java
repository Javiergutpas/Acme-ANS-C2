
package acme.features.customer.booking;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.TypeTravelClass;
import acme.entities.flight.Flight;
import acme.entities.passenger.Passenger;
import acme.realms.customer.Customer;

@GuiService
public class CustomerBookingUpdateService extends AbstractGuiService<Customer, Booking> {

	//Internal state ---------------------------------------------

	@Autowired
	private CustomerBookingRepository repository;

	//AbstractGuiService interface -------------------------------


	@Override
	public void authorise() {
		Booking booking;
		int bookingId;
		int customerId;
		boolean status;

		bookingId = super.getRequest().getData("id", int.class);
		booking = this.repository.findBookingById(bookingId);
		customerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		status = booking != null && !booking.isPublish() && booking.getCustomer().getId() == customerId;

		if (status) {
			String method;

			method = super.getRequest().getMethod();

			if (method.equals("GET"))
				status = true;
			else {
				int flightId;
				Flight flight;
				String travelClass;
				boolean correctTravelClass;

				flightId = super.getRequest().getData("flight", int.class);
				flight = this.repository.findPublishedFlightById(flightId);

				travelClass = super.getRequest().getData("travelClass", String.class);
				correctTravelClass = "0".equals(travelClass) || Arrays.stream(TypeTravelClass.values()).map(TypeTravelClass::name).anyMatch(name -> name.equals(travelClass));

				status = (flightId == 0 || flight != null) && correctTravelClass;
			}
		}
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
		super.bindObject(booking, "flight", "locatorCode", "travelClass", "lastNibble"); //quito price
	}

	@Override
	public void validate(final Booking booking) {

	}

	@Override
	public void perform(final Booking booking) {
		booking.setPublish(false);
		this.repository.save(booking);
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
