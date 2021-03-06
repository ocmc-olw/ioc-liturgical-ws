package net.ages.alwb.utils.transformers.adapters;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import org.ocmc.ioc.liturgical.utils.FileUtils;

public class MetaTemplateToInterlinearTest {

	@Test
	public void test() {
		String path = "/Volumes/ssd2/templates";
		String out =  "/Volumes/ssd2/canBeRemoved/temp/";
		MetaTemplateToInterlinear xform = null;
		Map<String,String> x = new TreeMap<String,String>();
		
		try {
			for (File f : FileUtils.getFilesInDirectory(path, "json")) {
				xform = new MetaTemplateToInterlinear (
						FileUtils.fileAsString(f)
						);
				System.out.println(xform.getTex().toString());
				FileUtils.writeFile(out + f.getName()+".tex", xform.getTex().toString());
				assertTrue(xform.getMap().size() > 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
