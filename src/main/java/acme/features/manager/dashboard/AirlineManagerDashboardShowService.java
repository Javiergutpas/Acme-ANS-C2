
package acme.features.manager.dashboard;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.datatypes.Money;
import acme.client.components.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.airport.Airport;
import acme.entities.leg.LegStatus;
import acme.forms.AirlineManagerDashboard;
import acme.realms.manager.AirlineManager;

@GuiService
public class AirlineManagerDashboardShowService extends AbstractGuiService<AirlineManager, AirlineManagerDashboard> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AirlineManagerDashboardRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {

		int managerId;
		AirlineManagerDashboard dashboard;

		Integer rankingByExperience;
		Integer yearsToRetire;
		double ratioOfOnTimeLegs;
		double ratioOfDelayedLegs;
		String mostPopularAirport;
		String leastPopularAirport;
		Integer numberOfOnTimeLegs;
		Integer numberOfDelayedLegs;
		Integer numberOfLandedLegs;
		Integer numberOfCancelledLegs;
		Money averageFlightCost = new Money();
		Money minFlightCost = new Money();
		Money maxFlightCost = new Money();
		Money flightCostStandardDeviation = new Money();

		managerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		dashboard = new AirlineManagerDashboard();

		rankingByExperience = this.repository.findRankingByExperience(managerId);
		yearsToRetire = this.repository.findYearsToRetire(managerId, MomentHelper.getCurrentMoment());

		Integer numberOfLegs = this.repository.findNumberOfLegsByManager(managerId);

		//Si no tiene legs el manager, el ratio va a ser 0, asi que lo asigno directamente para evitar nulls
		ratioOfOnTimeLegs = numberOfLegs == 0 ? 0 : this.repository.findRatioOfLegsByStatus(managerId, LegStatus.ON_TIME);
		ratioOfDelayedLegs = numberOfLegs == 0 ? 0 : this.repository.findRatioOfLegsByStatus(managerId, LegStatus.DELAYED);

		List<Airport> airports;

		airports = (List<Airport>) this.repository.findAirportsOrderedByPopularity(managerId);

		mostPopularAirport = airports.isEmpty() ? null : airports.get(0).getIataCode();
		leastPopularAirport = airports.isEmpty() ? null : airports.get(airports.size() - 1).getIataCode();

		Map<LegStatus, Integer> numberOfLegsByStatus = this.repository.findNumberOfLegsByStatus(managerId).stream().collect(Collectors.toMap(row -> (LegStatus) row[0], row -> ((Long) row[1]).intValue()));

		numberOfOnTimeLegs = numberOfLegsByStatus.getOrDefault(LegStatus.ON_TIME, 0);
		numberOfDelayedLegs = numberOfLegsByStatus.getOrDefault(LegStatus.DELAYED, 0);
		numberOfLandedLegs = numberOfLegsByStatus.getOrDefault(LegStatus.LANDED, 0);
		numberOfCancelledLegs = numberOfLegsByStatus.getOrDefault(LegStatus.CANCELLED, 0);

		averageFlightCost.setCurrency("EUR"); //Pongo euros por defecto
		minFlightCost.setCurrency("EUR");
		maxFlightCost.setCurrency("EUR");
		flightCostStandardDeviation.setCurrency("EUR");

		Double avg = this.repository.findAverageFlightCost(managerId);
		Double min = this.repository.findMinFlightCost(managerId);
		Double max = this.repository.findMaxFlightCost(managerId);
		Double stddev = this.repository.findFlightCostStandardDeviation(managerId);

		//Si no tienen vuelos, dejos las estadisticas a 0, en lugar de a null
		averageFlightCost.setAmount(avg == null ? 0.0 : avg);
		minFlightCost.setAmount(min == null ? 0.0 : min);
		maxFlightCost.setAmount(max == null ? 0.0 : max);
		flightCostStandardDeviation.setAmount(stddev == null ? 0.0 : stddev);

		dashboard.setRankingByExperience(rankingByExperience);
		dashboard.setYearsToRetire(yearsToRetire);
		dashboard.setRatioOfOnTimeLegs(ratioOfOnTimeLegs);
		dashboard.setRatioOfDelayedLegs(ratioOfDelayedLegs);
		dashboard.setMostPopularAirport(mostPopularAirport);
		dashboard.setLeastPopularAirport(leastPopularAirport);
		dashboard.setNumberOfOnTimeLegs(numberOfOnTimeLegs);
		dashboard.setNumberOfDelayedLegs(numberOfDelayedLegs);
		dashboard.setNumberOfLandedLegs(numberOfLandedLegs);
		dashboard.setNumberOfCancelledLegs(numberOfCancelledLegs);
		dashboard.setAverageFlightCost(averageFlightCost);
		dashboard.setMinFlightCost(minFlightCost);
		dashboard.setMaxFlightCost(maxFlightCost);
		dashboard.setFlightCostStandardDeviation(flightCostStandardDeviation);

		super.getBuffer().addData(dashboard);
	}

	@Override
	public void unbind(final AirlineManagerDashboard dashboard) {
		Dataset dataset;

		dataset = super.unbindObject(dashboard, "rankingByExperience", "yearsToRetire", "ratioOfOnTimeLegs", "ratioOfDelayedLegs", "mostPopularAirport", "leastPopularAirport", "numberOfOnTimeLegs", "numberOfDelayedLegs", "numberOfLandedLegs",
			"numberOfCancelledLegs", "averageFlightCost", "minFlightCost", "maxFlightCost", "flightCostStandardDeviation");

		super.getResponse().addData(dataset);
	}
}
