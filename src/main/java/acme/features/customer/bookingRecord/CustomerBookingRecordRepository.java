
package acme.features.customer.bookingRecord;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;

import acme.client.repositories.AbstractRepository;
import acme.entities.booking.Booking;
import acme.entities.booking.BookingRecord;
import acme.entities.passenger.Passenger;

public interface CustomerBookingRecordRepository extends AbstractRepository {

	@Query("select b from Booking b where b.id=:bookingId")
	Booking findBookingById(int bookingId);

	@Query("select p from Passenger p where p.id=:passengerId")
	Passenger findPassengerById(int passengerId);

	@Query("select br.passenger from BookingRecord br where br.booking.id=:bookingId")
	Collection<Passenger> findPassengersOfBooking(int bookingId);

	@Query("select p from Passenger p where p.customer.id=:customerId")
	Collection<Passenger> findAllPassengersByCustomer(int customerId);

	@Query("select br from BookingRecord br where br.id=:bookingRecordId")
	BookingRecord findBookingRecordById(int bookingRecordId);

	@Query("select br from BookingRecord  br where br.booking.id = :bookingId AND br.passenger.id = :passengerId")
	BookingRecord findBookingRecordByBookingAndPassenger(int bookingId, int passengerId);

	@Query("select count(br) from BookingRecord  br where br.booking.id = :bookingId AND br.passenger.id = :passengerId")
	Integer countBookingRecordByBookingIdAndPassenger(int bookingId, int passengerId);

}
