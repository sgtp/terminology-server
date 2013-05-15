package net.metarelate.terminology.publisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.publisher.templateElements.DummyTemplateElement;
import net.metarelate.terminology.publisher.templateElements.StringTemplateElement;
import net.metarelate.terminology.publisher.templateElements.TemplateElement;
import net.metarelate.terminology.publisher.templateElements.TemplateTermElement;
import net.metarelate.terminology.publisher.templateElements.TemplateFixedElement;

import net.metarelate.terminology.utils.SSLogger;

public class TemplateManager {
	private final String openTag="<!-- tmtOpen>";
	private final String closeTag="<tmtClose -->";
	
	private File templateFile=null;
	Map<String,ArrayList<TemplateElement>> indTemplates=new Hashtable<String,ArrayList<TemplateElement>>();
	Map<String,ArrayList<TemplateElement>> setTemplates=new Hashtable<String,ArrayList<TemplateElement>>();
	Map<String,ArrayList<TemplateElement>> preTemplates=new Hashtable<String,ArrayList<TemplateElement>>();
	Map<String,ArrayList<TemplateElement>> postTemplates=new Hashtable<String,ArrayList<TemplateElement>>();

	public TemplateManager(String templateFileDir) throws ConfigurationException, IOException {
		super();
		SSLogger.log("Loading templates in "+templateFileDir,SSLogger.DEBUG);
		templateFile=new File(templateFileDir);
		if(!templateFile.isDirectory()) throw new ConfigurationException("No template at "+templateFileDir);
		File[] templateFiles=templateFile.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fName) {
				if(fName.endsWith(".tmt")) return true;
				else return false;
			}});
		for(File templateFile:templateFiles) {
			//TODO we should constrain to known languge strings
			String tfName=templateFile.getName();
			SSLogger.log("template: "+tfName ,SSLogger.DEBUG);
			String[] tfNameBits=tfName.split("\\.");
			if(tfNameBits.length!=3) throw new ConfigurationException("Invalid template file name for "+tfName);
			if(tfNameBits[0].equalsIgnoreCase("code")) {
				indTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			else if(tfNameBits[0].equalsIgnoreCase("set")) {
				setTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			else if(tfNameBits[0].equalsIgnoreCase("pre")) {
				preTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			else if(tfNameBits[0].equalsIgnoreCase("post")) {
				postTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			
		}
	}
	
	public String getPageForLang(String language, TerminologySet set, String version, int level) throws ConfigurationException {
		return expandTermTemplate(setTemplates,language,set,version, level);
	}
	public String getPageForLang(String language, TerminologyIndividual ind, String version, int level) throws ConfigurationException {
		return expandTermTemplate(indTemplates,language,ind,version,level); 	// TODO we don't care about levels here
	}
	public String getIntroForLang(String language,String tag) throws ConfigurationException {
		return expandFixedTemplate(preTemplates,language,tag);
	}
	public String getClosingForLang(String language,String tag) throws ConfigurationException {
		return expandFixedTemplate(postTemplates,language,tag);
	}
	
	


	private String expandTermTemplate(Map<String,ArrayList<TemplateElement>> templateMap, String language, TerminologyEntity entity, String version, int level) throws ConfigurationException {
		if(templateMap.get(language)==null) {
			language=CoreConfig.DEFAULT_LANGUAGE;
			if(templateMap.get(language)==null) throw new ConfigurationException("No suitable template defined for "+entity.getURI());
		}
		StringBuilder answer=new StringBuilder();
		for(TemplateElement t:templateMap.get(language)) answer.append(((TemplateTermElement)t).render(entity, version, level));
		return answer.toString();
	}
	
	private String expandFixedTemplate(Map<String,ArrayList<TemplateElement>> templateMap, String language, String tag) throws ConfigurationException {
		if(templateMap.get(language)==null) {
			language=CoreConfig.DEFAULT_LANGUAGE;
			if(templateMap.get(language)==null) throw new ConfigurationException("No suitable template defined for pre or post block");
		}
		StringBuilder answer=new StringBuilder();
		for(TemplateElement t:templateMap.get(language)) answer.append(((TemplateFixedElement)t).render(tag));
		return answer.toString();
	}
	
	private ArrayList<TemplateElement> parseTemplate(File templateFile) throws IOException {
		ArrayList<TemplateElement> bits=new ArrayList<TemplateElement>();
		String templateString=readFileAsString(templateFile);
		int runningIndex=0;
		System.out.println(templateString); //TODO test
		while(runningIndex<templateString.length()) {
			System.out.println("Running index: "+runningIndex);
			int firstBit=templateString.indexOf(openTag,runningIndex);
			int secondBit=templateString.indexOf(closeTag,runningIndex);
			System.out.println("First bit: "+firstBit); //TODO test
			System.out.println("Second bit: "+secondBit); //TODO test
			if(firstBit<0) {
				bits.add(new StringTemplateElement(templateString.substring(runningIndex)));
				runningIndex=templateString.length()+1;
				break;
			}
			if(firstBit>runningIndex) bits.add(new StringTemplateElement(templateString.substring(runningIndex,firstBit)));
			//TODO here we should parse the real block
			if(firstBit>0 && secondBit>0) bits.add(new DummyTemplateElement(templateString.substring(firstBit+openTag.length(),secondBit)));
			runningIndex=secondBit+openTag.length();
			
		}
		return bits;
	}
	
	private String readFileAsString(File fileToRead) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (fileToRead));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");
	    
	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }
	    reader.close();
	    return stringBuilder.toString();
	}

	public String[] getLanguages() {
		return indTemplates.keySet().toArray(new String [0]);
	}
	
	

	

}