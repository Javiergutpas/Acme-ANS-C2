
package acme.features.manager.leg;

import java.util.Collection;
import java.util.Date;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.aircraft.Aircraft;
import acme.entities.airport.Airport;
import acme.entities.flight.Flight;
import acme.entities.leg.Leg;
import acme.realms.manager.AirlineManager;

@Repository
public interface AirlineManagerLegRepository extends AbstractRepository {

	@Query("select l from Leg l where l.flight.id = :flightId order by l.departure asc")
	Collection<Leg> findAllLegsByFlightId(int flightId);

	@Query("select m from AirlineManager m where m.id = :managerId")
	AirlineManager findManagerById(int managerId);

	@Query("select f from Flight f where f.id = :flightId")
	Flight findFlightById(int flightId);

	@Query("select l from Leg l where l.id = :legId")
	Leg findLegById(int legId);

	@Query("select a from Aircraft a")
	Collection<Aircraft> findAllAircrafts();

	@Query("select a from Aircraft a where a.id = :aircraftId")
	Aircraft findAircraftById(int aircraftId);

	@Query("select ap from Airport ap")
	Collection<Airport> findAllAirports();

	@Query("select ap from Airport ap where ap.id = :airportId")
	Airport findAirportById(int airportId);

	// Si bien not no es una funcion comot tal, si no un operador, los indices no ayudarán en gran medida a optimizar
	// Aun así, se definirán indices para ambas queries
	@Query("select count(l) from Leg l where l.publish = true and l.flight.id = :flightId and not (l.arrival < :departure or l.departure > :arrival)")
	Integer findNumberOfPublishedOverlappedLegs(Date departure, Date arrival, int flightId);

	@Query("select count(l) from Leg l where l.deployedAircraft.id = :aircraftId and l.publish = true and not (l.arrival < :departure or l.departure > :arrival)")
	Integer findNumberOfPublishedLegsDeployingSameAircraft(Date departure, Date arrival, Integer aircraftId);
}
