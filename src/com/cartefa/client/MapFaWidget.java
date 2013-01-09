package com.cartefa.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.MouseEvent;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.tiles.TilesLoadedMapEvent;
import com.google.gwt.maps.client.events.tiles.TilesLoadedMapHandler;
import com.google.gwt.maps.client.overlays.InfoWindow;
import com.google.gwt.maps.client.overlays.InfoWindowOptions;
import com.google.gwt.maps.client.overlays.Marker;
import com.google.gwt.maps.client.overlays.MarkerOptions;
import com.google.gwt.maps.client.services.Geocoder;
import com.google.gwt.maps.client.services.GeocoderRequest;
import com.google.gwt.maps.client.services.GeocoderRequestHandler;
import com.google.gwt.maps.client.services.GeocoderResult;
import com.google.gwt.maps.client.services.GeocoderStatus;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * {@link http://code.google.com/apis/maps/documentation/javascript/layers.html#
 * FusionTables}
 */
public class MapFaWidget extends Composite {

	private List<Marker> markers = new ArrayList<Marker>();
	private VerticalPanel pWidget;

	private MapWidget mapWidget;

	private static Map<String, String> listeIconMarker;

	private HTML htmlCompteRendu;

	public MapFaWidget(HTML htmlCompteRendu) {
		this.htmlCompteRendu = htmlCompteRendu;
		pWidget = new VerticalPanel();
		initWidget(pWidget);
		draw();
	}

	private void draw() {

		pWidget.clear();

		drawMap();
	}

	static {
		// TODO mettre des couleurs diff√©rents selon les types
		listeIconMarker = new HashMap<String, String>();
		listeIconMarker.put("département", "images/letter_d.png");
		listeIconMarker.put("d?partement", "images/letter_d.png");
		listeIconMarker.put("dÈpartement", "images/letter_d.png");
		listeIconMarker.put("dèpartement", "images/letter_d.png");
		listeIconMarker.put("departement", "images/letter_d.png");
		listeIconMarker.put("ville", "images/letter_v.png");
		listeIconMarker.put("région", "images/letter_r.png");
		listeIconMarker.put("r?gion", "images/letter_r.png");
		listeIconMarker.put("rÈgion", "images/letter_r.png");
		listeIconMarker.put("règion", "images/letter_r.png");
		listeIconMarker.put("region", "images/letter_r.png");
		listeIconMarker.put("utility", "images/letter_u.png");
		listeIconMarker.put("interco", "images/letter_i.png");
		
	}

	public void addVille(final String type, final String ville) {
		boolean sensor = false;
		ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
		loadLibraries.add(LoadLibrary.PLACES);
		loadLibraries.add(LoadLibrary.GEOMETRY);
		LoadApi.go(new Runnable() {
			public void run() {
				Geocoder o = Geocoder.newInstance();
				GeocoderRequest request = GeocoderRequest.newInstance();
				if (!GWT.isScript()) {
					request.removeGwtObjectId();
				}
				request.setAddress(ville);
				o.geocode(request, new GeocoderRequestHandler() {
					public void onCallback(JsArray<GeocoderResult> results,
							GeocoderStatus status) {
						if (status == GeocoderStatus.OK) {

							if (results.length() > 0) {
								GeocoderResult result = results.get(0);
								MarkerOptions options = MarkerOptions
										.newInstance();
								String typeFormate = type.toLowerCase().trim();
								if (listeIconMarker.containsKey(typeFormate)) {
									options.setIcon(listeIconMarker
											.get(typeFormate));
								} else {
									String message = "Type non reconnu: "
											+ typeFormate + " pour la ville "
											+ ville;
									ajoutAuCompteRendu(message);

								}
								options.setPosition(result.getGeometry()
										.getLocation());
								options.setTitle(type + " " + ville);

								final Marker marker = Marker
										.newInstance(options);
								marker.setMap(mapWidget);
								marker.addClickHandler(new ClickMapHandler() {
									public void onEvent(ClickMapEvent event) {
										drawInfoWindow(type, ville, marker,
												event.getMouseEvent());
									}
								});
								mapWidget.setCenter(result.getGeometry()
										.getLocation());
								markers.add(marker);
								System.out.println("Ville intégré " + ville
										+ " avec le type " + type 
										 + ", coordonnées: " );
							} else {
								String message = "une erreur est arrivee: plusieurs possibilités existent pour la ville "
										+ ville + "(" + type + ")";
								ajoutAuCompteRendu(message);
							}

						} else if (status == GeocoderStatus.ERROR) {
							String message = "une erreur est arrivee: ville en integration "
									+ ville
									+ "("
									+ type
									+ ")"
									+ "(ERROR)"
									+ status;
							ajoutAuCompteRendu(message);
						} else if (status == GeocoderStatus.INVALID_REQUEST) {
							String message = "une erreur est arrivee: ville en integration "
									+ ville
									+ "("
									+ type
									+ ")"
									+ "(INVALID_REQUEST)" + status;
							ajoutAuCompteRendu(message);
						} else if (status == GeocoderStatus.OVER_QUERY_LIMIT) {
							String message = "limite d'integration depassee (max 9), la ville "
									+ ville
									+ "("
									+ type
									+ ") n'a pas pu être integree ("
									+ status
									+ ")";
							ajoutAuCompteRendu(message);
						} else if (status == GeocoderStatus.REQUEST_DENIED) {
							String message = "une erreur est arrivee: ville en integration "
									+ ville
									+ "("
									+ type
									+ ")"
									+ "(REQUEST_DENIED)" + status;
							ajoutAuCompteRendu(message);
						} else if (status == GeocoderStatus.UNKNOWN_ERROR) {
							String message = "une erreur est arrivee: ville en integration "
									+ ville
									+ "("
									+ type
									+ ")"
									+ "(UNKNOWN_ERROR)" + status;
							ajoutAuCompteRendu(message);
						} else if (status == GeocoderStatus.ZERO_RESULTS) {
							String message = "aucun resultat trouve pour la ville "
									+ ville + "(" + type + ") (" + status + ")";
							ajoutAuCompteRendu(message);
						}

					}

				});
			}
		}, loadLibraries, sensor);
	}

	private void ajoutAuCompteRendu(String message) {
		if (htmlCompteRendu.getHTML() == null
				|| htmlCompteRendu.getHTML().isEmpty()) {
			htmlCompteRendu.setHTML("Compte rendu d'erreur:");
		}
		htmlCompteRendu.setHTML(htmlCompteRendu.getHTML() + "</br>" + message);
		System.out.println("Erreur: " + message);
	}

	protected void drawInfoWindow(String type, String ville,
			final Marker marker, MouseEvent mouseEvent) {
		if (marker == null || mouseEvent == null) {
			return;
		}

		HTML html = new HTML("<b>Type de la ville</b>: " + type
				+ "<br/><b>Ville</b>: " + ville);

		InfoWindowOptions options = InfoWindowOptions.newInstance();
		options.setContent(html);

		InfoWindow iw = InfoWindow.newInstance(options);
		iw.open(mapWidget, marker);

	}

	private void drawMap() {
		LatLng center = LatLng.newInstance(49.496675, -102.65625);
		MapOptions opts = MapOptions.newInstance();
		opts.setZoom(4);
		opts.setCenter(center);
		opts.setMapTypeId(MapTypeId.HYBRID);

		mapWidget = new MapWidget(opts);
		pWidget.add(mapWidget);
		mapWidget.setCenter(LatLng.newInstance(48.869084, 2.342152));
		mapWidget.setZoom(5);
		mapWidget.setSize("750px", "500px");

		mapWidget.addClickHandler(new ClickMapHandler() {
			public void onEvent(ClickMapEvent event) {
				// TODO fix the event getting, getting ....
				System.out.println("clicked on latlng="
						+ event.getMouseEvent().getLatLng());
			}
		});

		mapWidget.addTilesLoadedHandler(new TilesLoadedMapHandler() {
			public void onEvent(TilesLoadedMapEvent event) {

			}
		});
		System.out.println();

	}

	public void cleanMap() {
		for (Marker marker : markers) {
			marker.setMap((MapWidget) null);
		}

	}
}
