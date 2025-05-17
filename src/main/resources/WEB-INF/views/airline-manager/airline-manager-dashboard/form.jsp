<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form readonly="true"> 

	<acme:input-integer code="airline-manager.dashboard.form.label.rankingByExperience" path="rankingByExperience"/>
	<acme:input-integer code="airline-manager.dashboard.form.label.yearsToRetire" path="yearsToRetire"/>
	<acme:input-double code="airline-manager.dashboard.form.label.ratioOfOnTimeLegs" path="ratioOfOnTimeLegs"/>
	<acme:input-double code="airline-manager.dashboard.form.label.ratioOfDelayedLegs" path="ratioOfDelayedLegs"/>
	<acme:input-textbox code="airline-manager.dashboard.form.label.mostPopularAirport" path="mostPopularAirport"/>
	<acme:input-textbox code="airline-manager.dashboard.form.label.leastPopularAirport" path="leastPopularAirport"/>
	<acme:input-integer code="airline-manager.dashboard.form.label.numberOfOnTimeLegs" path="numberOfOnTimeLegs"/>
	<acme:input-integer code="airline-manager.dashboard.form.label.numberOfDelayedLegs" path="numberOfDelayedLegs"/>
	<acme:input-integer code="airline-manager.dashboard.form.label.numberOfLandedLegs" path="numberOfLandedLegs"/>
	<acme:input-integer code="airline-manager.dashboard.form.label.numberOfCancelledLegs" path="numberOfCancelledLegs"/>
	<acme:input-money code="airline-manager.dashboard.form.label.averageFlightCost" path="averageFlightCost"/>
	<acme:input-money code="airline-manager.dashboard.form.label.minFlightCost" path="minFlightCost"/>
	<acme:input-money code="airline-manager.dashboard.form.label.maxFlightCost" path="maxFlightCost"/>
	<acme:input-money code="airline-manager.dashboard.form.label.flightCostStandardDeviation" path="flightCostStandardDeviation"/>
	
</acme:form>