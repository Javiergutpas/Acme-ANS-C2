
package acme.entities.flight;

import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;

@Repository
public interface FlightRepository extends AbstractRepository {

	@Query("select count(l) from Leg l where l.flight.id = :flightId")
	Integer findNumberOfLegs(int flightId);

	@Query("select min(l.departure) from Leg l where l.flight.id = :flightId")
	Date findFlightScheduledDeparture(int flightId);

	@Query("select max(l.arrival) from Leg l where l.flight.id = :flightId")
	Date findFlightScheduledArrival(int flightId);

	@Query("select l.departureAirport.city from Leg l where l.flight.id = :flightId and l.departure = :departure")
	String findFlightOriginCity(int flightId, Date departure);

	@Query("select l.arrivalAirport.city from Leg l where l.flight.id = :flightId and l.arrival = :arrival")
	String findFlightDestinationCity(int flightId, Date arrival);

}
