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

package uk.gov.metoffice.terminology.utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class UtilityDebugger {
	public static void printStatementsForURI(String uri, Model model) {
		StmtIterator stats=model.listStatements(ResourceFactory.createResource(uri),null,(RDFNode)null);
		while(stats.hasNext()) {
			Statement stat=stats.next();
			SSLogger.log("\t"+stat.getSubject()+"\n"+"\t"+stat.getPredicate()+"\t"+stat.getObject()+"\n",SSLogger.DEBUG);
		}
	}
}
