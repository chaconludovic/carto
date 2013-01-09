package com.cartefa.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CarteFa implements EntryPoint {
	MapFaWidget2 wMap;
	HTML htmlCompteRendu;
	HTML htmlLegende;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		boolean sensor = true;

		// load all the libs for use
		ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
		loadLibraries.add(LoadLibrary.ADSENSE);
		loadLibraries.add(LoadLibrary.DRAWING);
		loadLibraries.add(LoadLibrary.GEOMETRY);
		loadLibraries.add(LoadLibrary.PANORAMIO);
		loadLibraries.add(LoadLibrary.PLACES);

		Runnable onLoad = new Runnable() {
			public void run() {
				// ajout du label de compte rendu
				VerticalPanel panelCompteRendu = new VerticalPanel();
				htmlCompteRendu = new HTML();
				panelCompteRendu.add(htmlCompteRendu);
				RootPanel.get("compteRendu").add(panelCompteRendu);
				
				VerticalPanel panelLegende = new VerticalPanel();
				htmlLegende = new HTML();
				panelLegende.add(htmlLegende);
				RootPanel.get("legende").add(htmlLegende);
				
				// temps d'intégration restant
				Label tempsDIntegrationRestant = new Label();
				RootPanel.get("tempsDIntegrationRestant").add(tempsDIntegrationRestant);
				
				// ajout de la carte
				wMap = new MapFaWidget2(htmlCompteRendu);
				RootPanel.get("carte").add(wMap);
				
				
				// ajout du bouton reset
				Button boutonReset = new Button("Nettoyer la carte");
				boutonReset.addClickHandler(actionBoutonReset());
				RootPanel.get("boutonReset").add(boutonReset);

				// ajout du formulaire d'intégration
				new FileUploadView(wMap,htmlLegende, htmlCompteRendu,tempsDIntegrationRestant);

			}

		};

		LoadApi.go(onLoad, loadLibraries, sensor);
	}

	private ClickHandler actionBoutonReset() {
		return new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				htmlCompteRendu.setHTML("");
				wMap.cleanMap();

			}
		};
	}
}
