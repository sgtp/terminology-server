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

package uk.gov.metoffice.terminology.management;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;

import uk.gov.metoffice.terminology.auth.AuthRegistryManager;
import uk.gov.metoffice.terminology.auth.AuthServer;
import uk.gov.metoffice.terminology.config.CoreConfig;
import uk.gov.metoffice.terminology.config.MetaLanguage;
import uk.gov.metoffice.terminology.coreModel.TerminologyEntity;
import uk.gov.metoffice.terminology.coreModel.TerminologyFactory;
import uk.gov.metoffice.terminology.coreModel.TerminologyIndividual;
import uk.gov.metoffice.terminology.coreModel.TerminologySet;
import uk.gov.metoffice.terminology.coreModel.Versioner;
import uk.gov.metoffice.terminology.exceptions.AuthException;
import uk.gov.metoffice.terminology.exceptions.RegistryManagerException;
import uk.gov.metoffice.terminology.utils.SimpleQueriesProcessor;

public class TerminologyManager {
	private static final int MODE_REPLACE = 1;
	private static final int MODE_ADD = 2;
	private static final int MODE_REMOVE = 3;

	
	TerminologyFactory myFactory;
	AuthServer myAuthServer;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public TerminologyManager(TerminologyFactory factory,AuthServer authServer) {
		myFactory=factory;
		myAuthServer=authServer;
	}
	
	public void replaceEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryManagerException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_REPLACE);
	}
	public void addToEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryManagerException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_ADD);

	}
	public void removeFromEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryManagerException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_REMOVE);		
	}
	
	public void amendEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description, int mode) throws AuthException, RegistryManagerException {
		if(!AuthRegistryManager.can(actionAuthor,RegistryPolicyConfig.terminologyAmendedActionURI,entityURI,myAuthServer,myFactory))
			throw new AuthException(actionAuthor,RegistryPolicyConfig.terminologyAmendedActionURI,entityURI);
		TerminologyEntity myEntity=checkEntityExistance(entityURI);
		if(myEntity==null) throw new RegistryManagerException("Unable to amend "+entityURI+" (entity does not exist)");
		String lastVersion=myEntity.getLastVersion();
		String preStatus=myEntity.getStateURI(lastVersion);
		String postStatus=preStatus;
		if(preStatus!=null)
			if(RegistryPolicyConfig.tm.updateTransitions.containsKey(preStatus))
				postStatus=RegistryPolicyConfig.tm.updateTransitions.get(preStatus);
		String newVersion=Versioner.createNextVersion(lastVersion);
		myEntity.registerVersion(newVersion);
		if(mode==MODE_REPLACE) 
			myEntity.replaceStatements(statsToReplace, newVersion);
		if(mode==MODE_ADD) 
			myEntity.getStatements(newVersion).add(myEntity.getStatements(lastVersion)).add(statsToReplace);
		if(mode==MODE_REMOVE)
			myEntity.getStatements(newVersion).add((myEntity.getStatements(lastVersion)).remove(statsToReplace));
		myEntity.setDefaultVersion(newVersion);
		if(postStatus!=null) myEntity.setStateURI(postStatus, newVersion);
		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		myEntity.setActionDate(dateFormat.format(date),newVersion);
		myEntity.setActionAuthorURI(actionAuthor, newVersion);
		if(description!=null) myEntity.setActionDescription(description,newVersion);
		myEntity.setActionURI(RegistryPolicyConfig.terminologyAmendedActionURI,newVersion);
		myEntity.linkVersions(lastVersion,newVersion);
		
	}
	
	public void validate(String entityURI, String actionAuthor, String description, boolean validate) throws AuthException, RegistryManagerException {
		if(validate) {
			if(!AuthRegistryManager.can(actionAuthor,RegistryPolicyConfig.validateAction,entityURI,myAuthServer,myFactory))
				throw new AuthException(actionAuthor,RegistryPolicyConfig.validateAction,entityURI);
		}
		else {
			if(!AuthRegistryManager.can(actionAuthor,RegistryPolicyConfig.invalidateAction,entityURI,myAuthServer,myFactory))
				throw new AuthException(actionAuthor,RegistryPolicyConfig.invalidateAction,entityURI);
		}
		
		TerminologyEntity myEntity=checkEntityExistance(entityURI);
		if(myEntity==null) throw new RegistryManagerException("Unable to change status to "+entityURI+" (entity does not exist)");
		String lastVersion=myEntity.getLastVersion();
		String preStatus=myEntity.getStateURI(lastVersion);
		System.out.println(">>>PRE: "+preStatus);
		if(preStatus==null) preStatus=RegistryPolicyConfig.nullState;
		String postStatus=null;
		if(validate) {
			if(RegistryPolicyConfig.tm.validateTransitions.containsKey(preStatus))
				postStatus=RegistryPolicyConfig.tm.validateTransitions.get(preStatus);
		} 
		if(!validate) {
			if(RegistryPolicyConfig.tm.invalidateTransitions.containsKey(preStatus))
				postStatus=RegistryPolicyConfig.tm.invalidateTransitions.get(preStatus);
		}
		
		if(postStatus==null) throw new RegistryManagerException("Unable to change status to "+entityURI+" (validation resulted didn't yield a state)");
		if(postStatus.equals(RegistryPolicyConfig.illegalState)) throw new RegistryManagerException("Unable to change status to "+entityURI+" (non viable transition)");

		String newVersion=Versioner.createNextVersion(lastVersion);
		myEntity.registerVersion(newVersion);
		myEntity.replaceStatements(myEntity.getStatements(lastVersion), newVersion);
		myEntity.setStateURI(postStatus,newVersion);
		Date date = new Date();
		myEntity.setActionDate(dateFormat.format(date),newVersion);
		myEntity.setActionAuthorURI(actionAuthor, newVersion);
		if(description!=null) myEntity.setActionDescription(description,newVersion);
		myEntity.linkVersions(lastVersion,newVersion);
		
		if(validate) 
			myEntity.setActionURI(RegistryPolicyConfig.validateAction,newVersion);

		else 
			myEntity.setActionURI(RegistryPolicyConfig.invalidateAction,newVersion);
		
		
	}
	

	

	
	public void addSubRegister() {
		
	}
	
	
	public void removeSubRegister() {
		
	}
	
	public void addTermToRegister(String codeURI, String registerURI, Model defaultEntityModel,String actionAuthor, String description, boolean isVersioned ) throws AuthException, RegistryManagerException {
	//////////////
		if(!AuthRegistryManager.can(actionAuthor,RegistryPolicyConfig.addItemAction,registerURI,myAuthServer,myFactory))
				throw new AuthException(actionAuthor,RegistryPolicyConfig.addItemAction,registerURI);
		TerminologySet myRegister=checkSetExistance(registerURI);
		if(myRegister==null) throw new RegistryManagerException("Unable to modify "+registerURI+" (register does not exist)");
		if(myFactory.terminologyIndividualExist(codeURI)) {
			// Note this is only for addding! Not for changing obsolete/valid status. In other words, a delete operation
			throw new RegistryManagerException("Code "+codeURI+" exists. Use \"move\" to change register");
		}
			
		String lastRegisterVersion=myRegister.getLastVersion();
		String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
		String postRegisterStatus=preRegisterStatus;
		if(preRegisterStatus!=null)
			if(RegistryPolicyConfig.tm.addTransitions.containsKey(preRegisterStatus))
				postRegisterStatus=RegistryPolicyConfig.tm.addTransitions.get(preRegisterStatus);
		String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
			
		//Now we create the entity
		TerminologyIndividual newTerm=myFactory.getOrCreateTerminologyIndividual(codeURI);
		newTerm.setDefaultVersion(newTerm.getLastVersion());
		if(isVersioned) newTerm.setIsVersioned(true);	
		newTerm.setStateURI(RegistryPolicyConfig.DEFAULT_CREATION_STATE,newTerm.getDefaultVersion());
		newTerm.setOwnerURI(actionAuthor);
		newTerm.setActionURI(RegistryPolicyConfig.addItemAction ,newTerm.getDefaultVersion());
		newTerm.setActionAuthorURI(actionAuthor,newTerm.getDefaultVersion());
		newTerm.setActionDescription("New term added to registry",newTerm.getDefaultVersion());
		newTerm.addStatements(defaultEntityModel,newTerm.getDefaultVersion());
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		newTerm.setActionDate(dateFormat.format(date),newTerm.getDefaultVersion());
		
		myRegister.registerVersion(newRegisterVersion);
		myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
			
		myRegister.registerContainedIndividual(newTerm, newRegisterVersion, newTerm.getDefaultVersion());
			
		if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
		myRegister.setActionAuthorURI(actionAuthor, newRegisterVersion);
		if(description!=null) myRegister.setActionDescription("Added term: "+newTerm.getURI(),newRegisterVersion);
		myRegister.setActionURI(RegistryPolicyConfig.addItemAction ,newRegisterVersion);
		myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
			
	}
		
		
	
	

	
	public void tagRelease(String authorURI, String tag, String description) throws AuthException {
		if(!AuthRegistryManager.can(authorURI,RegistryPolicyConfig.tagAction,null,myAuthServer,myFactory))
			throw new AuthException(authorURI,RegistryPolicyConfig.tagAction,null);
		TerminologySet[] roots=myFactory.getRootCollections();
		for(int i=0;i<roots.length;i++) myTag(roots[i],tag);
		//TODO we may want to register infos on tags somewhere.
		
	}
	


	private TerminologyEntity checkEntityExistance(String uri) {
		if(myFactory.terminologySetExist(uri)) return myFactory.getOrCreateTerminologySet(uri);
		if(myFactory.terminologyIndividualExist(uri)) return myFactory.getOrCreateTerminologyIndividual(uri);
		return null;
	}
	
	
	private TerminologySet checkSetExistance(String uri) {
		if(myFactory.terminologySetExist(uri)) return myFactory.getOrCreateTerminologySet(uri);
		return null;
	}
	
	
	private TerminologyIndividual checkIndividualExistance(String uri) {
		if(myFactory.terminologyIndividualExist(uri)) return myFactory.getOrCreateTerminologyIndividual(uri);
		return null;
	}
	
	private void myTag(TerminologySet set, String tag) {
		set.tagVersion(set.getLastVersion(),tag);
		Set<TerminologyIndividual>terms=set.getIndividuals(set.getLastVersion());
		Iterator<TerminologyIndividual>termIter=terms.iterator();
		while(termIter.hasNext()) {
			TerminologyIndividual term=termIter.next();
			term.tagVersion(term.getLastVersion(),tag);
		}
		
		Set<TerminologySet> children=set.getCollections(set.getLastVersion());
		Iterator<TerminologySet>childrenIter=children.iterator();
		while(childrenIter.hasNext()) {
			TerminologySet child=childrenIter.next();
			myTag(child,tag);
		}
		
		
	}

	public void delTermFromRegister(String termURI, String regURI,
			String actionAuthorURI, String description) throws AuthException, RegistryManagerException {
		
		if(!AuthRegistryManager.can(actionAuthorURI,RegistryPolicyConfig.delItemAction,regURI,myAuthServer,myFactory))
			throw new AuthException(actionAuthorURI,RegistryPolicyConfig.delItemAction,regURI);
	TerminologySet myRegister=checkSetExistance(regURI);
	if(myRegister==null) throw new RegistryManagerException("Unable to modify "+regURI+" (register does not exist)");
	TerminologyIndividual myTerm=checkIndividualExistance(termURI);
	if(myTerm==null) throw new RegistryManagerException("Code "+termURI+" does not exists.");
	if(!myRegister.getIndividuals(myRegister.getLastVersion()).contains(myTerm))
		throw new RegistryManagerException("Code "+termURI+" is not contained in the last version of "+regURI);
	// TODO maybe we should test the containment in the register here.
	
	String lastRegisterVersion=myRegister.getLastVersion();
	String lastTermVersion=myTerm.getLastVersion();
	
	String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
	String preTermStatus=myTerm.getStateURI(lastTermVersion);
	
	String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
	String newTermVersion=Versioner.createNextVersion(lastTermVersion);
	
	String postRegisterStatus=preRegisterStatus;
	if(preRegisterStatus!=null)
		if(RegistryPolicyConfig.tm.delRegTransitions.containsKey(preRegisterStatus))
			postRegisterStatus=RegistryPolicyConfig.tm.delRegTransitions.get(preRegisterStatus);
	
	String postTermStatus=preTermStatus;
	if(preTermStatus!=null)
		if(RegistryPolicyConfig.tm.delTermTransitions.containsKey(preTermStatus))
			postTermStatus=RegistryPolicyConfig.tm.delTermTransitions.get(preTermStatus);
	System.out.println(">>>pre Term Status: "+preTermStatus);
	System.out.println(">>>post Term Status: "+postTermStatus);
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	
	myRegister.registerVersion(newRegisterVersion);
	myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
	if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
	//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
	myRegister.setActionAuthorURI(actionAuthorURI, newRegisterVersion);
	if(description!=null) myRegister.setActionDescription("Deleted term: "+myTerm.getURI(),newRegisterVersion);
	myRegister.setActionURI(RegistryPolicyConfig.delItemAction ,newRegisterVersion);
	myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
	
	myTerm.registerVersion(newTermVersion);
	
	

		
	myTerm.setOwnerURI(actionAuthorURI);
	myTerm.setActionURI(RegistryPolicyConfig.delItemAction ,newTermVersion);
	myTerm.setActionAuthorURI(actionAuthorURI,newTermVersion);
	myTerm.setActionDescription("Term was removed from registry",newTermVersion);
	myTerm.addStatements(myTerm.getStatements(lastTermVersion),newTermVersion);
	myTerm.setActionDate(dateFormat.format(date),newTermVersion);
	myTerm.setStateURI(postTermStatus,newTermVersion); // Note this overwrites old statements!
	myTerm.linkVersions(lastTermVersion,newTermVersion);

	myRegister.unregisterContainedIndividual(myTerm, newRegisterVersion, newTermVersion);
	
		//////
		
	}
	
	
	
	public void superseedTermInRegister(String termURI,
			String superseedingTermURI, String regURI, 
			String actionAuthorURI, String description) throws AuthException, RegistryManagerException {
		// TODO Auto-generated method stub
		
		if(!AuthRegistryManager.can(actionAuthorURI,RegistryPolicyConfig.superseedAction,regURI,myAuthServer,myFactory))
			throw new AuthException(actionAuthorURI,RegistryPolicyConfig.superseedAction,regURI);
	TerminologySet myRegister=checkSetExistance(regURI);
	if(myRegister==null) throw new RegistryManagerException("Unable to modify "+regURI+" (register does not exist)");
	
	TerminologyIndividual myTerm=checkIndividualExistance(termURI);
	if(myTerm==null) throw new RegistryManagerException("Code "+termURI+" does not exists.");
	
	TerminologyIndividual superseedingTerm=checkIndividualExistance(superseedingTermURI);
	if(superseedingTerm==null) throw new RegistryManagerException("Code "+superseedingTermURI+" does not exist. Add it first.");
	
	
	
	String lastRegisterVersion=myRegister.getLastVersion();
	String lastTermVersion=myTerm.getLastVersion();
	String lastSuperseedingsTermVersion=superseedingTerm.getLastVersion();
	
	String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
	String preTermStatus=myTerm.getStateURI(lastTermVersion);
	String preSuperseedingTermStatus=superseedingTerm.getStateURI(lastSuperseedingsTermVersion);

	
	String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
	String newTermVersion=Versioner.createNextVersion(lastTermVersion);
	String newSuperseedingVersion=Versioner.createNextVersion(lastSuperseedingsTermVersion);

	String postRegisterStatus=preRegisterStatus;
	if(preRegisterStatus!=null)
		if(RegistryPolicyConfig.tm.delRegTransitions.containsKey(preRegisterStatus))
			postRegisterStatus=RegistryPolicyConfig.tm.delRegTransitions.get(preRegisterStatus);

	String postTermStatus=preTermStatus;
	if(preTermStatus!=null)
		if(RegistryPolicyConfig.tm.superseededTransitions.containsKey(preTermStatus))
			postTermStatus=RegistryPolicyConfig.tm.superseededTransitions.get(preTermStatus);

	String postSuperseedingTermStatus=preSuperseedingTermStatus;
	if(preSuperseedingTermStatus!=null)
		if(RegistryPolicyConfig.tm.superseederTransitions.containsKey(preSuperseedingTermStatus))
			postSuperseedingTermStatus=RegistryPolicyConfig.tm.superseederTransitions.get(preSuperseedingTermStatus);
	if(postSuperseedingTermStatus.equals(RegistryPolicyConfig.illegalState)) throw new RegistryManagerException(superseedingTermURI+" is not in a viable state for superseeding "+termURI);

	
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	
	myRegister.registerVersion(newRegisterVersion);
	myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
	if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
	myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
	myRegister.setActionAuthorURI(actionAuthorURI, newRegisterVersion);
	if(description!=null) myRegister.setActionDescription("Deleted term: "+myTerm.getURI(),newRegisterVersion);
	myRegister.setActionURI(RegistryPolicyConfig.delItemAction ,newRegisterVersion);
	myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
	
	
	
	myTerm.registerVersion(newTermVersion);
	myTerm.setStateURI(postTermStatus,newTermVersion);
	myTerm.setOwnerURI(actionAuthorURI);
	myTerm.setActionURI(RegistryPolicyConfig.superseedAction ,newTermVersion);
	myTerm.setActionAuthorURI(actionAuthorURI,newTermVersion);
	myTerm.setActionDescription("Term was removed from registry and superseeded by "+superseedingTermURI,newTermVersion);
	myTerm.addStatements(myTerm.getStatements(lastTermVersion),newTermVersion);
	myTerm.getStatements(newTermVersion).add(myTerm.getResource(),MetaLanguage.superseededBy,superseedingTerm.getResource());
	myTerm.setActionDate(dateFormat.format(date),newTermVersion);
	myTerm.linkVersions(lastTermVersion,newTermVersion);


	superseedingTerm.registerVersion(newSuperseedingVersion);
	superseedingTerm.setStateURI(postTermStatus,newSuperseedingVersion);
	superseedingTerm.setActionURI(RegistryPolicyConfig.superseedAction ,newSuperseedingVersion);
	superseedingTerm.setActionAuthorURI(actionAuthorURI,newSuperseedingVersion);
	superseedingTerm.setActionDescription("Term superseeded "+termURI,newSuperseedingVersion);
	superseedingTerm.addStatements(superseedingTerm.getStatements(lastSuperseedingsTermVersion),newSuperseedingVersion);
	superseedingTerm.getStatements(newSuperseedingVersion).add(superseedingTerm.getResource(),MetaLanguage.superseeds,myTerm.getResource());
	superseedingTerm.setActionDate(dateFormat.format(date),newSuperseedingVersion);
	superseedingTerm.linkVersions(lastSuperseedingsTermVersion,newSuperseedingVersion);
	
	
	
	myRegister.unregisterContainedIndividual(myTerm, newRegisterVersion, newTermVersion);
	
		//////	
	
	
	
	
	}


	
}
