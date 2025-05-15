
package acme.features.manager.dashboard;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.client.repositories.AbstractRepository;
import acme.entities.airport.Airport;
import acme.entities.leg.LegStatus;

@Repository
public interface AirlineManagerDashboardRepository extends AbstractRepository {

	@Query("select count(m) + 1 from AirlineManager m where m.yearsOfExperience > (select m2.yearsOfExperience from AirlineManager m2 WHERE m2.id = :managerId)")
	Integer findRankingByExperience(int managerId);

	@Query("select 65 - YEAR(:currentMoment) + YEAR(m.dateOfBirth) from AirlineManager m where m.id = :managerId")
	Integer findYearsToRetire(int managerId, Date currentMoment);

	@Query("select count(l) * 1.0 / (select count(l2) from Leg l2 where l.flight.manager.id = :managerId) from Leg l where l.flight.manager.id = :managerId and l.status = :status")
	Double findRatioOfLegsByStatus(int managerId, LegStatus status);

	@Query("select a from Airport a join Leg l on l.departureAirport = a or l.arrivalAirport = a where l.flight.manager.id = :managerId group by a order by count(l) desc")
	Collection<Airport> findAirportsOrderedByPopularity(int managerId);

	@Query("select l.status, count(l) from Leg l where l.flight.manager.id = :managerId group by l.status")
	List<Object[]> findNumberOfLegsByStatus(int managerId);

	@Query("select avg(f.cost.amount) from Flight f where f.manager.id = :managerId")
	Double findAverageFlightCost(int managerId);

	@Query("select min(f.cost.amount) from Flight f where f.manager.id = :managerId")
	Double findMinFlightCost(int managerId);

	@Query("select max(f.cost.amount) from Flight f where f.manager.id = :managerId")
	Double findMaxFlightCost(int managerId);

	@Query("select stddev(f.cost.amount) from Flight f where f.manager.id = :managerId")
	Double findFlightCostStandardDeviation(int managerId);

	@Query("select count(l) from Leg l where l.flight.manager.id = :managerId")
	Integer findNumberOfLegsByManager(int managerId);

}
