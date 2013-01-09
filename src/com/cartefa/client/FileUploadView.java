package com.cartefa.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * http://code.google.com/p/gwt-excel-import-export/
 * http://www.geiesample.appspot.com/
 * 
 */
public class FileUploadView {

	private String tempsRestantText = "Temps d'integration restant (en s): ";
	private MapFaWidget2 wMap;
	private Label tempsDIntegrationRestant;
	private HTML htmlCompteRendu;
	private HTML htmlLegende;
	private static Integer MAX_ELEMENT_A_INTEGRER = 9;
	private static Integer LAPSE_TEMPS_ENTRE_CHAQUE_LOT = 10000;

	public FileUploadView(MapFaWidget2 wMap, HTML htmlLegende,
			HTML htmlCompteRendu, Label tempsDIntegrationRestant) {
		this.htmlLegende = htmlLegende;
		this.htmlCompteRendu = htmlCompteRendu;
		this.wMap = wMap;
		this.tempsDIntegrationRestant = tempsDIntegrationRestant;
		createUploadForm("csv");
	}

	private void createUploadForm(final String suffix) {
		clear();
		final FormPanel form = new FormPanel();
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		form.setWidth("275px");

		VerticalPanel holder = new VerticalPanel();

		final FileUpload upload = new FileUpload();

		upload.setName("upload");
		holder.add(upload);
		holder.setHorizontalAlignment(HasAlignment.ALIGN_RIGHT);
		holder.add(new Button("Submit", new ClickHandler() {
			public void onClick(ClickEvent clickEvent) {
				form.submit();
			}
		}));

		form.add(holder);

		form.setAction("/cartefa/Import?type=" + suffix);

		form.addSubmitHandler(new FormPanel.SubmitHandler() {
			public void onSubmit(FormPanel.SubmitEvent event) {
				if (!(upload.getFilename().endsWith(suffix))) {
					event.cancel();
				}
			}
		});

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
				String results = event.getResults();
				if (results == null || results.isEmpty()) {
					ajoutAuCompteRendu("Aucune ligne n'a été trouvé dans le fichier excel");
					return;
				}
				// Remove html tags
				RegExp regExp = RegExp.compile("<(.|\\n)*?>,", "g");
				String key = regExp.replace(results, "");
				String[] ligneColonnes = key.split("echap");
				if (ligneColonnes == null || ligneColonnes.length == 0) {
					ajoutAuCompteRendu("Aucune ligne n'a été trouvé dans le fichier excel");
					return;
				}
				int compt = 0;
				final Map<Integer, Map<String, String>> temp = new HashMap<Integer, Map<String, String>>();
				Map<String, String> mapVilleType = new HashMap<String, String>();
				final Map<String, String> mapVilleAdresse = new HashMap<String, String>();
				int compteMapKey = 0;
				List<String> listePourVerifUnicite = new ArrayList<String>();
				final Map<String, String> typePourMarqueur = new HashMap<String, String>();
				int compteurType = 0;
				for (String ligne : ligneColonnes) {
					compt++;
					String[] elts = ligne.split(";");
					if (elts == null || elts.length == 0) {
						ajoutAuCompteRendu("Ligne vide pour la ligne numéro "
								+ compt);
						continue;
					}
					if (elts.length != 3) {
						ajoutAuCompteRendu("La ligne numero "
								+ compt
								+ " ne contient pas suffisament d'element (TYPE;VILLE;ADRESSE)");
						continue;
					}
					final String type = elts[0];
					final String ville = elts[1];
					final String adresse = elts[2];
					if (ville == null || ville.isEmpty()) {
						ajoutAuCompteRendu("A la ligne " + compt
								+ ", aucune ville n'a été définie");
						continue;
					}
					if (type == null || type.isEmpty()) {
						ajoutAuCompteRendu("A la ligne " + compt
								+ ", aucun type n'a été définie");
						continue;
					}
					if (listePourVerifUnicite.contains(ville)) {
						ajoutAuCompteRendu("A la ligne " + compt
								+ ", l'element " + ville
								+ " a deja ete integree");
						continue;
					}
					listePourVerifUnicite.add(ville);
					mapVilleType.put(ville, type);
					if (!typePourMarqueur.containsKey(type)) {
						typePourMarqueur.put(type, "" + ++compteurType);
					}
					mapVilleAdresse.put(ville, adresse);
					//
					if (mapVilleType.keySet().size() >= MAX_ELEMENT_A_INTEGRER) {
						temp.put(compteMapKey++, new HashMap<String, String>(
								mapVilleType));
						mapVilleType = new HashMap<String, String>();
					}
				}
				if (mapVilleType.keySet().size() != 0) {
					temp.put(compteMapKey++, new HashMap<String, String>(
							mapVilleType));
				}
				
				tempsDIntegrationRestant
						.setText(tempsRestantText
								+ (temp.keySet().size() * (LAPSE_TEMPS_ENTRE_CHAQUE_LOT / 1000)));
				// affichage de la légende
				affichageLegende(typePourMarqueur);
				// lancement de l'intégration
				for (final Integer compte : temp.keySet()) {
					// System.out.println("Compte " + compte);
					Timer timer = new Timer() {
						public void run() {
							// System.out.println("Timer lancer");
							for (final String ville : temp.get(compte).keySet()) {
								String type = temp.get(compte).get(ville);

								wMap.addVille(typePourMarqueur.get(type), type,
										ville, mapVilleAdresse.get(ville));
							}
							affichageTempsRestant();

						}

						private void affichageTempsRestant() {
							Integer tempsRestant = getTempsRestantDansLabel();
							if (tempsRestant == null || tempsRestant < 10) {
								tempsDIntegrationRestant.setText("");
							} else {
								int temps = tempsRestant
										- (LAPSE_TEMPS_ENTRE_CHAQUE_LOT / 1000);
								if (temps < 10) {
									temps = 0;
									tempsDIntegrationRestant.setText("");
								}
								tempsDIntegrationRestant
										.setText(tempsRestantText + temps);
							}
							/*
							 * if (compte == temp.keySet().size() - 1) {
							 * tempsDIntegrationRestant.setText(""); } else {
							 * 
							 * }
							 */
						}

						private Integer getTempsRestantDansLabel() {
							Integer resultat;
							try {
								resultat = Integer
										.valueOf(tempsDIntegrationRestant
												.getText().replace(
														tempsRestantText, ""));
							} catch (Throwable e) {
								resultat = null;
							}
							return resultat;
						}
					};

					timer.schedule(compte * LAPSE_TEMPS_ENTRE_CHAQUE_LOT + 1);
				}

			}

			private void affichageLegende(
					final Map<String, String> typePourMarqueur) {
				String message = "<h3>Legende</h3><ul>";
				for (String type : typePourMarqueur.keySet()) {
					String urlImage = MapFaWidget2.listeIconMarker
							.get(typePourMarqueur.get(type));
					String image = "<IMG SRC=\"" + urlImage + "\">";
					message += "<li>" + image + " " + type + "</li>";
				}
				message += "</ul>";
				htmlLegende.setHTML(message);
			}
		});

		RootPanel.get("upload").add(form);
	}

	private void ajoutAuCompteRendu(String message) {
		if (htmlCompteRendu.getHTML() == null
				|| htmlCompteRendu.getHTML().isEmpty()) {
			htmlCompteRendu.setHTML("<h3>Compte rendu d'erreur</h3></br>");
		}
		htmlCompteRendu.setHTML(htmlCompteRendu.getHTML() + "</br>" + message);
		// System.out.println("Erreur: " + message);
	}

	private void clear() {
		RootPanel.get("upload").clear();
		// System.out.println("Carte nettoyée");
	}

}
