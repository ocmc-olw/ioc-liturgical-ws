package net.ages.alwb.utils.transformers.adapters;

import org.ocmc.ioc.liturgical.schemas.models.db.docs.templates.Template;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.templates.TemplateNode;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.ocmc.ioc.liturgical.utils.LiturgicalDayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;

/**
 * Compiles a template's nodes.
 * - if it is a service, computes the liturgical day properties
 *   and applies any conditions it finds in the template nodes.
 * - recursively expands Insert_template and Insert_section.
 * - resolves all keys (if the value of key is an ID aka redirect, 
 *   looks up the ID and keeps doing this until the value is text.
 *   The orginal ID is replaced with the resolved ID.
 * 
 * @author mac002
 *
 */
public class TemplateNodeCompiler {
	private static final Logger logger = LoggerFactory.getLogger(TemplateNodeCompiler.class);
	private ExternalDbManager dbManager = null;
	private Template template = null;
	private LiturgicalDayProperties ldp = null;
	private boolean isService = false;

	public TemplateNodeCompiler(
			Template template
			, ExternalDbManager dbManager
			) {
		this.template = template;
		this.dbManager = dbManager;
	}

	public TemplateNodeCompiler(
			Template template
			, ExternalDbManager dbManager
			, String year
			) {
		this.template = template;
		this.dbManager = dbManager;
		try {
			if (template.getMonth() > 0 && template.getDay() > 0) {
				ldp.setDateTo(
						year
						, Integer.toString(template.getMonth())
						, Integer.toString(template.getDay()));
			}
			this.isService = true;
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
	}

	public TemplateNode getCompiledNodes() {
		TemplateNode root = dbManager.gson.fromJson(
				template.getNode()
				, TemplateNode.class
				);
		TemplateNode compiledNode = this.compileNode(root);
		return compiledNode;
	}

	private TemplateNode compileNode(TemplateNode node) {
		System.out.println(node.getTitle() + ": " + node.getSubtitle());
		TemplateNode result = new TemplateNode();
		result.setTitle(node.title);
		result.setSubtitle(node.subtitle);

		for (TemplateNode child : node.children) {
			switch (child.getTitle()) {
			case Actor:
				result.appendNode(compileNode(child));
				break;
			case APR:
				break;
			case AUG:
				break;
			case DAY_01:
				break;
			case DAY_02:
				break;
			case DAY_03:
				break;
			case DAY_04:
				break;
			case DAY_05:
				break;
			case DAY_06:
				break;
			case DAY_07:
				break;
			case DAY_08:
				break;
			case DAY_09:
				break;
			case DAY_10:
				break;
			case DAY_11:
				break;
			case DAY_12:
				break;
			case DAY_13:
				break;
			case DAY_14:
				break;
			case DAY_15:
				break;
			case DAY_16:
				break;
			case DAY_17:
				break;
			case DAY_18:
				break;
			case DAY_19:
				break;
			case DAY_20:
				break;
			case DAY_21:
				break;
			case DAY_22:
				break;
			case DAY_23:
				break;
			case DAY_24:
				break;
			case DAY_25:
				break;
			case DAY_26:
				break;
			case DAY_27:
				break;
			case DAY_28:
				break;
			case DAY_29:
				break;
			case DAY_30:
				break;
			case DAY_31:
				break;
			case DAY_32:
				break;
			case DAY_33:
				break;
			case DAY_34:
				break;
			case DAY_35:
				break;
			case DAY_36:
				break;
			case DAY_37:
				break;
			case DAY_38:
				break;
			case DAY_39:
				break;
			case DAY_40:
				break;
			case DAY_41:
				break;
			case DAY_42:
				break;
			case DAY_43:
				break;
			case DAY_44:
				break;
			case DAY_45:
				break;
			case DAY_46:
				break;
			case DAY_47:
				break;
			case DAY_48:
				break;
			case DAY_49:
				break;
			case DAY_50:
				break;
			case DAY_51:
				break;
			case DAY_52:
				break;
			case DAY_53:
				break;
			case DAY_54:
				break;
			case DAY_55:
				break;
			case DAY_56:
				break;
			case DAY_57:
				break;
			case DAY_58:
				break;
			case DAY_59:
				break;
			case DAY_60:
				break;
			case DAY_61:
				break;
			case DAY_62:
				break;
			case DAY_63:
				break;
			case DAY_64:
				break;
			case DAY_65:
				break;
			case DAY_66:
				break;
			case DAY_67:
				break;
			case DAY_68:
				break;
			case DAY_69:
				break;
			case DAY_70:
				break;
			case DEC:
				break;
			case Dialog:
				result.appendNode(compileNode(child));
				break;
			case FEB:
				break;
			case FRIDAY:
				break;
			case Hymn:
				result.appendNode(compileNode(child));
				break;
			case INSERT_SECTION:
				break;
			case INSERT_TEMPLATE:
				break;
			case JAN:
				break;
			case JUL:
				break;
			case JUN:
				break;
			case MAR:
				break;
			case MAY:
				break;
			case MODE_1:
				break;
			case MODE_2:
				break;
			case MODE_3:
				break;
			case MODE_4:
				break;
			case MODE_5:
				break;
			case MODE_6:
				break;
			case MODE_7:
				break;
			case MODE_8:
				break;
			case MONDAY:
				break;
			case NOV:
				break;
			case OCT:
				break;
			case OTHERWISE:
				break;
			case Paragraph:
				result.appendNode(compileNode(child));
				break;
			case Reading:
				result.appendNode(compileNode(child));
				break;
			case RID:
				// TODO: resolve the rid based on LDP and resolving all redirects
				child.setSubtitle(this.resolveId(child.getSubtitle()));
				result.appendNode(child);
				break;
			case Rubric:
				result.appendNode(compileNode(child));
				break;
			case SATURDAY:
				break;
			case SECTION:
				result.appendNode(compileNode(child));
				break;
			case SEP:
				break;
			case SID:
				// TODO: resolve SID by resolving all redirects
				child.setSubtitle(this.resolveId(child.getSubtitle()));
				result.appendNode(child);
				break;
			case SUNDAY:
				break;
			case TEMPLATE:
				result.appendNode(compileNode(child));
				break;
			case THURSDAY:
				break;
			case TITLE:
				break;
			case TUESDAY:
				break;
			case Verse:
				result.appendNode(compileNode(child));
				break;
			case WEDNESDAY:
				break;
			case WHEN_DATE_IS:
				break;
			case WHEN_DAY_NAME_IS:
				break;
			case WHEN_DAY_OF_MONTH_IS:
				break;
			case WHEN_EXISTS:
				break;
			case WHEN_LUKAN_CYCLE_DAY_IS:
				break;
			case WHEN_MODE_OF_WEEK_IS:
				break;
			case WHEN_MONTH_NAME_IS:
				break;
			case WHEN_MOVABLE_CYCLE_DAY_IS:
				break;
			case WHEN_PASCHA:
				break;
			case WHEN_PENTECOSTARIAN_DAY_IS:
				break;
			case WHEN_SUNDAYS_BEFORE_TRIODION:
				break;
			case WHEN_SUNDAY_AFTER_ELEVATION_OF_CROSS_DAY_IS:
				break;
			case WHEN_TRIODION_DAY_IS:
				break;
			default:
				break;
			}
		}
		return result;
	}
	
	/**
	 * TODO: add real code to this.  It is just stubbed out.
	 * @param id to resolved
	 * @return resolved ID
	 */
	public String resolveId(String id) {
		String result = id;
		return result;
	}
	
}
