package switches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ioc.liturgical.ws.app.ServiceProvider;


public class AgesHtmlToOslwSwitch {
	private static final Logger logger = LoggerFactory.getLogger(AgesHtmlToOslwSwitch.class);

	/**
	 * Maps AGES HTML class names and tag names to the
	 * corresponding OSLW command.
	 * 
	 * If you add an OSLW command here, make sure it exists in the
	 * oslw-liturgical-text.sty file.  Update it in the git
	 * oslw-system-tex project and push it to Github.
	 * 
	 * Any servers using it will then need to be updated from Github.  Put the
	 * updated file in /usr/local/ocmc/docker/pdf/data/system
	 * 
	 * @param command
	 * @return
	 */
	public static String getOslw(String command) {
		String strCommand = command;
		String parts[] = strCommand.split("kvp"); // throw away 'kvp readonly'
		try {
			strCommand = parts[0].trim();
		} catch (Exception e) {
			// ignore
		}
		strCommand = strCommand.replace("-optional", "");
		String result = "";
		switch (strCommand) {
		case "-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p": {
			result = "Para";
			break;
		}
		case "p-actor": {
		// {actors}{Priest} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "ActorSingleLine";
			break;
		}
		case "p-actor-rubric": {
		// {actors}{Priest}{rubrical}{InALowVoice} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "ActorRubric";
			break;
		}
		case "p-alttext": {
		// {ps}{psa17.v2.text}{ps}{psa17.v3a.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "AltText";
			break;
		}
		case "p-alttext-designation": {
		// {ps}{psa17.v2.text}{ps}{psa17.v3a.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "AltText";
			break;
		}
		case "p-alttext-italicsred": {
		// {eu.lichrysbasil}{euLI.Key1206.rubric} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "RubricItalics";
			break;
		}
		case "p-alttext-rubric-italics": {
		// {rubrical}{Or}{ps}{psa117.v1.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "AltVerse";
			break;
		}
		case "p-bmc_collapse": {
		// {template.titles}{clps.prayers.top} (http://www.agesinitiatives.com/dcs/public/dcs/h/c/matinsordinary/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_eothinongospel_position1": {
		// {website.preferences.titles}{mc.eothinongospel.position1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_eothinongospel_position2": {
		// {website.preferences.titles}{mc.eothinongospel.position2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_kontakion_position1": {
		// {website.preferences.titles}{mc.kontakion.position1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_kontakion_position12": {
		// {website.preferences.titles}{mc.kontakion.position12} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_kontakion_position2": {
		// {website.preferences.titles}{mc.kontakion.position2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_magnificat_modeofcanon": {
		// {website.preferences.titles}{mc.magnificat.modeofcanon} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_magnificat_modeokatavasia": {
		// {website.preferences.titles}{mc.magnificat.modeofkatavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_matins_close": {
		// {website.preferences.titles}{mc.matins.close} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_matins_dismissal": {
		// {website.preferences.titles}{mc.matins.dismissal} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_matins_end_before_dismissal": {
		// {website.preferences.titles}{mc.matins.end.before.dismissal} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_matins_end_no_dismissal": {
		// {website.preferences.titles}{mc.matins.end.no.dismissal} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode1_katavasia": {
		// {website.preferences.titles}{mc.ode1katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode1_me_canon1": {
		// {website.preferences.titles}{mc.ode1mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode1_me_canon2": {
		// {website.preferences.titles}{mc.ode1mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode1_oc_canon1": {
		// {website.preferences.titles}{mc.ode1occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode3_katavasia": {
		// {website.preferences.titles}{mc.ode3katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode3_kathisma": {
		// {website.preferences.titles}{mc.ode3.kathisma} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode3_litany": {
		// {website.preferences.titles}{mc.ode3.litany} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode3_me_canon1": {
		// {website.preferences.titles}{mc.ode3mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode3_me_canon2": {
		// {website.preferences.titles}{mc.ode3mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode3_oc_canon1": {
		// {website.preferences.titles}{mc.ode3occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode4_katavasia": {
		// {website.preferences.titles}{mc.ode4katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode4_me_canon1": {
		// {website.preferences.titles}{mc.ode4mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode4_me_canon2": {
		// {website.preferences.titles}{mc.ode4mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode4_oc_canon1": {
		// {website.preferences.titles}{mc.ode4occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode5_katavasia": {
		// {website.preferences.titles}{mc.ode5katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode5_me_canon1": {
		// {website.preferences.titles}{mc.ode5mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode5_me_canon2": {
		// {website.preferences.titles}{mc.ode5mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode5_oc_canon1": {
		// {website.preferences.titles}{mc.ode5occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode6_katavasia": {
		// {website.preferences.titles}{mc.ode6katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode6_litany": {
		// {website.preferences.titles}{mc.ode6.litany} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode6_me_canon1": {
		// {website.preferences.titles}{mc.ode6mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode6_me_canon2": {
		// {website.preferences.titles}{mc.ode6mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode6_oc_canon1": {
		// {website.preferences.titles}{mc.ode6occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode7_katavasia": {
		// {website.preferences.titles}{mc.ode7katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode7_me_canon1": {
		// {website.preferences.titles}{mc.ode7mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode7_me_canon2": {
		// {website.preferences.titles}{mc.ode7mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode7_oc_canon1": {
		// {website.preferences.titles}{mc.ode7occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode8_katavasia": {
		// {website.preferences.titles}{mc.ode8katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode8_me_canon1": {
		// {website.preferences.titles}{mc.ode8mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode8_me_canon2": {
		// {website.preferences.titles}{mc.ode8mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode8_oc_canon1": {
		// {website.preferences.titles}{mc.ode8occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode9_katavasia": {
		// {website.preferences.titles}{mc.ode9katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode9_me_canon1": {
		// {website.preferences.titles}{mc.ode9mecanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode9_me_canon2": {
		// {website.preferences.titles}{mc.ode9mecanon2} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_ode9_oc_canon1": {
		// {website.preferences.titles}{mc.ode9occanon1} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-bmc_odes1345678_katavasia": {
		// {website.preferences.titles}{mc.odes1345678katavasia} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-boldred": {
		// {misc}{book.Menaion.name} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "ParaRedBold";
			break;
		}
		case "p-break": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Break";
			break;
		}
		case "p-chant": {
			result = "Para";
			break;
		}
		case "p-chapverse": {
		// {le.ep.va.baptism}{levaBAP.Epistle.chapverse} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Chapverse";
			break;
		}
		case "p-chapverse-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-LectionaryDate": {
			result = "Section";
			break;
		}
		case "p-designation": {
		// {eu.baptism}{euBAP.title} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Designation";
			break;
		}
		case "p-designationdesignation": {
			result = "Designation";
			break;
		}
		case "p-designation-designation": {
		// {eu.baptism}{euBAP.title} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Designation";
			break;
		}
		case "p-designation-designation-designation": {
		// {eu.baptism}{euBAP.title} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Designation";
			break;
		}
		case "p-designationdesignationdesignation": {
			result = "Designation";
			break;
		}
		case "p-designation-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-designation-key-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-designation-key-key-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-designation-rubric": {
			result = "Designation";
			break;
		}
		case "p-dialog": {
		// {prayers}{enarxis01} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Dialog";
			break;
		}
		case "p-dialog-bold": {
			result = "DialogBold";
			break;
		}
		case "p-dialog-italics": {
		// {prayers}{res04p} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "DialogItalics";
			break;
		}
		case "p-dialog-italics-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-dialog-italics-red": {
		// {prayers}{EisToOnoma}{prayers}{res04p}{rubrical}{Thrice} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "DialogItalicsRed"; 
			break;
		}
		case "p-dialog-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-dialog-key-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-dialog-key-key-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-dialog-key-key-key-key-key-key-key-key-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/memorial/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-dialog-key-key-key-key-key-key-key-key-key-red-key~-key-key-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-dialog-key-red-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/memorial/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-dialog-red": {
		// {eu.baptism}{euBAP.Key0107.text}{rubrical}{Thrice} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "DialogJumbo";
			break;
		}
		case "p-dialogzero": {
		// {prayers}{DoxaSoiChriste2} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/smallwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_collapse": {
		// {template.titles}{clps.prayers.bottom} (http://www.agesinitiatives.com/dcs/public/dcs/h/c/matinsordinary/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_eothinongospel_position1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_eothinongospel_position2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_kontakion_position1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_kontakion_position12": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_kontakion_position2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_magnificat_modeofcanon": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_magnificat_modeofkatavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_matins_close": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_matins_dismissal": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_matins_end_before_dismissal": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_matins_end_no_dismissal": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode1_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode1_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode1_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode1_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode3_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode3_kathisma": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode3_litany": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode3_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode3_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode3_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode4_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode4_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode4_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode4_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode5_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode5_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode5_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode5_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode6_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode6_litany": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode6_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode6_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode6_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode7_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode7_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode7_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode7_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode8_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode8_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode8_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode8_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode9_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode9_me_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode9_me_canon2": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_ode9_oc_canon1": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-emc_odes1345678_katavasia": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-heirmos": {
		// {he.h.m1}{SouITropaiouchos.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Heirmos";
			break;
		}
		case "p-heirmosheirmos": {
			result = "Hiermos";
			break;
		}
		case "p-heirmos-designation": {
			result = "Designation";
			break;
		}
		case "p-heirmos-red": {
			result = "HeirmosRubric";
			break;
		}
		case "p-heirmos-red-designation": {
			result = "Designation";
			break;
		}
		case "p-hymn": {
			result = "Hymn";
			break;
		}
		case "p-hymnhymn": {
			result = "Hymn";
			break;
		}
		case "p-hymn-designation": {
			result = "HymnVersion";
			break;
		}
		case "p-hymn designation": {
			result = "HymnVersion";
			break;
		}
		case "p-hymn-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-hymn-key-red-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-hymn-red": {
		// {eu.baptism}{euBAP.Key0316.text}{rubrical}{Thrice} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "HymnRubric";
			break;
		}
		case "p-hymn-red-designation": {
		// {eu.baptism}{euBAP.Key0316.text}{rubrical}{Thrice} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "HymnRubricDesignation";
			break;
		}
		case "p-hymn-versiondesignation": {
			result = "Hymn";
			break;
		}
		case "p-hymnlinefirst": {
		// {ho.s07}{hoLI.TonStavronSou3.text}{ho.s07}{hoLI.TonStavronSou4.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "Hymn";
			break;
		}
		case "p-hymnlinelast": {
		// {ho.s07}{hoLI.TonStavronSou3.text}{ho.s07}{hoLI.TonStavronSou4.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "Hymn";
			break;
		}
		case "p-hymnlinelast-versiondesignation": {
		// {ho.s07}{hoLI.TonStavronSou3.text}{ho.s07}{hoLI.TonStavronSou4.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "Hymn";
			break;
		}
		case "p-hymnlinemiddle": {
		// {ho.s07}{hoLI.TonStavronSou3.text}{ho.s07}{hoLI.TonStavronSou4.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "Hymn";
			break;
		}
		case "p-inaudible": {
		// {eu.baptism}{euBAP.Key0201.text}{prayers}{res04} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "DialogInaudible";
			break;
		}
		case "p-inaudible-italics": {
		// {prayers}{res04p} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "DialogInaudibleItalics";
			break;
		}
		case "p-melody": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Melody";
			break;
		}
		case "p-melodymelody": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Melody";
			break;
		}
		case "p-melody-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-mixed": {
		// {prayers}{Doxa} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "Mixed";
			break;
		}
		case "p-mixedmixed": {
		// {prayers}{Doxa} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "Mixed";
			break;
		}
		case "p-mixed-black": {
			result = "BlackRedBoldRedItalic";
			break;
		}
		case "p-mixed-black-designation": {
			result = "BlackRedBoldRedItalic";
			break;
		}
		case "p-mixed-black-designation-designation": {
			result = "BlackRedBoldRedItalic";
			break;
		}
		case "p-mixed-black-designation-melody": {
			result = "BlackRedBoldRedItalic";
			break;
		}
		case "p-mixed-boldred": {
		// {template.titles}{li.html.tab} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "ParaRedBold";
			break;
		}
		case "p-mixed-designation": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/funeral_brightweek/gr-en/index.html)
			result = "MixedDesignation";
			break;
		}
		case "p-mixed-designation-designation": {
		// {titles}{Apolytikion}{titles}{TouStavrou} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "MixedDesignationDesignation";
			break;
		}
		case "p-mixed-designation-designation-designation": {
		// {titles}{Apolytikion}{titles}{TouStavrou} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "MixedDesignationDesignation";
			break;
		}
		case "p-mixed-designation-key~-mode-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-mixed-designation-melody": {
		// {misc}{Ode3}{he.h.m4}{EffrainetaiEpiSoi.name} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/unction/gr-en/index.html)
			result = "MixedModeDesignation";
			break;
		}
		case "p-mixed-designation-mode": {
		// {misc}{Ode1}{misc}{Mode8} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "MixedDesignationMode";
			break;
		}
		case "p-mixed-designation-mode-designation": {
		// {misc}{Ode1}{misc}{Mode1}{misc}{Heirmos} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "MixedDesignation";
			break;
		}
		case "p-mixed-designation-mode-melody": {
		// {misc}{Ode1}{misc}{Mode8} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "MixedDesignationMode";
			break;
		}
		case "p-mixed-italicsred": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "RubricItalics";
			break;
		}
		case "p-mixed-italicsred-italicsred": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "RubricItalics";
			break;
		}
		case "p-mixed-key-mode-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-mixed-mode": {
		// {prayers}{Doxa}{misc}{Mode8} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/funeral/gr-en/index.html)
			result = "MixedMode";
			break;
		}
		case "p-mixed-mode-designation": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/funeral_brightweek/gr-en/index.html)
			result = "MixedModeDesignation";
			break;
		}
		case "p-mixed-mode-designation-melody": {
		// {misc}{Mode4}{misc}{Ode1}{he.h.m4}{ThalassisToErythraion.name} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/unction/gr-en/index.html)
			result = "MixedModeDesignation";
			break;
		}
		case "p-mixed-mode-key~-melody-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-mixed-mode-melody": {
		// {misc}{Mode4}{he.a.m4}{EdokasSimeiosin.name} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/funeral/gr-en/index.html)
			result = "ModeMelody";
			break;
		}
		case "p-mixed-mode-rubric": {
		// {misc}{Mode2}{titles}{ITisEvdomados} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/13/ma2/gr-en/index.html)
			result = "Designation";
			break;
		}
		case "p-mixed-red": {
		// {titles}{Prokeimenon}{misc}{Mode4}{ps}{psa26.title} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Designation";
			break;
		}
		case "p-mixed-red-key~-key~-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-mixed-rubric": {
			result = "Rubric";
			break;
		}
		case "p-mode": {
		// {misc}{Mode1} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Mode";
			break;
		}
		case "p-mode-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-name": {
			result = "Designation";
			break;
		}
		case "p-namename": {
			result = "Designation";
			break;
		}
		case "p-namenamename": {
			result = "Designation";
			break;
		}
		case "p-prayer": {
		// {eu.baptism}{euBAP.Key0209.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Prayer";
			break;
		}
		case "p-prayer-designation": {
		// {prayers}{exc20}{prayers}{res04p} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/05/ve/gr-en/index.html)
			result = "PrayerVersion";
			break;
		}
		case "p-prayer-italics": {
		// {prayers}{exc20}{prayers}{res04p} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/05/ve/gr-en/index.html)
			result = "PrayerItalics";
			break;
		}
		case "p-prayerzero": {
		// {eu.baptism}{euBAP.Key0208.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Prayer";
			break;
		}
		case "p-reading": {
			result = "Reading";
			break;
		}
		case "p-reading-anum-reference": {
			result = "ReadingVersion";
			break;
		}
		case "p-reading-boldred": {
			result = "ReadingRedBold";
			break;
		}
		case "p-readingcenter": {
			result = "ReadingCenter";
			break;
		}
		case "p-readingcenterzero": {
			result = "ReadingCenter";
			break;
		}
		case "p-reading-designation": {
			result = "ReadingVersion";
			break;
		}
		case "p-reading-italics": {
			result = "ReadingItalics";
			break;
		}
		case "p-reading-italicsred": {
			result = "ReadingRedItalics";
			break;
		}
		case "p-reading-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-reading-key-red-key~": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-reading-red": {
		// {ho.s03}{hoMA.DoxologyVerse16.text}{rubrical}{Thrice} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/ma/gr-en/index.html)
			result = "ReadingRubric";
			break;
		}
		case "p-reading-rubric": {
		// {ps}{psa142.text}{properties}{version.designation} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/smallwaterblessing/gr-en/index.html)
			result = "ReadingRubric";
			break;
		}
		case "p-readingzero": {
		// {ps}{psa26.v1a.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "ReadingZero";
			break;
		}
		case "p-readingzero-designation": {
			result = "ReadingZero";
			break;
		}
		case "p-readingzero-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-rubric": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/funeral_brightweek/gr-en/index.html)
			result = "RubricItalics";
			break;
		}
		case "p-rubric-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-small": {
		// {eu.lichrysbasil}{euLI.Key0201.title} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "SmallTitle";
			break;
		}
		case "p-smallcenter": {
		// {eu.lichrysbasil}{euLI.Key0201.title} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			// result = "Designation";
			result = "SmallTitle";
			break;
		}
		case "p-source": {
		// {titles}{sourceMenaion} (http://www.agesinitiatives.com/dcs/public/dcs/h/s/2017/08/01/li/gr-en/index.html)
			result = "Source";
			break;
		}
		case "p-source0": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/funeral_brightweek/gr-en/index.html)
			result = "SourceZero";
			break;
		}
		case "p-titlezero": {
			result = "TitleZero";
			break;
		}
		case "p-verse": {
		// {ps}{bode8.v19.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Verse";
			break;
		}
		case "p-verse-boldred": {
		// {misc}{vVerse}{ps}{psa26.v1b.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "VerseRedBoldItalics";
			break;
		}
		case "p-verse-boldred-designation": {
		// {misc}{vVerse}{ps}{psa26.v1b.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "VerseRedBoldVersion";
			break;
		}
		case "p-verse-boldred-key~-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/greatwaterblessing/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-verse-designation": {
		// {misc}{vVerse}{ps}{psa26.v1b.text} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "VerseDesignation";
			break;
		}
		case "p-verse-key": {
		//  (http://www.agesinitiatives.com/dcs/public/dcs/h/b/memorial/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "span": {
		// {prayers}{DoxaSoiKyrie} (http://www.agesinitiatives.com/dcs/public/dcs/h/b/baptism/gr-en/index.html)
			result = "Undefined";
			break;
		}
		case "p-actor-italicsred": {
			result = "Undefined";
			break;
		}

		case "p-alttext-italicsred-italics": {
			result = "Undefined";
			break;
		}

		case "p-alttext-italicsred-italics-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-alttext-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode1_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode1_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode1_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode1_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode1_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode3_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode3_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode3_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode3_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode3_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode4_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode4_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode4_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode4_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode4_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode5_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode5_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode5_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode5_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode5_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode6_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode6_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode6_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode6_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode6_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode7_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode7_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode7_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode7_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode7_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode8_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode8_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode8_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode8_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode8_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode9_me_canon3": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode9_pa_canon": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode9_pe_canon1": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode9_pe_canon2": {
			result = "Undefined";
			break;
		}

		case "p-bmc_ode9_tr_canon1": {
			result = "Undefined";
			break;
		}

		case "p-boldred-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-chant-red-red": {
			result = "Undefined";
			break;
		}

		case "p-chant-red-red-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-chant-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-dialog-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-heirmos-red-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-heirmos-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-hymn-bold": {
			result = "Undefined";
			break;
		}

		case "p-hymn-italics": {
			result = "Undefined";
			break;
		}

		case "p-hymn-red-versiondesignation": {
			result = "Para";
			break;
		}

		case "p-mixed-bold": {
			result = "Undefined";
			break;
		}

		case "p-mixed-designation-designation-mode": {
			result = "Undefined";
			break;
		}

		case "p-mixed-designation-mode-designation-": {
			result = "Undefined";
			break;
		}

		case "p-mixed-designation-mode-melody-": {
			result = "Undefined";
			break;
		}

		case "p-mixed-melody": {
			result = "Melody";
			break;
		}

		case "p-mixed-mode-designation-": {
			result = "Undefined";
			break;
		}

		case "p-mixed-mode-mode": {
			result = "Undefined";
			break;
		}

		case "p-mixed-text": {
			result = "Mixed";
			break;
		}

		case "p-mixed-text-designation": {
			result = "MixedTextDesignation";
			break;
		}

		case "p-mixed-text-designation-designation": {
			result = "MixedTextDesignation";
			break;
		}

		case "p-mixed-text-designation-melody": {
			result = "MixedTextDesignationMelody";
			break;
		}

		case "p-mixed-text-melody": {
			result = "MixedTextMelody";
			break;
		}

		case "p-mixed-text-mode": {
			result = "MixedTextModeMelody";
			break;
		}

		case "p-mixed-text-mode-melody": {
			result = "MixedTextModeMelody";
			break;
		}

		case "p-mixed-text-rubric": {
			result = "MixedTextRubric";
			break;
		}

		case "p-mixed-text-text": {
			result = "Mixed";
			break;
		}

		case "p-mixed-text-text-designation": {
			result = "MixedTextTextDesignation";
			break;
		}

		case "p-mixed-text-text-mode": {
			result = "Undefined";
			break;
		}

		case "p-mixed-text-text-red-text": {
			result = "Undefined";
			break;
		}

		case "p-mixed-text-text-text": {
			result = "Undefined";
			break;
		}

		case "p-prayer-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-prayerzero-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-reading-anum-reference-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-reading-red-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-reading-versiondesignation": {
			result = "Para";
			break;
		}

		case "p-readingzero-versiondesignation": {
			result = "Para";
			break;
		}

		case "p-rubric-": {
			result = "Undefined";
			break;
		}

		case "p-source1": {
			result = "Undefined";
			break;
		}

		case "p-verse-bolditalicsred": {
			result = "Undefined";
			break;
		}

		case "p-verse-bolditalicsred-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-verse-boldred-rubric": {
			result = "Undefined";
			break;
		}

		case "p-verse-boldred-versiondesignation": {
			result = "Para";
			break;
		}

		case "p-verse-italics": {
			result = "Undefined";
			break;
		}

		case "p-verse-italicsred": {
			result = "Undefined";
			break;
		}

		case "p-verse-italicsred-versiondesignation": {
			result = "Undefined";
			break;
		}

		case "p-verse-versiondesignation": {
			result = "Para";
			break;
		}
		default: {
			result = "Unknown";
		}
	}
	if (result.equals("Undefined") || result.equals("Unknown")) {
		if (command.contains("bmc_")
		|| command.contains("emc_")) {
			// ignore it
		} else { // report the problem
			ServiceProvider.sendMessage("switches.AgesHtmlToOslw: " + result + " command  - "  + strCommand );
			logger.error(result + " command: " + strCommand);
		}

	}
	result = "\\lt" + result; 
	return result;
	}
}
