/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/
package org.eclipse.californium.plugtests;

import java.net.SocketException;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.network.interceptors.OriginTracer;
import org.eclipse.californium.plugtests.resources.Create;
import org.eclipse.californium.plugtests.resources.DefaultTest;
import org.eclipse.californium.plugtests.resources.Large;
import org.eclipse.californium.plugtests.resources.LargeCreate;
import org.eclipse.californium.plugtests.resources.LargePost;
import org.eclipse.californium.plugtests.resources.LargeSeparate;
import org.eclipse.californium.plugtests.resources.LargeUpdate;
import org.eclipse.californium.plugtests.resources.Link1;
import org.eclipse.californium.plugtests.resources.Link2;
import org.eclipse.californium.plugtests.resources.Link3;
import org.eclipse.californium.plugtests.resources.LocationQuery;
import org.eclipse.californium.plugtests.resources.LongPath;
import org.eclipse.californium.plugtests.resources.MultiFormat;
import org.eclipse.californium.plugtests.resources.Observe;
import org.eclipse.californium.plugtests.resources.ObserveLarge;
import org.eclipse.californium.plugtests.resources.ObserveNon;
import org.eclipse.californium.plugtests.resources.ObservePumping;
import org.eclipse.californium.plugtests.resources.ObserveReset;
import org.eclipse.californium.plugtests.resources.Path;
import org.eclipse.californium.plugtests.resources.Query;
import org.eclipse.californium.plugtests.resources.Separate;
import org.eclipse.californium.plugtests.resources.Shutdown;
import org.eclipse.californium.plugtests.resources.Validate;

import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;
import org.eclipse.californium.core.network.CoapEndpoint;

import java.net.InetSocketAddress;
import java.net.InetAddress;


// ETSI Plugtest environment
//import java.net.InetSocketAddress;
//import org.eclipse.californium.core.network.CoAPEndpoint;


/**
 * The class PlugtestServer implements the test specification for the
 * ETSI IoT CoAP Plugtests, London, UK, 7--9 Mar 2014.
 */
public class PlugtestServer extends CoapServer {

    // exit codes for runtime errors
    public static final int ERR_INIT_FAILED = 1;

    public static void main(String[] args) {
    	
        // create server
        try {
            CoapServer server = new PlugtestServer();
            // ETSI Plugtest environment
//            server.addEndpoint(new CoAPEndpoint(new InetSocketAddress("::1", port)));
//            server.addEndpoint(new CoAPEndpoint(new InetSocketAddress("127.0.0.1", port)));
//            server.addEndpoint(new CoAPEndpoint(new InetSocketAddress("2a01:c911:0:2010::10", port)));
//            server.addEndpoint(new CoAPEndpoint(new InetSocketAddress("10.200.1.2", port)));

            InetSocketAddress sockAddr = new InetSocketAddress(5683);
            server.addEndpoint(new CoapEndpoint(sockAddr));

            InMemoryPskStore pskStore = new InMemoryPskStore();
            pskStore.setKey("Client_identity", "secretPSK".getBytes());

            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(5684));
            builder.setPskStore(pskStore);
            DTLSConnector dtlsConnector = new DTLSConnector(builder.build(), null);
            server.addEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));
            
            server.start();
            
            // add special interceptor for message traces
            for (Endpoint ep:server.getEndpoints()) {
            	ep.addInterceptor(new MessageTracer());
            	// Eclipse IoT metrics
            	ep.addInterceptor(new OriginTracer());
            }
            
        } catch (Exception e) {
            
            System.err.printf("Failed to create "+PlugtestServer.class.getSimpleName()+": %s\n", e.getMessage());
            System.err.println("Exiting");
            System.exit(ERR_INIT_FAILED);
        }
        
    }
    
    public PlugtestServer() throws SocketException {
    	
    	NetworkConfig.getStandard() // used for plugtest
			.setInt(NetworkConfig.Keys.MAX_MESSAGE_SIZE, 64) 
			.setInt(NetworkConfig.Keys.PREFERRED_BLOCK_SIZE, 64)
    		.setInt(NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_COUNT, 4)
    		.setInt(NetworkConfig.Keys.NOTIFICATION_CHECK_INTERVAL_TIME, 30000)
    		.setString(NetworkConfig.Keys.HEALTH_STATUS_PRINT_LEVEL, "INFO");
        
        // add resources to the server
        add(new DefaultTest());
        add(new LongPath());
        add(new Query());
        add(new Separate());
        add(new Large());
        add(new LargeUpdate());
        add(new LargeCreate());
        add(new LargePost());
        add(new LargeSeparate());
        add(new Observe());
        add(new ObserveNon());
        add(new ObserveReset());
        add(new ObserveLarge());
        add(new ObservePumping());
        add(new ObservePumping(Type.NON));
        add(new LocationQuery());
        add(new MultiFormat());
        add(new Link1());
        add(new Link2());
        add(new Link3());
        add(new Path());
        add(new Validate());
        add(new Create());
        add(new Shutdown());
    }
    
    
    // Application entry point /////////////////////////////////////////////////
    
}
