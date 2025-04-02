

<%@page%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:list>
	<acme:list-column code ="airline-manager.leg.list.label.departure" path ="departure" width="20%" sortable="true"/>
	<acme:list-column code ="airline-manager.leg.list.label.arrival" path ="arrival" width="20%" sortable="true"/>
	<acme:list-column code ="airline-manager.leg.list.label.flightNumber" path ="flightNumber" width ="20%" sortable="false"/>
	<acme:list-column code ="airline-manager.leg.list.label.status" path ="status" width ="20%" sortable="false"/>
	<acme:list-column code ="airline-manager.leg.list.label.publish" path ="publish" width ="20%" sortable="false"/>
	<acme:list-payload path="payload"/>	
</acme:list>

<jstl:if test="${showCreate}">
	<acme:button code="airline-manager.flight.list.button.create" action ="/airline-manager/leg/create?flightId=${flightId}"/>
</jstl:if>