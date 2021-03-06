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
	
package uk.gov.metoffice.terminology.coreModel;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class TerminologyIndividualTDBImpl extends TerminologyEntityTDBImpl implements TerminologyIndividual{

	TerminologyIndividualTDBImpl(String uri,
			TerminologyFactoryTDBImpl factory) {
		super(uri, factory);
	}

	/**
	 * Unimplemented, possibly redundant
	 */
	public void unregisterContainerCollection(TerminologySet collection,
			String version) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 */
	public void cloneTo(TerminologyIndividual toTerm) {
		String[] versions=getVersions();
		Resource sourceResource=getResource();
		Resource targetResource=toTerm.getResource();
		for(int i=0;i<versions.length;i++) {
			toTerm.registerVersion(versions[i]);
			if(this.getActionAuthorURI(versions[i])!=null)toTerm.setActionAuthorURI(this.getActionAuthorURI(versions[i]),versions[i]);
			if(this.getActionDate(versions[i])!=null) toTerm.setActionDate(this.getActionDate(versions[i]),versions[i]);
			if(this.getActionDescription(versions[i])!=null)toTerm.setActionDescription(this.getActionDescription(versions[i]),versions[i]);
			if(this.getActionURI(versions[i])!=null)toTerm.setActionURI(this.getActionURI(versions[i]),versions[i]);
			if(this.getStateURI(versions[i])!=null)toTerm.setStateURI(this.getStateURI(versions[i]),versions[i]);
		
			if(this.getOwnerURI()!=null)toTerm.setOwnerURI(this.getOwnerURI());
			if(this.getLocalNamespace()!=null)toTerm.setLocalNamespace(this.getLocalNamespace());
			//if(this.getLabel()!=null)toTerm.setLabel(this.getLabel());
			if(this.getStatements(versions[i])!=null) {
				StmtIterator statementsToTransfer=this.getStatements(versions[i]).listStatements();
				while(statementsToTransfer.hasNext()) {
					Statement statToTransfer=statementsToTransfer.nextStatement();
					if(!(statToTransfer.getSubject().equals(sourceResource) || statToTransfer.getObject().equals(targetResource))) {
						toTerm.getStatements(versions[i]).add(statToTransfer);
					}
					else {
						Resource subject=statToTransfer.getSubject();
						Property prop=statToTransfer.getPredicate();
						RDFNode obj= statToTransfer.getObject();
						if(subject.equals(sourceResource)) subject=targetResource;
						if(obj.equals(sourceResource)) obj=targetResource;
						toTerm.getStatements(versions[i]).add(subject,prop,obj);
					}
				}
				//toTerm.addStatements(this.getStatements(versions[i]),versions[i]);
			}


		}
		

		
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 */
	public Set<TerminologySet> getContainers() {
		return getContainers(getDefaultVersion());
	}

	
	
}
