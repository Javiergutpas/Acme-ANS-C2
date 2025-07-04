
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="acme" uri="http://acme-framework.org/"%>

<acme:form>
    <acme:input-select code="customer.booking.list.label.flight" path="flight" choices="${flights}"/>
    <acme:input-textbox code="customer.booking.list.label.locatorCode" path="locatorCode"/>
    <acme:input-textbox code="customer.booking.list.label.purchaseMoment" path="purchaseMoment" readonly="true"/>
    <acme:input-textbox code="customer.booking.list.label.lastNibble" path="lastNibble"/>
    <acme:input-select code="customer.booking.list.label.travelClass" path="travelClass" choices="${travelClasses}"/>
    <acme:input-double code="customer.booking.list.label.price" path="price" readonly="true"/>
    
    <jstl:choose>
		   <jstl:when test="${acme:anyOf(_command,'show|update|delete|publish') && publish == false}">
				<acme:submit code="customer.booking.list.button.update" action="/customer/booking/update"/>
				<acme:submit code="customer.booking.form.button.publish" action="/customer/booking/publish"/>
				<acme:submit code="customer.booking.form.button.delete" action="/customer/booking/delete"/>
				
				<jstl:if test="${_command != 'create'}">
 					<acme:button code="customer.booking.form.add.passenger" action="/customer/booking-record/create?bookingId=${id}"/>
 					<jstl:if test="${showDelete}">
 						<acme:button code="customer.booking.form.delete.passenger" action="/customer/booking-record/delete?bookingId=${id}"/>
 					</jstl:if>
 				
 				</jstl:if>
		</jstl:when>
		
     	<jstl:when test="${acme:anyOf(_command,'create') }">
			<acme:submit code="customer.booking.list.button.create" action="/customer/booking/create"/>
		</jstl:when>
		
			
    </jstl:choose>
    
    <jstl:if test="${_command != 'create'}">
    	<acme:button code="customer.booking.form.show.passengers" action="/customer/passenger/bookingRecord-list?bookingId=${id}"/>
    </jstl:if>
    
</acme:form>