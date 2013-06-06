/* 
 (C) British Crown Copyright 2011 - 2012, Met Office

 This file is part of terminology-server.

 terminology-server is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 terminology-server is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with terminology-server. If not, see <http://www.gnu.org/licenses/>.
*/

package net.metarelate.terminology.config;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class MetaLanguage {
	
	private static Set<Resource> nonDataResources=new HashSet<Resource>();
	private static Set<Property> nonDataProperties=new HashSet<Property>();
	
	private static Set<Resource> filteredRForWebDisplay=new HashSet<Resource>();
	private static Set<Property> filteredPForWebDisplay=new HashSet<Property>();
	
	private static Set<Resource> filteredRForDataDisplay=new HashSet<Resource>();
	private static Set<Property> filteredPForDataDisplay=new HashSet<Property>();
	
	private static Set<Resource> filteredRForFormEdit=new HashSet<Resource>();
	private static Set<Property> filteredPForFormEdit=new HashSet<Property>();
	

	
	/********************************************************
	 * Utility String, properties, types
	 ********************************************************/
	public static final String rdfsLabelPropertyString="http://www.w3.org/2000/01/rdf-schema#label";
	
	public static final String rdfsTypePropertyString="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final Property rdfsTypeProperty=ResourceFactory.createProperty(rdfsTypePropertyString);
	
	public static final String skosConceptTypeString="http://www.w3.org/2004/02/skos/core#Concept";
	public static final Resource skosConceptType=ResourceFactory.createProperty(skosConceptTypeString);

	public static final String skosCollectionTypeString="http://www.w3.org/2004/02/skos/core#Collection";
	public static final Resource skosCollectionType=ResourceFactory.createProperty(skosCollectionTypeString);

	public static final String skosNarrowerPropertyString="http://www.w3.org/2004/02/skos/core#narrower";
	public static final Property skosNarrowerProperty=ResourceFactory.createProperty(skosNarrowerPropertyString);
	
	public static final String skosBroaderPropertyString="http://www.w3.org/2004/02/skos/core#broader";
	public static final Property skosBroaderProperty=ResourceFactory.createProperty(skosBroaderPropertyString);
	
	public static final String skosTopConceptPropertyString="http://www.w3.org/2004/02/skos/core#hasTopConcept";
	public static final Property skosTopConceptProperty=ResourceFactory.createProperty(skosTopConceptPropertyString);
	
	public static final String skosSchemeTypeString="http://www.w3.org/2004/02/skos/core#Scheme";
	public static final Resource skosSchemeType=ResourceFactory.createProperty(skosSchemeTypeString);
	
	public static final String rdfsCommentPropertyString="http://www.w3.org/2000/01/rdf-schema#comment";
	public static final Property rdfsCommentProperty = ResourceFactory.createProperty(rdfsCommentPropertyString);
	
	public static final String skosNotationPropertyString="http://www.w3.org/2004/02/skos/core#notation";
	public static final Property skosNotationProperty=ResourceFactory.createProperty(skosNotationPropertyString);
	
	public static final String dcReplacesString="http://purl.org/dc/terms/replaces";
	public static final Property dcReplacesProperty=ResourceFactory.createProperty(dcReplacesString);
	 
	public static final String dcReplacedByString="http://purl.org/dc/terms/isReplacedBy";
	public static final Property dcReplacedByProperty=ResourceFactory.createProperty(dcReplacedByString);
	
	
	
	/*******************************************************
	 * Data properties that are understood by the system.
	 *******************************************************/
	
	/**
	 * The property that indicates labels. Defaults to rdfs:label but can be re-defined.
	 */
	public static Property labelProperty=ResourceFactory.createProperty("http://metarelate.net/core/data/label");

	/**
	 * a definedIn b: a is a sub-register of b or a can be redefined by b
	 * defaults to a metarelate property but can be re-defined
	 */
	public static Property definedInProperty=ResourceFactory.createProperty("http://metarelate.net/core/data/definedIn");
	/**
	 * @see definedInProperty (symmetric)
	 */
	public static Property definesProperty=ResourceFactory.createProperty("http://metarelate.net/core/data/defines");
	/**
	 * Used as a typing property throughout the system
	 */
	public static Property typeProperty=rdfsTypeProperty;
	/**
	 * The property to be considered as a comment for visualization purposes
	 */
	public static Property commentProperty=ResourceFactory.createProperty("http://metarelate.net/core/data/comment");
	/**
	 * the property generally used to denote IDs
	 */
	public static Property notationProperty=ResourceFactory.createProperty("http://metarelate.net/core/data/notation");
	
	
	public static Property hasStatusProperty=ResourceFactory.createProperty("http://metarelate.net/core/data/state");
	
	private static void initDataFilters() {
	
		filteredPForWebDisplay.add(definedInProperty);	//ad-hoc display
		filteredPForWebDisplay.add(definesProperty);	//ad-hoc display
		filteredPForWebDisplay.add(typeProperty);		//ad-hoc display
		filteredPForWebDisplay.add(commentProperty);	//ad-hoc display
		filteredPForWebDisplay.add(notationProperty);	//ad-hoc display
	
		filteredPForFormEdit.add(definedInProperty);	//Handled by design
		filteredPForFormEdit.add(definesProperty);		//Handled by design
	}
	
	/************************************************************************
	 * Data properties that are annotations used at model-building time only
	 ************************************************************************/
	/**
	 * declares a string that can be used to build URLs for the register or entity.
	 */
	public static final Property nameSpaceProperty=ResourceFactory.createProperty("http://metarelate.net/core/system/hasNamespace");	
	
	/**
	 * Entry to pragma specifications. For specific pargama configuration options, @see TerminologyModelBuilderConfig
	 */
	public static final Property pragmaProperty=ResourceFactory.createProperty("http://metarelate.net/config/hasPragma");
	public static void initConfigFilters() {
		nonDataProperties.add(nameSpaceProperty);
		nonDataProperties.add(pragmaProperty);
	}
	
	
	/************************************************************************
	 * Meta-properties (typically in the global graph only)
	 ************************************************************************/
	public static final Resource terminologySetType=ResourceFactory.createResource("http://metarelate.net/core/meta/TerminologySet");
	public static final Resource terminologyIndividualType=ResourceFactory.createResource("http://metarelate.net/core/meta/TerminologyIndividual");
	
	/**
	 * Property metadata. Whether it pertains to a concept or a code. This is held in the extraKnowledge (global) graph.
	 */
	public static final Property propertyHasFocus = ResourceFactory.createProperty("http://metarelate.net/config/propertyFocus");
	public static final Resource propertyCodeFocus = ResourceFactory.createResource("http://metarelate.net/config/codeRelatedProperty");
	public static final Resource propertyConceptFocus = ResourceFactory.createResource("http://metarelate.net/config/conceptRelatedProperty");
	
	
	/**
	 * Used to specify a version number. Understood at import time and used in data rendering, it is not part of the entity data,
	 * but it's a meta-property pertaining to a named graph.
	 */
	public static Property hasVersionProperty=ResourceFactory.createProperty("http://metarelate.net/core/meta/hasVersion");
	
	public static Property hasManagerProperty =ResourceFactory.createProperty("http://metarelate.net/code/meta/hasOwner");

	public static Property hasPreviousVersionProperty=ResourceFactory.createProperty("http://metarelate.net/code/structure/hasPreviousVersion");

	//public static final Property hasNonVersionModel = ResourceFactory.createProperty("http://metarelate.net/code/types/hasNonVersionModel");
	
	//public static final Property hasVersionModel =  ResourceFactory.createProperty("http://metarelate.net/code/types/hasVersionModel");

	public static Property superseeds=ResourceFactory.createProperty("http://metarelate.net/code/data/superseds");
	
	public static Property superseededBy=ResourceFactory.createProperty("http://metarelate.net/code/data/superseded");

	
	public static void initMetaFilters() {
		nonDataResources.add(terminologySetType);
		nonDataResources.add(terminologyIndividualType);
		nonDataProperties.add(hasVersionProperty);
		
		
		
		nonDataProperties.add(propertyHasFocus);
		nonDataResources.add(propertyCodeFocus);
		nonDataResources.add(propertyConceptFocus);
	}
	
	
	/************************************************************************
	 * Version-properties 
	 ************************************************************************/
	public static Property versionActionDateProperty=ResourceFactory.createProperty("http://metarelate.net/core/meta/hasActionDate");
	public static Property versionActionProperty=ResourceFactory.createProperty("http://metarelate.net/core/meta/hasAction");
	public static Property versionActorProperty=ResourceFactory.createProperty("http://metarelate.net/core/meta/hasActor");
	public static Property versionActionDescription=ResourceFactory.createProperty("http://metarelate.net/core/meta/hasActionDescription");
	public static final Property hasTag=ResourceFactory.createProperty("http://metarelate.net/core/structure/hasTag");

	
	
	public static void initVersionFilters() {
	
	}
	
	
	/**
	 * MetaLanguage config properties
	 */
	public static String overridesPropertyString="http://metarelate.net/config/overrides";
	public static Property overridesProperty=ResourceFactory.createProperty(overridesPropertyString);
	
	
	public static Model filterForWeb(Model ModelIn) {
		Model modelOut=ModelFactory.createDefaultModel();
		if(filteredPForWebDisplay==null) buildFilters();
		StmtIterator stats=ModelIn.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			if(!filteredPForWebDisplay.contains(stat.getPredicate()) && !filteredRForWebDisplay.contains(stat.getObject())) {
				modelOut.add(stat);
			}
		}
		return modelOut;
		
	}
	
	public static Model filterForData(Model ModelIn) {
		Model modelOut=ModelFactory.createDefaultModel();
		if(filteredPForDataDisplay==null) buildFilters();
		StmtIterator stats=ModelIn.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			if(!filteredPForDataDisplay.contains(stat.getPredicate()) && !filteredRForDataDisplay.contains(stat.getObject())) {
				modelOut.add(stat);
			}
		}
		return modelOut;
		
	}
	
	public static Model filterForModel(Model ModelIn) {
		Model modelOut=ModelFactory.createDefaultModel();
		if(nonDataProperties==null) buildFilters();
		StmtIterator stats=ModelIn.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			if(!nonDataProperties.contains(stat.getPredicate()) && !nonDataResources.contains(stat.getObject())) {
				modelOut.add(stat);
			}
		}
		return modelOut;
		
	}
	
	public static Model filterForEdit(Model ModelIn) {
		Model modelOut=ModelFactory.createDefaultModel();
		if(filteredPForFormEdit==null) buildFilters();
		StmtIterator stats=ModelIn.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			if(!filteredPForFormEdit.contains(stat.getPredicate()) && !filteredRForFormEdit.contains(stat.getObject())) {
				modelOut.add(stat);
			}
		}
		return modelOut;
	}
	public static Model filterForEditComplement(Model modelIn) {
		Model answer=ModelFactory.createDefaultModel();
		answer.add(modelIn);
		answer.remove(filterForEdit(modelIn));
		return answer;
	}
	
	

	
	private static void buildFilters() {
		nonDataResources.clear();
		nonDataProperties.clear();
		
		filteredRForWebDisplay.clear();
		filteredPForWebDisplay.clear();
		
		filteredRForDataDisplay.clear();
		filteredPForDataDisplay.clear();
		
		filteredRForFormEdit.clear();
		filteredPForFormEdit.clear();
		initDataFilters();
		initConfigFilters();
		initMetaFilters();
		initVersionFilters();
		
		
		
		
	}


	
	
		  
		       
		   


}