/*
 * OpenSIRF
 * 
 * Copyright IBM Corporation 2016.
 * All Rights Reserved.
 * 
 * MIT License:
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * Except as contained in this notice, the name of a copyright holder shall not
 * be used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization of the
 * copyright holder.
 */
package org.opensirf.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opensirf.jaxrs.config.SIRFConfiguration;

/**
 * @author pviana
 *
 */
public class SirfClient {

	public SirfClient(String endpoint) {
		this(endpoint, MediaType.APPLICATION_JSON);
	}
	
	public SirfClient(String endpoint, String mediaType) {
		this.endpoint = endpoint;
		this.mediaType = mediaType;
	}
	
	private Response doRequest(String uri) {
		Client client = ClientBuilder.newClient();
		WebTarget resource = client.target("http://" + endpoint + "/sirf/" + uri);
		request = resource.request();
		request.accept(mediaType);
		return request.get();
	}
	
	private <T> T getObject(String uri, Class<T> c) {
		return doRequest(uri).readEntity(c);
	}
	
	public SIRFConfiguration getConfiguration() {
		return getObject("config", SIRFConfiguration.class);
		//return doRequest("config").readEntity(SIRFConfiguration.class);
	}
	
	private String mediaType;
	private String endpoint;
	private Builder request;
}
