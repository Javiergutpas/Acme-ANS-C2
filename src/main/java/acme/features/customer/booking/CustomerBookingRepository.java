
package acme.features.customer.booking;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.flight.Flight;
import acme.entities.passenger.Passenger;

@Repository
public interface CustomerBookingRepository extends AbstractRepository {

	@Query("select b from Booking b where b.customer.id =:customerId")
	Collection<Booking> findBookingsByCustomerId(int customerId);

	@Query("select b from Booking b where b.id = :id")
	Booking findBookingById(int id);

	@Query("select f from Flight f where f.publish = true")
	Collection<Flight> findAllPublishFlights();

	@Query("select distinct(l.flight) from Leg l where l.flight.publish = true and l.departure >= :departure and l.departure = (select min(l2.departure) from Leg l2 where l2.flight.id = l.flight.id)")
	Collection<Flight> findAllPublishFutureFlights(Date departure);

	@Query("select f from Flight f")
	Collection<Flight> findAllFlights();

	@Query("select b from Booking b where b.locatorCode= :locatorCode")
	Booking findBookingByLocatorCode(String locatorCode);

	@Query("select br.passenger from BookingRecord br where br.booking.id = :bookingId")
	Collection<Passenger> findAllPassengersByBookingId(int bookingId);

	@Query("select br from BookingRecord br where br.booking.id = :bookingId")
	Collection<BookingRecord> findAllBookingRecordByBookingId(int bookingId);

	@Query("select min(l.departure) from Leg l where l.flight.id = :flightId")
	Date findFlightScheduledDeparture(int flightId);

	@Query("select f from Flight f where f.id = :flightId")
	Flight findFlightById(int flightId);

	@Query("select f from Flight f where f.id = :flightId and f.publish = true")
	Flight findPublishedFlightById(int flightId);

}
