<%@page language="java"%>
<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="acme" uri="http://acme-framework.org/" %>

<table class="table table-sm">
	<tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.topFiveAircrafts"/>
        </th>
        <td>
            <acme:print value="${topFiveAircrafts}"/>
        </td>
    </tr>
    <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.nearestInspectionDue"/>
        </th>
        <td>
            <acme:print value="${nearestInspectionMaintenanceRecord}"/>
        </td>
    </tr>
    <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.averageEstimatedCost"/>
        </th>
        <td>
            <acme:print value="${averageEstimatedCost}"/>
        </td>
    </tr>
    <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.minEstimatedCost"/>
        </th>
        <td>
            <acme:print value="${minEstimatedCost}"/>
        </td>
    </tr>
    <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.maxEstimatedCost"/>
        </th>
        <td>
            <acme:print value="${maxEstimatedCost}"/>
        </td>
    </tr>
        <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.deviationEstimatedCost"/>
        </th>
        <td>
            <acme:print value="${deviationEstimatedCost}"/>
        </td>
    </tr>
    <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.averageEstimatedDuration"/>
        </th>
        <td>
            <acme:print value="${averageEstimatedDuration}"/>
        </td>
    </tr>
    <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.minEstimatedDuration"/>
        </th>
        <td>
            <acme:print value="${minEstimatedDuration}"/>
        </td>
    </tr>
    <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.maxEstimatedDuration"/>
        </th>
        <td>
            <acme:print value="${maxEstimatedDuration}"/>
        </td>
    </tr>
     <tr>
        <th scope="row">
            <acme:print code="technician.dashboard.form.label.deviationEstimatedDuration"/>
        </th>
        <td>
            <acme:print value="${deviationEstimatedDuration}"/>
        </td>
    </tr>
</table>
<!-- Gr�fico de Maintenance Records por Status -->
<h3>Maintenance Records Status Distribution</h3>
<div>
    <canvas id="statusCanvas"></canvas>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        var statusData = {
            labels : ["PENDING", "IN_PROGRESS", "COMPLETED"],
            datasets : [{
                data : [
                    ${numberOfRecordsGroupedByStatus['PENDING']}, 
                    ${numberOfRecordsGroupedByStatus['IN_PROGRESS']}, 
                    ${numberOfRecordsGroupedByStatus['COMPLETED']}
                ],
                backgroundColor: [
                    'rgb(255, 192, 203)',  // PENDING
                    'rgb(64, 224, 208)',  // IN_PROGRESS
                    'rgb(147, 112, 219)'   // COMPLETED
                ]
            }]
        };
        
        var statusOptions = {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    enabled: true
                }
            }
        };
        
        var statusCtx = document.getElementById("statusCanvas").getContext("2d");
        new Chart(statusCtx, {
            type : "doughnut",  // Cambiamos de 'bar' a 'doughnut'
            data : statusData,
            options : statusOptions
        });
    });
</script>


<h3>Cost Statistics Chart</h3>
<div>
    <canvas id="costCanvas"></canvas>
</div>


<script type="text/javascript">
    $(document).ready(function() {
        var data = {
            labels : [
                "AVERAGE", "MAX", "MIN", "DEVIATION"
            ],
            datasets : [
                {
                    data : [
                        ${averageEstimatedCost}, 
                        ${maxEstimatedCost}, 
                        ${minEstimatedCost}, 
                        ${deviationEstimatedCost}
                    ],
                    backgroundColor: [
                        'rgb(173, 216, 230)',
                        'rgb(144, 238, 144)',
                        'rgb(255, 182, 193)',
                        'rgb(255, 215, 0)'
                    ]
                }
            ]
        };
        
        var options = {
            scales : {
                y: {
                    beginAtZero: true
                }
            },
            legend : {
                display : false
            }
        };
        
        var canvas = document.getElementById("canvas");
        var context = canvas.getContext("2d");
        new Chart(context, {
            type : "bar",
            data : data,
            options : options
        });
    });
</script>


<h3>Duration Statistics Chart</h3>
<div>
    <canvas id="durationCanvas"></canvas>
</div>

<script type="text/javascript">
    $(document).ready(function() {
        // Gr�fico de Costos
        var costData = {
            labels : ["AVERAGE", "MAX", "MIN", "DEVIATION"],
            datasets : [{
                data : [${averageEstimatedCost}, ${maxEstimatedCost}, ${minEstimatedCost}, ${deviationEstimatedCost}],
                backgroundColor: ['rgb(173, 216, 230)','rgb(144, 238, 144)','rgb(255, 182, 193)','rgb(255, 215, 0)']
            }]
        };
        
        var costOptions = {
            scales : {
                y: { beginAtZero: true }
            },
            legend : { display : false }
        };
        
        var costCtx = document.getElementById("costCanvas").getContext("2d");
        new Chart(costCtx, {
            type : "bar",
            data : costData,
            options : costOptions
        });

        // Gr�fico de Duraci�n
        var durationData = {
            labels : ["AVERAGE", "MAX", "MIN", "DEVIATION"],
            datasets : [{
                data : [${averageEstimatedDuration}, ${maxEstimatedDuration}, ${minEstimatedDuration}, ${deviationEstimatedDuration}],
                backgroundColor: ['rgb(173, 216, 230)','rgb(144, 238, 144)','rgb(255, 182, 193)','rgb(255, 215, 0)']
            }]
        };
        
        var durationOptions = {
            scales : {
                y: { beginAtZero: true }
            },
            legend : { display : false }
        };
        
        var durationCtx = document.getElementById("durationCanvas").getContext("2d");
        new Chart(durationCtx, {
            type : "bar",
            data : durationData,
            options : durationOptions
        });
    });
</script>

<acme:return/>