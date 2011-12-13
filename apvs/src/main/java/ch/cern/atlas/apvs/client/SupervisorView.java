package ch.cern.atlas.apvs.client;

import ch.cern.atlas.apvs.client.widget.HorizontalFlowPanel;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;

public class SupervisorView extends DockPanel {

	private ScrollPanel mainScrollPanel;
	private int windowWidth;
	private int windowHeight;

	public SupervisorView(final RemoteEventBus remoteEventBus) {

		add(new Label("Atlas Procedures Visualization System"), NORTH);
		add(new Label("Version 0.1"), SOUTH);

		TabPanel tabPanel = new TabPanel();
		add(tabPanel, NORTH);

		final DockPanel mainPanel = new DockPanel();
		mainPanel.add(new SupervisorWorkerView(remoteEventBus, null), NORTH);
		// mainPanel.add(new SupervisorWorkerView(eventBus), NORTH);
		
		HorizontalFlowPanel buttonPanel = new HorizontalFlowPanel();
		mainPanel.add(buttonPanel, SOUTH);
		Button addWorker = new Button("Add Worker");
		addWorker.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Button deleteButton = new Button("Delete");
				final SupervisorWorkerView workerView = new SupervisorWorkerView(remoteEventBus, deleteButton);

				deleteButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (Window.confirm("Are you sure you want to delete the view of worker: "+workerView.getName())) {
							mainPanel.remove(workerView);
						}
					}
				});
				
				mainPanel.add(workerView, NORTH);
				resize();
			}
		});
		buttonPanel.add(addWorker);
		
		mainScrollPanel = new ScrollPanel(mainPanel);
		tabPanel.add(mainScrollPanel, "Workers");

		tabPanel.add(new PtuView(remoteEventBus), "PTUs");
		tabPanel.add(new DosimeterView(remoteEventBus), "Dosimeters");
		tabPanel.add(new SupervisorSettingsView(remoteEventBus),
				"Supervisor Settings");
		tabPanel.add(new ServerSettingsView(remoteEventBus), "Server Settings");

		tabPanel.selectTab(0);
		
		// Save the initial size of the browser.
		windowWidth = Window.getClientWidth();
		windowHeight = Window.getClientHeight();

		// Add a listener for browser resize events.
		Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				// Save the new size of the browser.
				windowWidth = event.getWidth();
				windowHeight = event.getHeight();
				// Reformat everything for the new browser size.
				resize();
			}
		});
		
		mainScrollPanel.addAttachHandler(new Handler() {
			
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				resize();
			}
		});

		resize();
	}

	private void resize() {
		// Set the size of main body scroll panel so that it fills the
		mainScrollPanel.setSize(
				Math.max(windowWidth - mainScrollPanel.getAbsoluteLeft(), 0)
						+ "px",
				Math.max(windowHeight - mainScrollPanel.getAbsoluteTop() - 25, 0)
						+ "px");
	}

}
