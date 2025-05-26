
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
import acme.realms.customer.Customer;

@GuiService
public class CustomerBookingCreateService extends AbstractGuiService<Customer, Booking> {

	//Internal state ---------------------------------------------

	@Autowired
	private CustomerBookingRepository repository;

	//AbstractGuiService interface -------------------------------


	@Override
	public void authorise() {
		String method;
		boolean status;

		method = super.getRequest().getMethod();

		if (method.equals("GET"))
			status = true;
		else {
			int id;
			int version;
			int flightId;
			Flight flight;
			String travelClass;
			boolean correctTravelClass;

			id = super.getRequest().getData("id", int.class);
			version = super.getRequest().getData("version", int.class);
			flightId = super.getRequest().getData("flight", int.class);
			flight = this.repository.findPublishedFlightById(flightId);

			travelClass = super.getRequest().getData("travelClass", String.class);
			correctTravelClass = "0".equals(travelClass) || Arrays.stream(TypeTravelClass.values()).map(TypeTravelClass::name).anyMatch(name -> name.equals(travelClass));

			status = (flightId == 0 || flight != null) && id == 0 && version == 0 && correctTravelClass;
		}

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Customer customer = (Customer) super.getRequest().getPrincipal().getActiveRealm();
		Booking booking;

		booking = new Booking();
		booking.setPurchaseMoment(MomentHelper.getCurrentMoment());
		booking.setPublish(false);
		booking.setCustomer(customer);

		super.getBuffer().addData(booking);
	}

	@Override
	public void bind(final Booking booking) {
		super.bindObject(booking, "flight", "locatorCode", "travelClass", "lastNibble"); //quito price porque es readOnly
	}

	@Override
	public void validate(final Booking booking) {

	}

	@Override
	public void perform(final Booking booking) {
		this.repository.save(booking);
	}

	@Override
	public void unbind(final Booking booking) {
		Dataset dataset;
		SelectChoices travelClasses = SelectChoices.from(TypeTravelClass.class, booking.getTravelClass());
		Collection<Flight> publishFutureFlights = this.repository.findAllPublishFutureFlights(MomentHelper.getCurrentMoment());
		SelectChoices flightChoices = SelectChoices.from(publishFutureFlights, "flightLabel", booking.getFlight());

		dataset = super.unbindObject(booking, "flight", "locatorCode", "travelClass", "lastNibble", "publish", "id");
		dataset.put("travelClasses", travelClasses);
		dataset.put("flights", flightChoices);

		super.getResponse().addData(dataset);

	}
}
