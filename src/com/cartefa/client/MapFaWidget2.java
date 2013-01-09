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
public class MapFaWidget2 extends Composite {

	private List<Marker> markers = new ArrayList<Marker>();
	private VerticalPanel pWidget;

	private MapWidget mapWidget;

	public static Map<String, String> listeIconMarker;

	private HTML htmlCompteRendu;

	public MapFaWidget2(HTML htmlCompteRendu) {
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
		listeIconMarker.put("1",
				"http://labs.google.com/ridefinder/images/mm_20_purple.png");
		listeIconMarker.put("2",
				"http://labs.google.com/ridefinder/images/mm_20_yellow.png");
		listeIconMarker.put("3",
				"http://labs.google.com/ridefinder/images/mm_20_blue.png");
		listeIconMarker.put("4",
				"http://labs.google.com/ridefinder/images/mm_20_white.png");
		listeIconMarker.put("5",
				"http://labs.google.com/ridefinder/images/mm_20_green.png");
		listeIconMarker.put("6",
				"http://labs.google.com/ridefinder/images/mm_20_red.png");
		listeIconMarker.put("7",
				"http://labs.google.com/ridefinder/images/mm_20_black.png");
		listeIconMarker.put("8",
				"http://labs.google.com/ridefinder/images/mm_20_orange.png");
		listeIconMarker.put("9",
				"http://labs.google.com/ridefinder/images/mm_20_gray.png");
		listeIconMarker.put("10",
				"http://labs.google.com/ridefinder/images/mm_20_brown.png");
		listeIconMarker.put("11",
				"http://labs.google.com/ridefinder/images/mm_20_shadow.png");

	}

	private Geocoder geocoder = Geocoder.newInstance();

	public void addVille(final String typeMarker, final String type,
			final String ville, final String adresse) {
		boolean sensor = false;
		ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
		loadLibraries.add(LoadLibrary.PLACES);
		loadLibraries.add(LoadLibrary.GEOMETRY);
		LoadApi.go(new Runnable() {
			public void run() {
				GeocoderRequest request = GeocoderRequest.newInstance();
				if (!GWT.isScript()) {
					request.removeGwtObjectId();
				}
				request.setAddress(adresse);
				geocoder.geocode(request, new GeocoderRequestHandler() {
					public void onCallback(JsArray<GeocoderResult> results,
							GeocoderStatus status) {
						if (status == GeocoderStatus.OK) {

							if (results.length() > 0) {
								affichageVille(typeMarker, type, ville,
										adresse, results);
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
									+ ville
									+ "(type:"
									+ type
									+ ") (adresse: "
									+ adresse + ") (status: " + status + ")";
							ajoutAuCompteRendu(message);
						}

					}

					private void affichageVille(final String typeMarker,
							final String type, final String ville,
							final String adresse,
							JsArray<GeocoderResult> results) {
						GeocoderResult result = results.get(0);

						MarkerOptions options = MarkerOptions.newInstance();
						String typeFormate = type.toLowerCase().trim();
						if (listeIconMarker.containsKey(typeMarker)) {
							options.setIcon(listeIconMarker.get(typeMarker));
						} else {
							String message = "Type non reconnu: " + typeFormate
									+ " pour la ville " + ville;
							ajoutAuCompteRendu(message);

						}
						options.setPosition(result.getGeometry().getLocation());
						options.setTitle(type + " " + ville + " " + adresse);

						final Marker marker = Marker.newInstance(options);
						marker.setMap(mapWidget);
						marker.addClickHandler(new ClickMapHandler() {
							public void onEvent(ClickMapEvent event) {
								drawInfoWindow(type, ville, adresse, marker,
										event.getMouseEvent());
							}
						});
						mapWidget.setCenter(result.getGeometry().getLocation());
						markers.add(marker);
						// System.out.println("Ville intégré " + ville
						// + " avec le type " + type + ", coordonnées: lat -> "
						// + result.getGeometry()
						// .getLocation().getLat() +", long -> "+
						// result.getGeometry()
						// .getLocation().getLongitude());
						System.out.println(type
								+ ";"
								+ ville
								+ ";"
								+ result.getGeometry().getLocation().getLat()
								+ ";"
								+ result.getGeometry().getLocation()
										.getLongitude());
					}

				});
			}
		}, loadLibraries, sensor);
	}

	private void ajoutAuCompteRendu(String message) {
		if (htmlCompteRendu.getHTML() == null
				|| htmlCompteRendu.getHTML().isEmpty()) {
			htmlCompteRendu.setHTML("<h3>Compte rendu d'erreur</h3>");
		}
		htmlCompteRendu.setHTML(htmlCompteRendu.getHTML() + "</br>" + message);
		// System.out.println("Erreur: " + message);
	}

	protected void drawInfoWindow(String type, String ville, String adresse,
			final Marker marker, MouseEvent mouseEvent) {
		if (marker == null || mouseEvent == null) {
			return;
		}

		HTML html = new HTML("<b>Type de la ville</b>: " + type
				+ "<br/><b>Ville</b>: " + ville + "<br/><b>Adresse</b>: "
				+ adresse);

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
		mapWidget.setSize("1300px", "900px");

		mapWidget.addClickHandler(new ClickMapHandler() {
			public void onEvent(ClickMapEvent event) {
				// TODO fix the event getting, getting ....
				// System.out.println("clicked on latlng="
				// + event.getMouseEvent().getLatLng());
			}
		});

		mapWidget.addTilesLoadedHandler(new TilesLoadedMapHandler() {
			public void onEvent(TilesLoadedMapEvent event) {

			}
		});
		// System.out.println();

	}

	public void cleanMap() {
		for (Marker marker : markers) {
			marker.setMap((MapWidget) null);
		}

	}
}
