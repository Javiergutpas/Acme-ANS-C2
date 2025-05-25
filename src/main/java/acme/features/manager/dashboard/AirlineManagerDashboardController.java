
package acme.features.manager.dashboard;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.controllers.AbstractGuiController;
import acme.client.controllers.GuiController;
import acme.forms.AirlineManagerDashboard;
import acme.realms.manager.AirlineManager;

@GuiController
public class AirlineManagerDashboardController extends AbstractGuiController<AirlineManager, AirlineManagerDashboard> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private AirlineManagerDashboardShowService showService;

	// Constructors -----------------------------------------------------------


	@PostConstruct
	protected void initialise() {
		super.addBasicCommand("show", this.showService);
	}
}
