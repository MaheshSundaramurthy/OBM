/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
@Provider
public class BatchProvider extends PerRequestTypeInjectableProvider<Context, Batch> {

	private final BatchDao batchDao;

	@Inject
	private BatchProvider(BatchDao batchDao) {
		super(Batch.class);

		this.batchDao = batchDao;
	}

	@Override
	public Injectable<Batch> getInjectable(ComponentContext ic, Context a) {
		return new AbstractHttpContextInjectable<Batch>() {

			@Override
			public Batch getValue(HttpContext c) {
				MultivaluedMap<String, String> pathParameters = c.getUriInfo().getPathParameters();
				String batchId = pathParameters.getFirst("batchId");

				if (batchId == null) {
					throw new WebApplicationException(Status.BAD_REQUEST);
				}

				Batch batch = null;
				ObmDomain domainInRequest = (ObmDomain) c.getProperties().get(ObmDomainProvider.DOMAIN_KEY);

				try {
					batch = batchDao.get(Batch.Id.valueOf(batchId), domainInRequest);
				} catch (DaoException e) {
					throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
				} catch (BatchNotFoundException e) {
					throw new WebApplicationException(Status.NOT_FOUND);
				} catch (DomainNotFoundException e) {
					throw new WebApplicationException(Status.NOT_FOUND);
				}

				return batch;
			}

		};
	}

}
