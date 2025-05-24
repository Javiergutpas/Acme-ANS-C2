

<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<table class="table table-sm">
	<tr>
		<th scope="row">
			<acme:print code="flight-crew-member.dashboard.form.label.lastFiveDestinations"/>
		</th>
		<td>
			<acme:print value="${lastFiveDestinations}"/>
		</td>
	</tr>
	
	<tr>
		<th scope="row">
			<acme:print code="flight-crew-member.dashboard.form.label.legsWithLowSeverityIncidents"/>
		</th>
		<td>
			<acme:print value="${legsWithLowSeverityIncidents}"/>
		</td>
	</tr>
	
	<tr>
		<th scope="row">
			<acme:print code="flight-crew-member.dashboard.form.label.legsWithMediumSeverityIncidents"/>
		</th>
		<td>
			<acme:print value="${legsWithMediumSeverityIncidents}"/>
		</td>
	</tr>
	
	<tr>
		<th scope="row">
			<acme:print code="flight-crew-member.dashboard.form.label.legsWithHighSeverityIncidents"/>
		</th>
		<td>
			<acme:print value="${legsWithHighSeverityIncidents}"/>
		</td>
	</tr>
	
	<tr>
		<th scope="row">
			<acme:print code="flight-crew-member.dashboard.form.label.membersAssignedInLastLeg"/>
		</th>
		<td>
			<acme:print value="${membersAssignedInLastLeg}"/>
		</td>
	</tr>
	
	<tr>
            <th><acme:print code="flight-crew-member.flight-crew-member-dashboard.label.grouped-by-status" /></th>
            <td><acme:print value="${CONFIRMED}" /></td>
            <td><acme:print value="${PENDING}" /></td>
            <td><acme:print value="${CANCELLED}" /></td>
        </tr>
	
	<tr>
		<th scope="row">
			<acme:print code="flight-crew-member.dashboard.form.label.confirmedFlightAssignments"/>
		</th>
		<td>
			<acme:print value="${flightAssignmentsByStatus}"/>
		</td>
	</tr>
	
	<tr>
        <th scope="row">
            <acme:print code="flight-crew-member.dashboard.form.label.averageFlightAssignmentInLastMonth"/>
        </th>
        <td>
            <acme:print value="${averageFlightAssignmentInLastMonth}"/>
        </td>
    </tr>
    
    <tr>
        <th scope="row">
            <acme:print code="flight-crew-member.dashboard.form.label.minimumFlightAssignmentInLastMonth"/>
        </th>
        <td>
            <acme:print value="${minimumFlightAssignmentInLastMonth}"/>
        </td>
    </tr>
    
    <tr>
        <th scope="row">
            <acme:print code="flight-crew-member.dashboard.form.label.maximumFlightAssignmentInLastMonth"/>
        </th>
        <td>
            <acme:print value="${maximumFlightAssignmentInLastMonth}"/>
        </td>
    </tr>
    
     <tr>
        <th scope="row">
            <acme:print code="flight-crew-member.dashboard.form.label.standardDeviationInLastMonth"/>
        </th>
        <td>
            <acme:print value="${standardDeviationInLastMonth}"/>
        </td>
    </tr>
    
</table>


<acme:return/>