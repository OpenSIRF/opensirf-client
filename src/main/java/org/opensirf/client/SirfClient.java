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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.opensirf.catalog.SIRFCatalog;
import org.opensirf.container.ProvenanceInformation;
import org.opensirf.format.GenericUnmarshaller;
import org.opensirf.jaxrs.config.SIRFConfiguration;
import org.opensirf.obj.PreservationObjectInformation;

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
	
	private Response doRequest(String method, String uri) {
		Client client = ClientBuilder.newClient();
		WebTarget resource = client.target("http://" + endpoint + "/sirf/" + uri);
		request = resource.request();
		request.accept(mediaType);
		return request.method(method);
	}
	
	private Response doGet(String uri) {
		return doRequest("GET", uri);
	}
	
	private <T> Response doPut(String uri, Entity<T> o) {
		return doRequest("PUT", uri, o);
	}
	
	private <T> Response doPut(String uri) {
		return doRequest("PUT", uri, Entity.text(""));
	}
	
	private <T> Response doDelete(String uri) {
		return doRequest("DELETE", uri, null);
	}
	
	private <T> T getObject(String uri, Class<T> c) {
		return doGet(uri).readEntity(c);
	}

	public byte[] getPreservationObject(String containerName, String uuid) {
		String previousMediaType = new String(mediaType);
		mediaType = MediaType.MEDIA_TYPE_WILDCARD;
		byte[] po = getObject("container/" + containerName + "/" + uuid + "/data", byte[].class);
		mediaType = previousMediaType;
		return po;
	}
	
	public ProvenanceInformation getProvenance(String containerName) {
		String previousMediaType = new String(mediaType);
		mediaType = MediaType.MEDIA_TYPE_WILDCARD;
		String provResponse = getObject("container/" + containerName + "/provenance.po.json/data",
				String.class);
		mediaType = previousMediaType;
		return GenericUnmarshaller.unmarshal("application/json", provResponse, 
				ProvenanceInformation.class);
	}
	
	public PreservationObjectInformation getPreservationObjectInformation(String containerName,
			String uuid) {
		return getObject("container/" + containerName + "/" + uuid,
				PreservationObjectInformation.class);
	}
	
	public SIRFConfiguration getConfiguration() {
		String configResponse = getObject("config", String.class);
		return GenericUnmarshaller.unmarshal("application/json", configResponse,
				SIRFConfiguration.class);
	}
	
	public Response createContainer(String containerName) {
		return doPut("container/" + containerName);
	}
	
	public Response deleteContainer(String containerName) {
		return doDelete("container/" + containerName);
	}
	
	public SIRFCatalog getCatalog(String containerName) {
		return getObject("container/" + containerName + "/catalog", SIRFCatalog.class);
	}
	
	public Response pushCatalog(String containerName, SIRFCatalog catalog) {
		return putObjectMultipart("catalog", "container/" + containerName + "/catalog", catalog);
	}

	private <T> Response putObjectMultipart(String formField, String uri, T o) {
		FormDataMultiPart mp = new FormDataMultiPart();
		MultiPart multipartEntity = mp.field(formField, o, MediaType.APPLICATION_JSON_TYPE);
		Response r = doPut(uri, Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA));
		
		try {
			mp.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return r;
	}
	
	public Response deletePreservationObject(String containerName, String uuid) {
		String uri = "container/" + containerName + "/" + uuid + "/data";
		return doDelete(uri);
	}
	
	public Response deletePreservationObjectInformation(String containerName, String uuid) {
		String uri = "container/" + containerName + "/" + uuid;
		return doDelete(uri);		
	}
	
	public Response pushPreservationObject(String containerName, PreservationObjectInformation poi,
			byte[] po) {
		String uri = "container/" + containerName + "/po";
		FormDataMultiPart mp = new FormDataMultiPart();
		FormDataBodyPart poiPart = new FormDataBodyPart("poi", poi, MediaType.APPLICATION_JSON_TYPE);
	    StreamDataBodyPart poPart = new StreamDataBodyPart("inputstream", new ByteArrayInputStream(po));

		MultiPart multipartEntity = mp.bodyPart(poiPart).bodyPart(poPart);
		Response r = doPost(uri, Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA));
		
		try {
			mp.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return r;
	}

	private <T> Response doPost(String uri, Entity<T> o) {
		return doRequest("POST", uri, o);
	}
	
	private <T> Response doRequest(String method, String uri, Entity<T> e) {
		Client client = ClientBuilder.newClient();
		client.register(MultiPartFeature.class);
		WebTarget resource = client.target("http://" + endpoint + "/sirf/" + uri);
		request = resource.request();
		if(e != null) {
			request.header("Content-type", e.getMediaType());
		}
		return request.method(method, e);
	}
	
	private String mediaType;
	private String endpoint;
	private Builder request;
}
