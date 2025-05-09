
package acme.features.customer.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.datatypes.Money;
import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.booking.TypeTravelClass;
import acme.forms.CustomerDashboard;
import acme.realms.customer.Customer;

@GuiService
public class CustomerDashboardService extends AbstractGuiService<Customer, CustomerDashboard> {
	// Internal state ---------------------------------------------------------

	@Autowired
	private CustomerDashboardRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		boolean status = super.getRequest().getPrincipal().hasRealmOfType(Customer.class);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {

		int customerId = this.getRequest().getPrincipal().getActiveRealm().getId();
		Collection<Booking> bookings = this.repository.findAllPublishedBookingsByCustomerId(customerId);
		Collection<BookingRecord> bookingRecords = this.repository.findAllBookingRecordsByCustomerId(customerId);
		List<String> currencies = bookings.stream().map(b -> b.getPrice().getCurrency()).distinct().toList();

		CustomerDashboard dashboard = new CustomerDashboard();
		dashboard.setLastFiveDestinations(List.of());
		dashboard.setSpentBookingsMoney(new LinkedList<>());
		dashboard.setEconomyBookings(0);
		dashboard.setBusinessBookings(0);
		dashboard.setBookingsTotalCost(new LinkedList<>());
		dashboard.setBookingsAverageCost(new LinkedList<>());
		dashboard.setBookingsMinimumCost(new LinkedList<>());
		dashboard.setBookingsMaximumCost(new LinkedList<>());
		dashboard.setBookingsDeviationCost(new LinkedList<>());
		dashboard.setBookingsTotalPassengers(0);
		dashboard.setBookingsAveragePassengers(Double.NaN);
		dashboard.setBookingsMinimumPassengers(0);
		dashboard.setBookingsMaximumPassengers(0);
		dashboard.setBookingsDeviationPassengers(Double.NaN);

		if (!bookings.isEmpty() && !bookingRecords.isEmpty()) {
			int currentYear = MomentHelper.getCurrentMoment().getYear();
			List<Booking> lastFiveYearsBookings = bookings.stream().filter(booking -> booking.getPurchaseMoment().getYear() > currentYear - 5).toList();
			Collection<String> lastFiveDestinations = bookings.stream().sorted(Comparator.comparing(Booking::getPurchaseMoment).reversed()).map(b -> b.getFlight().getDestinationCity()).distinct().limit(5).toList();
			dashboard.setLastFiveDestinations(lastFiveDestinations);

			for (String currency : currencies) {
				Double totalMoney = bookings.stream().filter(booking -> booking.getPurchaseMoment().getYear() > currentYear - 1).filter(booking -> booking.getPrice().getCurrency().equals(currency)).map(Booking::getPrice).map(Money::getAmount).reduce(0.0,
					Double::sum);

				Money spentMoney = new Money();
				spentMoney.setAmount(totalMoney);
				spentMoney.setCurrency(currency);
				List<Money> spentBookingsMoney = new ArrayList<>(dashboard.getSpentBookingsMoney());
				spentBookingsMoney.add(spentMoney);
				dashboard.setSpentBookingsMoney(spentBookingsMoney);
			}

			long economyBookings = bookings.stream().filter(b -> b.getTravelClass().equals(TypeTravelClass.ECONOMY)).count();
			dashboard.setEconomyBookings(economyBookings);

			long businessBookings = bookings.stream().filter(b -> b.getTravelClass().equals(TypeTravelClass.BUSINESS)).count();
			dashboard.setBusinessBookings(businessBookings);

			for (String currency : currencies) {
				Money bookingsTotalCost = new Money();
				bookingsTotalCost.setAmount(lastFiveYearsBookings.stream().filter(booking -> booking.getPrice().getCurrency().equals(currency)).map(Booking::getPrice).map(Money::getAmount).reduce(0.0, Double::sum));
				bookingsTotalCost.setCurrency(currency);
				List<Money> bookingTotalCostRes = new ArrayList<>(dashboard.getBookingsTotalCost());
				bookingTotalCostRes.add(bookingsTotalCost);
				dashboard.setBookingsTotalCost(bookingTotalCostRes);
			}

			for (String currency : currencies) {
				Money bookingAverageCost = new Money();
				Money bookingTotalCost = dashboard.getBookingsTotalCost().stream().filter(b -> b.getCurrency().equals(currency)).findFirst().get();
				long totalFiveYearsBookingCurrency = lastFiveYearsBookings.stream().filter(b -> b.getPrice().getCurrency().equals(currency)).count();
				bookingAverageCost.setAmount(Double.NaN);

				if (totalFiveYearsBookingCurrency > 0)
					bookingAverageCost.setAmount(bookingTotalCost.getAmount() / totalFiveYearsBookingCurrency);

				bookingAverageCost.setCurrency(currency);
				List<Money> bookingAverageCostRes = new ArrayList<>(dashboard.getBookingsAverageCost());
				bookingAverageCostRes.add(bookingAverageCost);
				dashboard.setBookingsAverageCost(bookingAverageCostRes);
			}

			for (String currency : currencies) {
				Money bookingMinimumCost = new Money();
				bookingMinimumCost.setAmount(lastFiveYearsBookings.stream().map(Booking::getPrice).filter(p -> p.getCurrency().equals(currency)).map(Money::getAmount).min(Double::compare).orElse(0.0));
				bookingMinimumCost.setCurrency(currency);
				List<Money> bookingMinimumCostRes = new ArrayList<>(dashboard.getBookingsMinimumCost());
				bookingMinimumCostRes.add(bookingMinimumCost);
				dashboard.setBookingsMinimumCost(bookingMinimumCostRes);
			}

			for (String currency : currencies) {
				Money bookingMaximumCost = new Money();
				bookingMaximumCost.setAmount(lastFiveYearsBookings.stream().map(Booking::getPrice).filter(p -> p.getCurrency().equals(currency)).map(Money::getAmount).max(Double::compare).orElse(0.0));
				bookingMaximumCost.setCurrency(currency);
				List<Money> bookingMaximumCostRes = new ArrayList<>(dashboard.getBookingsMaximumCost());
				bookingMaximumCostRes.add(bookingMaximumCost);
				dashboard.setBookingsMaximumCost(bookingMaximumCostRes);
			}

			for (String currency : currencies) {
				Money bookingDeviationCost = new Money();
				Money bookingAverageCost = dashboard.getBookingsAverageCost().stream().filter(p -> p.getCurrency().equals(currency)).findFirst().get();
				long totalFiveYearsBookingCurrency = lastFiveYearsBookings.stream().filter(b -> b.getPrice().getCurrency().equals(currency)).count();
				bookingDeviationCost.setAmount(Double.NaN);

				if (totalFiveYearsBookingCurrency > 0) {
					double variance = lastFiveYearsBookings.stream().map(Booking::getPrice).filter(p -> p.getCurrency().equals(currency)).map(Money::getAmount).map(price -> Math.pow(price - bookingAverageCost.getAmount(), 2)).reduce(0.0, Double::sum)
						/ totalFiveYearsBookingCurrency;
					double deviation = Math.sqrt(variance);
					bookingDeviationCost.setAmount(deviation);
				}
				bookingDeviationCost.setCurrency(currency);
				List<Money> bookingDeviationCostRes = new ArrayList<>(dashboard.getBookingsDeviationCost());
				bookingDeviationCostRes.add(bookingDeviationCost);
				dashboard.setBookingsDeviationCost(bookingDeviationCostRes);
			}

			long passengerCount = bookingRecords.stream().map(BookingRecord::getPassenger).count();
			dashboard.setBookingsTotalPassengers(passengerCount);
			int totalBookings = bookings.size();
			double passengerAverage = (double) passengerCount / totalBookings;
			dashboard.setBookingsAveragePassengers(passengerAverage);
			Map<Booking, Long> bookingPassengers = bookingRecords.stream().collect(Collectors.groupingBy(BookingRecord::getBooking, Collectors.counting()));

			int minimumPassengers = bookingPassengers.isEmpty() ? 0 : Collections.min(bookingPassengers.values()).intValue();
			dashboard.setBookingsMinimumPassengers(minimumPassengers);

			int maximumPassengers = bookingPassengers.isEmpty() ? 0 : Collections.max(bookingPassengers.values()).intValue();
			dashboard.setBookingsMaximumPassengers(maximumPassengers);

			double variancePassengers = bookingPassengers.values().stream().mapToDouble(count -> Math.pow(count - passengerAverage, 2)).sum() / (totalBookings - 1);
			double standardDeviationPassengers = Math.sqrt(variancePassengers);
			dashboard.setBookingsDeviationPassengers(standardDeviationPassengers);
		}

		super.getBuffer().addData(dashboard);
	}

	@Override
	public void unbind(final CustomerDashboard object) {
		Dataset dataset;

		dataset = super.unbindObject(object, "lastFiveDestinations", "spentBookingsMoney", "economyBookings", "businessBookings", "bookingsTotalCost", "bookingsAverageCost", "bookingsMinimumCost", "bookingsMaximumCost", "bookingsDeviationCost",
			"bookingsTotalPassengers", "bookingsAveragePassengers", "bookingsMinimumPassengers", "bookingsMaximumPassengers", "bookingsDeviationPassengers");

		super.getResponse().addData(dataset);
	}
}
