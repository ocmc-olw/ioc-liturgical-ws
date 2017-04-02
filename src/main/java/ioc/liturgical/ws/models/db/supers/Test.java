package ioc.liturgical.ws.models.db.supers;

import com.github.reinert.jjschema.Attributes;
import com.google.gson.annotations.Expose;

import ioc.liturgical.ws.annotations.UiWidget;
import ioc.liturgical.ws.constants.Constants;

public class Test extends LTKDb {
	
	private static double serialVersion = 1.1;
	private static String schema = Test.class.getSimpleName();

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Word or phrase that makes the reference")
	@Expose String referredByPhrase = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Word or phrase that is referred to")
	@Expose String referredToPhrase = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Notes on the Text")
	@Expose String text = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Vocabulary (Lexical Items)")
	@Expose String voc = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Literary Devices")
	@Expose String dev = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Literary Genre")
	@Expose String gen = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "History and Geography")
	@Expose String hge = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Ancient Texts")
	@Expose String anc = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Ancient Cultures")
	@Expose String cul = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Biblical Intertextuality")
	@Expose String bib = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Comparison of Versions / Synoptic Reading")
	@Expose String syn = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Peritestamental Literature")
	@Expose String ptes = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Jewish Tradition")
	@Expose String jew = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Christian Tradition")
	@Expose String chr = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Liturgy")
	@Expose String lit = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Theology")
	@Expose String theo = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Islam")
	@Expose String isl = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Literature")
	@Expose String litt = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Visual Arts")
	@Expose String vis = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Music")
	@Expose String mus = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Theater, Dance, and Film")
	@Expose String tdf = "";

	public Test(String library, String topic, String key) {
		super(library, topic, key, schema, serialVersion);
	}

	public static void main (String [] args) {
		Test t = new Test("a","b","c");
		System.out.println(t.toJsonString());
		System.out.println(t.toUiSchema());
	}

	public String getReferredByPhrase() {
		return referredByPhrase;
	}

	public void setReferredByPhrase(String referredByPhrase) {
		this.referredByPhrase = referredByPhrase;
	}

	public String getReferredToPhrase() {
		return referredToPhrase;
	}

	public void setReferredToPhrase(String referredToPhrase) {
		this.referredToPhrase = referredToPhrase;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getVoc() {
		return voc;
	}

	public void setVoc(String voc) {
		this.voc = voc;
	}

	public String getDev() {
		return dev;
	}

	public void setDev(String dev) {
		this.dev = dev;
	}

	public String getGen() {
		return gen;
	}

	public void setGen(String gen) {
		this.gen = gen;
	}

	public String getHge() {
		return hge;
	}

	public void setHge(String hge) {
		this.hge = hge;
	}

	public String getAnc() {
		return anc;
	}

	public void setAnc(String anc) {
		this.anc = anc;
	}

	public String getCul() {
		return cul;
	}

	public void setCul(String cul) {
		this.cul = cul;
	}

	public String getBib() {
		return bib;
	}

	public void setBib(String bib) {
		this.bib = bib;
	}

	public String getSyn() {
		return syn;
	}

	public void setSyn(String syn) {
		this.syn = syn;
	}

	public String getPtes() {
		return ptes;
	}

	public void setPtes(String ptes) {
		this.ptes = ptes;
	}

	public String getJew() {
		return jew;
	}

	public void setJew(String jew) {
		this.jew = jew;
	}

	public String getChr() {
		return chr;
	}

	public void setChr(String chr) {
		this.chr = chr;
	}

	public String getLit() {
		return lit;
	}

	public void setLit(String lit) {
		this.lit = lit;
	}

	public String getTheo() {
		return theo;
	}

	public void setTheo(String theo) {
		this.theo = theo;
	}

	public String getIsl() {
		return isl;
	}

	public void setIsl(String isl) {
		this.isl = isl;
	}

	public String getLitt() {
		return litt;
	}

	public void setLitt(String litt) {
		this.litt = litt;
	}

	public String getVis() {
		return vis;
	}

	public void setVis(String vis) {
		this.vis = vis;
	}

	public String getMus() {
		return mus;
	}

	public void setMus(String mus) {
		this.mus = mus;
	}

	public String getTdf() {
		return tdf;
	}

	public void setTdf(String tdf) {
		this.tdf = tdf;
	}

}
