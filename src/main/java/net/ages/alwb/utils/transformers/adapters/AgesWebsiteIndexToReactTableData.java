package net.ages.alwb.utils.transformers.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.AgesIndexTableData;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.AgesIndexTableRowData;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ioc.liturgical.ws.constants.Constants;
import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;

/**
 * Reads the index of available sacraments and services from
 * the GOARCH DCS website, and converts it to json
 * data that can be rendered by a React BootstrapTable
 * @author mac002
 *
 */
public class AgesWebsiteIndexToReactTableData {
	private static final Logger logger = LoggerFactory.getLogger(AgesWebsiteIndexToReactTableData.class);
	public static String typePdf = "PDF/Print";
	public static String typeText = "Text/Music";
	private String baseUrl = "https://dcs.goarch.org/goa/dcs/";
	private String booksPath = "h/b/";
	private String servicesIndex = baseUrl + "servicesindex.html";
	private String booksIndex = baseUrl + "booksindex.html";
	private String jsonservicesIndex = baseUrl + "servicesindex.json";
	private String olwBaseUrl = "https://books.liml.org/";
	private String olwBooksUrl = olwBaseUrl + "booksindex.html";
	private String agesOcmcBaseUrl = "https://books.liml.org/";
	private String agesOcmcIndex = "customindex.html";
	private String agesOcmcJsonIndex = agesOcmcBaseUrl + "h/c/index.json";
	private String readingsIndex = agesOcmcBaseUrl + agesOcmcIndex;
	private String theophanyUrl = Constants.LIML_URL + Constants.LIML_STATIC + "theophany.html";
	private String typikaUrl = Constants.LIML_URL + Constants.LIML_STATIC + "h/b/ho/s07/h61/gr-en/index.html";
    // durham
	private String durhamLiUrl = Constants.LIML_URL + Constants.LIML_STATIC + "durham/bk.li.chrys.html";
	// mexico
	private String mexicoLiUrl = Constants.LIML_URL + Constants.LIML_STATIC + "mexico/bk.li.chrysbasil.html";
	private String octoechosUrl = agesOcmcBaseUrl + "h/b/oc/";
	private String limlBooksUrl = Constants.LIML_URL + Constants.LIML_STATIC + "h/b/";
	private String triodionUrl = agesOcmcBaseUrl + "tr/h/b/";
	private String basilUrl = Constants.LIML_URL + Constants.LIML_STATIC + "bk.liturgy.basil.html";
	private String akathistCanon = baseUrl + "h/b/tr/d56_canon/gr-en/index.html";
	private List<AgesIndexTableRowData> additionalAgesBookRows = new ArrayList<AgesIndexTableRowData>();
	private boolean printPretty = false;
	private Gson gson = new com.google.gson.Gson();
	
	public AgesWebsiteIndexToReactTableData() {
		this.addAdditionalUrls();
	}
	public AgesWebsiteIndexToReactTableData(boolean printPretty) {
		this.printPretty = printPretty;
		this.addAdditionalUrls();
	}
	
	private void addAdditionalUrls() {
		String base = this.baseUrl + this.booksPath;
		AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);

		row.setDayOfWeek("any");
		row.setType("Liturgy of St. Chrysostom");
		row.setDate("any");
		row.setUrl(base + "skeleton/liturgy/chrys/gr-en/index.html");
		this.additionalAgesBookRows.add(row);

		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("any");
		row.setType("Liturgy of St. James");
		row.setDate("any");
		row.setUrl(base + "liturgy/james/gr-en/index.html");
		this.additionalAgesBookRows.add(row);

		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("any");
		row.setType("Typika Service (no priest)");
		row.setDate("any");
		row.setUrl(base + "ho/s07/h61/gr-en/index.html");
		this.additionalAgesBookRows.add(row);

		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("any");
		row.setType("Service of Preparation for Holy Communion (Ἀκολουθία τῆς Θείας Μεταλήψεως)");
		row.setDate("any");
		row.setUrl(base + "ho/s21/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);

		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("any");
		row.setType("Service of Small Paraklesis (Ἀκολουθία τοῦ Μικροῦ Παρακλητικοῦ Κανόνος)");
		row.setDate("any");
		row.setUrl(base + "ho/s23/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);

		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("any");
		row.setType("Service of Great Paraklesis (Ἀκολουθία τοῦ Μεγάλου Παρακλητικοῦ Κανόνος)");
		row.setDate("any");
		row.setUrl(base + "ho/s24/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// add Holy Week services
		// Sunday bridegroom service
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Sunday");
		row.setType("Holy Week Bridegroom Service on Sunday Evening - Matins of Holy Monday (Μεγάλη Ἑβδομάδα· Ἀκολουθία τοῦ Νυμφίου τῇ Κυριακῇ τὸ Βράδυ  Ὄρθρος τῆς Μεγάλης Δευτέρας ΑΚΟΛΟΥΘΙΑ ΤΟΥ ΟΡΘΡΟΥ)");
		row.setDate("Triodion d065");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d065/ma/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 56 Akathist Canon
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Saturday of the Fifth Week");
		row.setType("The Canon of the Akathist (Ὁ Κανὼν τοῦ Ἀακαθίστου)");
		row.setDate("Triodion d056");
		row.setUrl(this.akathistCanon);		
		this.additionalAgesBookRows.add(row);
		// Palm Sunday Eveing Vespers
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Sunday");
		row.setType("Palm Sunday - On Sunday Evening at Vespers (Κυριακὴ τῶν Βαΐων· Τῇ Κυριακῇ Ἑσπέρας εἰς τὸν Ἑσπερινόν )");
		row.setDate("Triodion d065");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d065/ve/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 66 Matins
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Monday");
		row.setType("Holy Week -  Bridegroom Service on Monday Evening - Matins of Holy Tuesday (Μεγάλη Ἑβδομάδα· Ἀκολουθία τοῦ Νυμφίου τῇ Δευτέρᾳ τὸ Βράδυ· Ὄρθρος τῆς Μεγάλης Τρίτης)");
		row.setDate("Triodion d066");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d066/ma/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 66 Presanctified Liturgy
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Monday");
		row.setType("Holy Week - Presanctified Liturgy of Holy Monday - Vespers of Holy Tuesday (Μεγάλη Ἑβδομάδα· Προηγιασμένη Λειτουργία τῆς Μεγάλης Δευτέρας· Ἑσπερινὸς τῆς Μεγάλης Τρίτης)");
		row.setDate("Triodion d066");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d066/pl/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 67  Matins
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Tuesday");
		row.setType("Holy Week - Bridegroom Service on Tuesday Evening -  Matins of Holy Wednesday (Μεγάλη Ἑβδομάδα·  Ἀκολουθία τοῦ Νυμφίου τῇ Τρίτῃ τὸ Βράδυ· Ὄρθρος τῆς Μεγάλης Τετάρτης)");
		row.setDate("Triodion d067");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d067/ma/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 67 Presanctified Liturgy
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Tuesday");
		row.setType("Holy Week - Presanctified Liturgy of Holy Tuesday - Vespers of Holy Wednesday (Μεγάλη Ἑβδομάδα· Προηγιασμένη Λειτουργία τῆς Μεγάλης Τρίτης· Ἑσπερινὸς τῆς Μεγάλης Τετάρτης)");
		row.setDate("Triodion d067");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d067/pl/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 68 Matins
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Wedneday");
		row.setType("Holy Week - Bridegroom Service on Wednesday Evening -  Matins of Holy Thursday (Μεγάλη Ἑβδομάδα·  Ἀκολουθία τοῦ Νυμφίου τῇ Τετάρτῃ  τὸ Βράδυ· Ὄρθρος τῆς Μεγάλης Πέμπτης)");
		row.setDate("Triodion d068");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d068/ma/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 68 Presanctified Liturgy
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Wednesday");
		row.setType("Holy Week - Presanctified Liturgy of Holy Wednesday - Vespers of Holy Thursday (Μεγάλη Ἑβδομάδα· Προηγιασμένη Λειτουργία τῆς Μεγάλης Τετάρτης· Ἑσπερινὸς τῆς Μεγάλης Πέμπτης)");
		row.setDate("Triodion d068");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d068/pl/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 69 12 Passion Gospels
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Thursday");
		row.setType("Holy Week - Service of the Twelve Passion Gospels on Thursday Evening - Matins of Holy Friday (Μεγάλη Ἑβδομάδα·  Ἀκολουθία τῶν Ἁγίων Παθῶν τῇ Πέμπτῃ τὸ Βράδυ· Ὄρθρος τῆς Μεγάλης Παρασκευῆς)");
		row.setDate("Triodion d069");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d069/ma/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 69 Vesperal Liturgy of St. Basil
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Thursday");
		row.setType("Holy Week - Vesperal Liturgy of St. Basil of Holy Thursday -  Vespers of Holy Friday (Μεγάλη Ἑβδομάδα·  Ἑσπερινὴ Λειτουργία Ἁγ. Βασιλείου τῆς Μεγάλης Πέμπτης· Ἑσπερινὸς τῆς Μεγάλης Παρασκευῆς)");
		row.setDate("Triodion d069");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d069/vl/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 69 Hours
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Thursday");
		row.setType("Holy Week - The Service of Hours on Great and Holy Friday  (Μεγάλη Ἑβδομάδα· ΑΚΟΛΟΥΘΙΑ ΤΩΝ ΩΡΩΝ ΤΗΣ ΑΓΙΑΣ ΚΑΙ ΜΕΓΑΛΗΣ ΠΑΡΑΣΚΕΥΗΣ )");
		row.setDate("Triodion d069");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d069/gh/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 70 Graveside Lamentations
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Friday");
		row.setType("Holy Week - The Graveside Lamentation on Friday Evening -  Matins of Holy Saturday (Μεγάλη Ἑβδομάδα·  Ὁ Ἐπιτάφιος Θρῆνος τῇ Παρασκευῇ τὸ Βράδυ·  Ὄρθρος τοῦ Μεγάλου Σαββάτου)");
		row.setDate("Triodion d070");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d070/ma/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 70 The Lamentations
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Friday");
		row.setType("Holy Week - The Lamentations (Μεγάλη Ἑβδομάδα -  Τὰ Ἐγκώμια)");
		row.setDate("Triodion d070");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d070/em3/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 70 Removal From the Cross
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Friday");
		row.setType("Holy Week -  Removal from the Cross on Friday Afternoon - Vespers of Holy Saturday (Μεγάλη Ἑβδομάδα·  Ἡ Ἀποκαθήλωσις τῇ Παρασκευῇ τὸ Ἀπόγευμα· Ἑσπερινὸς τοῦ Μεγάλου Σαββάτου)");
		row.setDate("Triodion d070");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d070/ve/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 71 Service before resurrection service
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Saturday");
		row.setType("Holy Week - The Service before the Resurrection on Saturday Evening (Μεγάλη Ἑβδομάδα·  Ἡ Πανηχὶς πρὸ τῆς Ἀναστάσεως τῷ Σαββάτῳ τὸ Βράδυ)");
		row.setDate("Triodion d071");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d071/co1/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		// Day 71 Holy Saturday Vesperal Liturgy of St. Basil
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("Saturday");
		row.setType("Holy Week - Holy Saturday -  VESPERAL LITURGY OF ST. BASIL (Μεγάλη Ἑβδομάδα - Τῷ Ἁγίῳ καὶ Μεγάλῳ Σαββάτῳ· ΕΣΠΕΡΙΝΗ ΛΕΙΤΟΥΡΓΙΑ ΑΓ ΒΑΣΙΛΕΟΥ)");
		row.setDate("Triodion d071");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/tr/d071/vl/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
		
		/**
		 * Add the Heirmologion
		 */
		for (int m = 1; m < 9; m++ ) {
			// heirmoi
			row = new AgesIndexTableRowData(printPretty);
			row.setDayOfWeek("any");
			row.setType("Heirmologion - Heirmoi - Mode " + m + " ( Εἱρμολόγιον - Εἱρμοί - " + this.getGreek(m) + ") ");
			row.setDate("any");
			row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/he/h/m" + m + "/gr-en/index.html");		
			this.additionalAgesBookRows.add(row);
			// automela
			row = new AgesIndexTableRowData(printPretty);
			row.setDayOfWeek("any");
			row.setType("Heirmologion - Automela - Mode " + m + " ( Εἱρμολόγιον - Αυτόμελα - " + this.getGreek(m) + ") ");
			row.setDate("any");
			row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/he/a/m" + m + "/gr-en/index.html");		
			this.additionalAgesBookRows.add(row);
		}
		// apolytikion
		row = new AgesIndexTableRowData(printPretty);
		row.setDayOfWeek("any");
		row.setType("Heirmologion - General Apolytikia ( Εἱρμολόγιον - Ἀπολυτίκια) ");
		row.setDate("any");
		row.setUrl("https://dcs.goarch.org/goa/dcs/h/b/he/ga/gr-en/index.html");		
		this.additionalAgesBookRows.add(row);
	}
	
	private String getGreek(int m) {
		String result = "";
		switch (m) {
		case (1): {
			result = "Ἦχος αʹ.";
			break;
		}
		case (2): {
			result = "Ἦχος βʹ.";
			break;
		}
		case (3): {
			result = "Ἦχος γʹ.";
			break;
		}
		case (4): {
			result = "Ἦχος δʹ.";
			break;
		}
		case (5): {
			result = "Ἦχος πλ. αʹ.";
			break;
		}
		case (6): {
			result = "Ἦχος πλ. βʹ.";
			break;
		}
		case (7): {
			result = "Ἦχος βαρύς.";
			break;
		}
		case (8): {
			result = "Ἦχος πλ. δʹ.";
			break;
		}
		}
		return result;
	}
	
	public AgesIndexTableData  toReactTableDataFromOlwBooksHtml() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Document readingsIndexDoc = null;
		Connection readingsIndexConnection = null;
		try {
			readingsIndexConnection = Jsoup.connect(olwBooksUrl);
			readingsIndexDoc = readingsIndexConnection
					.timeout(60*1000)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.maxBodySize(0)
					.get();
			Elements books = readingsIndexDoc.select("a.index-books-file-link");
			for (Element bookAnchor : books) {
				String href = bookAnchor.attr("href");
				// 0  1  2    3       4
				// h/b/me/m01/d01/gr-en/index.html = menaion
				// h/b/pe/d00p/gr-en/index.html = Pentecostarion
				// h/b/sy/m01/d03_olw/gr-en/index.html = Synaxarion
				// h/b/tr/d046/gr-en/index.html = Triodion
				String[] hrefParts = href.split("/");
				if (href.startsWith("h/b/")) {
					String bookType = hrefParts[2];
					AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
					row.setDayOfWeek("any");
					row.setUrl(olwBaseUrl + href);
					switch (bookType) {
					case ("ho"): {
						String [] dateParts = hrefParts[4].split("_");
						String date = hrefParts[3] + "/" + dateParts[0];
						row.setDate(date);
						row.setType("Horologion (τό ῾Ωρλόγιον)");
						result.addRow(row);
						break;
					}
					case ("me"): {
						String [] dateParts = hrefParts[4].split("_");
						String date = hrefParts[3] + "/" + dateParts[0];
						row.setDate(date);
						row.setType("Menaion (τά Μηναῖα)");
						result.addRow(row);
						break;
					}
					case ("oc"): {
						String [] dateParts = hrefParts[4].split("_");
						String date = hrefParts[3] + "/" + dateParts[0];
						row.setDate(date);
						row.setType("Octoechos (ἡ Παρακλητική)");
						result.addRow(row);
						break;
					}
					case ("pe"): {
						String [] dateParts = hrefParts[3].split("_");
						String date = dateParts[0];
						if (date.equals("d00p")) {
							date = "d000 Pascha (Το Πάσχα)";
						}
						row.setDate(date);
						row.setType("Pentecostarion (τό Πεντηκοστάριον)");
						result.addRow(row);
						break;
					}
					case ("psalter"): {
						String [] dateParts = hrefParts[4].split("_");
						String date = hrefParts[3] + "/" + dateParts[0];
						row.setDate(date);
						row.setType("Psalterion (τό Ψαλτήριον)");
						result.addRow(row);
						break;
					}
					case ("sy"): {
						String [] dateParts = hrefParts[4].split("_");
						String date = hrefParts[3] + "/" + dateParts[0];
						row.setDate(date);
						row.setType("Synaxarion (τό Συναξάριον)");
						result.addRow(row);
						break;
					}
					case ("tr"): {
						String [] dateParts = hrefParts[3].split("_");
						String date = dateParts[0];
						row.setDate(date);
						row.setType("Triodion (τό Τριῴδιον)");
						result.addRow(row);
						break;
					}
					default: {
						logger.info("Missing switch for OLW Daily Readings book " + bookType);
					}
					}
				}
			}
			AgesIndexTableRowData theophany = new AgesIndexTableRowData(printPretty);
			theophany.setDayOfWeek("any");
			theophany.setType("Theophany");
			theophany.setDate("m01/d06");
			theophany.setUrl(theophanyUrl);
			result.addRow(theophany);

			AgesIndexTableRowData liDurham = new AgesIndexTableRowData(printPretty);
			liDurham.setDayOfWeek("any");
			liDurham.setType("Durham - Divine Liturgy of St. John Chrysostom");
			liDurham.setDate("any");
			liDurham.setUrl(durhamLiUrl);
			result.addRow(liDurham);

			AgesIndexTableRowData liMexico = new AgesIndexTableRowData(printPretty);
			liMexico.setDayOfWeek("any");
			liMexico.setType("Divine Liturgy for Greek Orthodox Metropolis of Mexico, Central America, Colombia and Venezuela, and the Caribbean Islands");
			liMexico.setDate("any");
			liMexico.setUrl(mexicoLiUrl);		
			this.additionalAgesBookRows.add(liMexico);

			AgesIndexTableRowData basil = new AgesIndexTableRowData(printPretty);
			basil.setDayOfWeek("any");
			basil.setType("Divine Liturgy of St. Basil");
			basil.setDate("any");
			basil.setUrl(basilUrl);
			result.addRow(basil);
			for (AgesIndexTableRowData row : this.additionalAgesBookRows) {
				result.addRow(row);
			}
			for (AgesIndexTableRowData row : this.toReactTableDataOctoechos().getTableData()) {
				result.addRow(row);
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}	
	
	private String getDayOfWeek(int j) {
		String day = "";
		switch (j) {
		case (0): {
			day = "Sunday Orthos (Matins)";
			break;
		}
		case (1): {
			day = "Saturday Evening / Sunday";
			break;
		}
		case (2): {
			day = "Sunday Evening / Monday";
			break;
		}
		case (3): {
			day = "Monday Evening / Tuesday";
			break;
		}
		case (4): {
			day = "Tuesday Evening / Wednesday";
			break;
		}
		case (5): {
			day = "Wednesday Evening / Thursday";
			break;
		}
		case (6): {
			day = "Thursday Evening / Friday";
			break;
		}
		case (7): {
			day = "Friday Evening / Saturday";
			break;
		}
		}
		return day;
	}
	
	public AgesIndexTableData  toReactTableDataOctoechos() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		int modeCount = 9;
		int dayCount = 8;
		for (int i = 1; i < modeCount; i++) {
			for (int j = 1; j < dayCount; j++) {
				StringBuilder path = new StringBuilder();
				path.append("oc/m");
				path.append(i);
				path.append("/d");
				path.append(j);
				path.append("/gr-en/index.html");
				AgesIndexTableRowData mode = new AgesIndexTableRowData(printPretty);
				mode.setDayOfWeek(this.getDayOfWeek(j));
				mode.setType("Octoechos Mode " + i + " Day " + j);
				mode.setDate("any");
				mode.setUrl(limlBooksUrl + path.toString());
				result.addRow(mode);
				// if (j == 1) {
				// 	path = new StringBuilder();
				// 	path.append("oc/m");
				// 	path.append(i);
				// 	path.append("/d");
				// 	path.append(j);
				// 	path.append("/ma/gr-en/index.html");
				// 	mode = new AgesIndexTableRowData(printPretty);
				// 	mode.setDayOfWeek(this.getDayOfWeek(0));
				// 	mode.setType("Octoechos Mode " + i + " Day " + j);
				// 	mode.setDate("any");
				// 	mode.setUrl(limlBooksUrl + path.toString());
				// 	result.addRow(mode);
				// }
			}
		}
		return result;
	}
	
	public AgesIndexTableData  toReactTableDataFromDailyReadingHtml() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Document readingsIndexDoc = null;
		Connection readingsIndexConnection = null;
		try {
			readingsIndexConnection = Jsoup.connect(readingsIndex);
			readingsIndexDoc = readingsIndexConnection
					.timeout(60*1000)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.maxBodySize(0).get();
			Elements months = readingsIndexDoc.select("a.index-custom-file-link");
			for (Element monthAnchor : months) {
				String href = monthAnchor.attr("href");
				String[] hrefParts = href.split("/");
				if (hrefParts.length == 5) {
					if (hrefParts[0].equals("h")) {
						if (hrefParts[3].equals("gr-en") || hrefParts[3].equals("gr-spa")) {
							String [] dateParts = hrefParts[2].split("_");
							// TODO: we need to compute the year or it needs to be included in the html
							String year = dateParts[2];
							String month = dateParts[3];
							String date = year + "/"  + month;
							AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
							row.setDate(date);
							row.setDayOfWeek("all");
							row.setType("daily readings");
							row.setUrl(agesOcmcBaseUrl + href);
							result.addRow(row);
						}
					}
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}	
	
	public AgesIndexTableData  toReactTableDataFromHtml() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Document booksIndexDoc = null;
		Document servicesIndexDoc = null;
		Document dayIndexDoc = null;
		Document readingsIndexDoc = null;
		Connection datesIndexConnection = null;
		Connection servicesIndexConnection = null;
		Connection booksIndexConnection = null;
		Connection readingsIndexConnection = null;
		try {
			datesIndexConnection = Jsoup.connect(servicesIndex);
			servicesIndexDoc = datesIndexConnection
					.timeout(60*1000)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.maxBodySize(0).get();
			Elements days = servicesIndexDoc.select("a.index-day-link");
			for (Element day : days) {
				String href = day.attr("href");
				String year = href.substring(8, 12);
				String month = href.substring(12, 14);
				String monthDay = href.substring(14,16);
				String date = year + "/" + month + "/" + monthDay;
				String dayOfWeekName = day.text().trim().split("-")[1];
				servicesIndexConnection = Jsoup.connect(baseUrl + href);
				dayIndexDoc = servicesIndexConnection
						.timeout(60*1000)
						.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
						.maxBodySize(0).get();
				Elements services = dayIndexDoc.select("a.index-file-link");
				for (Element service : services) {
					String fileHref = service.attr("href");
					String[] hrefParts = fileHref.split("/");
					if (hrefParts.length == 8) {
						if (hrefParts[0].equals("h")) {
							if (hrefParts[6].equals("gr-en")) {
								AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
								row.setDate(date);
								row.setDayOfWeek(dayOfWeekName);
								row.setType(hrefParts[5]);
								row.setUrl(baseUrl + fileHref);
								result.addRow(row);
							}
						}
					}
				}
			}
			booksIndexConnection = Jsoup.connect(booksIndex);
			booksIndexDoc = booksIndexConnection
					.timeout(60*1000)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.maxBodySize(0).get();
			Element menu = booksIndexDoc.getElementById("dcs_tree");
			Elements anchors = menu.select("li > ul > li > ul > li");
			for (Element anchor : anchors) {
				if (anchor.hasAttr("dcslink")) {
					String fileHref =  anchor.attr("dcslink");
					String[] hrefParts = fileHref.split("/");
					if (hrefParts.length == 5) {
						if (hrefParts[0].equals("h")) {
							if (hrefParts[3].equals("gr-en")) {
								AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
								row.setDate("any");
								row.setDayOfWeek("any");
								row.setType(hrefParts[2]);
								row.setUrl(baseUrl + fileHref);
								result.addRow(row);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}

	public AgesIndexTableData  toReactTableDataFromDailyReadingHtmlUsingJson() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Connection serviceIndexConnection = null;
		try {
			serviceIndexConnection = Jsoup.connect(this.agesOcmcJsonIndex);
			String s  = serviceIndexConnection
					.timeout(60*1000)
					.maxBodySize(0)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.ignoreContentType(true)
					.execute()
					.body();
			result = this.gson.fromJson(s, AgesIndexTableData.class);
		} catch (Exception e) {
				ErrorUtils.report(logger, e);
		}
		return result;
	}
	
	public AgesIndexTableData  toReactTableDataFromJson(String fileType) throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Connection serviceIndexConnection = null;
		Connection booksIndexConnection = null;
		Document booksIndexDoc = null;
		try {
			serviceIndexConnection = Jsoup.connect(this.jsonservicesIndex);
			String s  = serviceIndexConnection
					.timeout(60*1000)
					.maxBodySize(0)
					.ignoreContentType(true)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.execute()
					.body();
			JsonParser p = new JsonParser();
			JsonObject o = p.parse(s).getAsJsonObject();
			// years
			JsonArray years = o.get("years").getAsJsonArray();
			for (JsonElement year : years) {
				for (Entry<String, JsonElement> yearEntry : year.getAsJsonObject().entrySet()) {
					// months
					JsonArray months = yearEntry.getValue().getAsJsonArray();
					for (JsonElement month : months) {
						for (Entry<String, JsonElement> monthEntry : month.getAsJsonObject().entrySet()) {
							// days
							JsonArray days = monthEntry.getValue().getAsJsonArray();
							for (JsonElement day : days) {
								for (Entry<String, JsonElement> dayEntry : day.getAsJsonObject().entrySet()) {
									// service types
									JsonArray serviceTypes = dayEntry.getValue().getAsJsonArray();
									for (JsonElement serviceType : serviceTypes) {
										for (Entry<String, JsonElement> serviceTypeEntry : serviceType.getAsJsonObject().entrySet()) {
											// service language combinations
											JsonArray serviceLanguages = serviceTypeEntry.getValue().getAsJsonArray();
											for (JsonElement serviceLanguage : serviceLanguages) {
												for (Entry<String, JsonElement> serviceLanguageEntry : serviceLanguage.getAsJsonObject().entrySet()) {
													JsonObject serviceLanguageEntryValue = serviceLanguageEntry.getValue().getAsJsonArray().get(0).getAsJsonObject();
													String fileHref = serviceLanguageEntryValue.get("href").getAsString();
													String type = serviceLanguageEntryValue.get("type").getAsString();
													if (serviceLanguageEntry.getKey().startsWith("GR-EN") && type.startsWith(fileType)) {
														String theYear = fileHref.substring(4, 8);
														String theMonth = fileHref.substring(9, 11);
														String monthDay = fileHref.substring(12,14);
														String date = theYear + "/" + theMonth + "/" + monthDay;
														String dayOfWeekName = dayEntry.getKey().trim().split("-")[1];
														String [] hrefParts = fileHref.split("/");
														if (hrefParts.length == 8) {
																	String [] filenameParts = hrefParts[7].split("\\.");
																	String serviceCode = filenameParts[filenameParts.length-2];
																	AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
																	row.setLanguages(serviceLanguageEntry.getKey());
																	row.setFileType(type);
																	row.setDate(date);
																	row.setDayOfWeek(dayOfWeekName);
																	row.setType(serviceTypeEntry.getKey());
																	row.setUrl(baseUrl + fileHref);
																	result.addRow(row);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			booksIndexConnection = Jsoup.connect(booksIndex);
			booksIndexDoc = booksIndexConnection
					.timeout(60*1000)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.maxBodySize(0)
					.get();
			Element menu = booksIndexDoc.getElementById("dcs_tree");
			Elements anchors = menu.select("li > ul > li > ul > li");
			for (Element anchor : anchors) {
				if (anchor.hasAttr("dcslink")) {
					String fileHref =  anchor.attr("dcslink");
					String[] hrefParts = fileHref.split("/");
					if (hrefParts.length == 5) {
						if (hrefParts[0].equals("h")) {
							if (hrefParts[3].equals("gr-en")) {
								AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
								row.setDate("any");
								row.setDayOfWeek("any");
								row.setType(hrefParts[2]);
								row.setUrl(baseUrl + fileHref);
								result.addRow(row);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}
}
