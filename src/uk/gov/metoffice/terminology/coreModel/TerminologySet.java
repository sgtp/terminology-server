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

public interface TerminologySet extends TerminologyEntity{
	
	/**
	 * Add the collection (set, register) to set of collections known for the default version.
	 * @param myCollection
	 */
	public void registerContainedCollection(TerminologySet myCollection) ;
	
	/**
	 * Add the collection (set, register) to set of collections known for the given version.
	 * @param myCollection
	 * @param myVersion
	 * @param containedVersion
	 */
	public  void registerContainedCollection(TerminologySet myCollection, String myVersion, String containedVersion);

	/**
	 * return true is this collection (set, register) is not part of another register (now or in the past!)
	 * @return
	 */
	public abstract boolean isRoot() ;

	/**
	 * Add the individual (code) to set of individuals known for the default version.
	 * @param myIndividual
	 */
	public void registerContainedIndividual(TerminologyIndividual myIndividual) ;
	
	/**
	 * Add the individual (code) to set of individuals known for the given version.
	 * @param myIndividual
	 */
	public  void registerContainedIndividual(TerminologyIndividual myIndividual, String myVersion, String containedVersion);
	
	/**
	 * Remove the individual (code) to set of individuals known for the default version.
	 * @param toRemove the individual to remove
	 */
	public void unregisterContainedIndividual(TerminologyIndividual toRemove) ;
		
	/**
	 * Remove the individual (code) to set of individuals known for the given version.
	 * @param toRemove the individual to remove
	 * @param myVersion
	 */
	public void unregisterContainedIndividual(TerminologyIndividual myIndividual, String myVersion, String containedVersion);
	
	/**
	 * Get all individuals contained in this collection in the default version
	 * @return
	 */
	public Set<TerminologyIndividual> getIndividuals();

	/**
	 * Get all individuals contained in this collection in the given version
	 * @param version
	 * @return
	 */
	public Set<TerminologyIndividual> getIndividuals(String version) ;

	/**
	 * Get all collections (sub-registers) for the default version
	 * @return
	 */
	public Set<TerminologySet> getCollections() ;

	/**
	 * Get all collections (sub-registers) for the given version
	 * @param version
	 * @return
	 */
	public abstract Set<TerminologySet> getCollections(String version) ;

	/**
	 * Returns the union all sub-registers ever contained in any version, without duplicates.
	 * @return
	 */
	public Set<TerminologySet> getAllKnownContainedCollections() ;
	
	/**
	 * Returns the union all individuals ever contained in any version, without duplicates.
	 * @return
	 */
	public Set<TerminologyIndividual> getAllKnownContainedInviduals() ;






}
