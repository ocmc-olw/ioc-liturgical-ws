package net.ages.alwb.utils.nlp.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import net.ages.alwb.utils.core.datastores.json.models.AbstractModel;

public class Phrase extends AbstractModel {
	@Expose public String text = "";
	@Expose public List<Token> tokens = new ArrayList<Token>();
	
	public Phrase(String text) {
		super();
		this.text = text;
	}
}
