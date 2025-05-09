
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form readonly="true">
   
    <acme:input-textbox code="customer.dashboard.list.label.lastFiveDestinations" path="lastFiveDestinations"/>
    <acme:input-money code="customer.dashboard.list.label.spentBookingsMoney" path="spentBookingsMoney"/>
	<acme:input-integer code="customer.dashboard.list.label.economyBookings" path="economyBookings" />
	<acme:input-integer code="customer.dashboard.list.label.businessBookings" path="businessBookings" />
	<acme:input-money code="customer.dashboard.list.label.bookingsTotalCost" path="bookingsTotalCost" />	
	<acme:input-money code="customer.dashboard.list.label.bookingsAverageCost" path="bookingsAverageCost" />	
	<acme:input-money code="customer.dashboard.list.label.bookingsMinimumCost" path="bookingsMinimumCost" />	
	<acme:input-money code="customer.dashboard.list.label.bookingsMaximumCost" path="bookingsMaximumCost" />	
	<acme:input-money code="customer.dashboard.list.label.bookingsDeviationCost" path="bookingsDeviationCost" />	
	<acme:input-integer code="customer.dashboard.list.label.bookingsTotalPassengers" path="bookingsTotalPassengers" />	
	<acme:input-double code="customer.dashboard.list.label.bookingsAveragePassengers" path="bookingsAveragePassengers" />	
	<acme:input-integer code="customer.dashboard.list.label.bookingsMinimumPassengers" path="bookingsMinimumPassengers" />	
	<acme:input-integer code="customer.dashboard.list.label.bookingsMaximumPassengers" path="bookingsMaximumPassengers" />	
	<acme:input-double code="customer.dashboard.list.label.bookingsDeviationPassengers" path="bookingsDeviationPassengers" />	
		
	
</acme:form>