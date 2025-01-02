
package com.cats.ir.redrathub;

/*
 * Copyright 2021 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import com.cats.ir.IRCommunicator;
import com.cats.ir.redrat.RedRatConstants;
import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObjectBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hubconnection pool using furious_objectPool.
 */
public class HubConnectionPool
{
    private static final Logger               logger = LoggerFactory.getLogger( HubConnectionPool.class );

    ObjectPool< RedRatHubCommunicator > connectionPool;
    private int                 poolSize = RedRatConstants.DEFAULT_POOL_SIZE;

    /**
     * Sort of like a latch that increments when IRCommunicator is borrowed and decremented when returned.
     * If this count starts to skew up then we might be leaking connections in a bad way.
     */
    AtomicLong active;
    /**
     * Define notion of unique id for when you borrowed a connection against the pool.
     */
    AtomicLong poolTransaction;
    private String hubIp;
    private Integer hubPort;

    public HubConnectionPool() {
	super();

    }
    public HubConnectionPool(String hubIp, Integer hubPort, AtomicLong active, AtomicLong poolTransaction){
        this.hubIp = hubIp;
        this.hubPort = hubPort;
        this.active = active;
        this.poolTransaction = poolTransaction;
        logger.info("Creating new HubConnectionPool");
        init();
    }
    public HubConnectionPool(String hubIp, Integer hubPort){
        this(hubIp, hubPort, new AtomicLong(0), new AtomicLong(0));
    }
    
    public void init()
    {
        readPoolSize();

        PoolSettings< RedRatHubCommunicator > poolSettings = 
        		new PoolSettings< RedRatHubCommunicator >( new PoolObject( hubIp, hubPort ) );
        poolSettings.min( poolSize ).max( poolSize ).maxWait( RedRatConstants.POOL_WAIT_TIME ) ; // wait 60 sec before
                                                       // timeout.
        connectionPool = poolSettings.pool();

        logger.info( "hubconnectionPool " + connectionPool );
    }

    private void readPoolSize()
    {
        Properties props = new Properties();
        try
        {
            props.load( HubConnectionPool.class.getClassLoader().getResourceAsStream( RedRatConstants.REDRAT_PROPERTIES_FILE ) );
            poolSize = Integer.parseInt( props.getProperty( RedRatConstants.REDRATHUB_POOL_SIZE ) );
            logger.info( "poolSize from properties file " + poolSize );
        }
        catch ( Exception e ) // specifically includes IOException,
                              // NumberFormatException
        {
            logger.warn( "Couldnt load redrat.props file " + e.getMessage() );
            poolSize = RedRatConstants.DEFAULT_POOL_SIZE;

            logger.info( "poolSize from default setting " + poolSize );
        }
    }

    public synchronized RedRatHubCommunicator getConnection()
    {
        long id = poolTransaction.incrementAndGet();
        logger.info("connectionPool.getConnection[{}]", id);
        RedRatHubCommunicator telnetConnection = null;
        try
        {
            telnetConnection = connectionPool.getObj();
            if(telnetConnection == null){
            	logger.warn( "connectionPool getConnection[{}] ",id);
            }
            else
            {
                active.incrementAndGet();
                telnetConnection.setTransactionId(id);
            	logger.trace( "connectionPool getConnection[{}]",id);
            }
        }
        catch ( PoolException e )
        {
            logger.warn( "connectionPool getConnection PoolException[{}] - {}", id, e.getMessage() );
        }

        return telnetConnection;
    }

    public void releaseConnection( IRCommunicator irCommunicator )
    {
        if(irCommunicator instanceof RedRatHubCommunicator)
        {
            logger.trace( "connectionPool.releaseConnection[{}]", (( RedRatHubCommunicator ) irCommunicator).getTransactionId());
            connectionPool.returnObj( ( RedRatHubCommunicator ) irCommunicator );
            active.decrementAndGet();
        }
    }

    public Long getActive() {
        return active.get();
    }
}

/**
 * Pool Object.
 *
 */
class PoolObject extends PoolableObjectBase< RedRatHubCommunicator >
{
    private static final Logger               logger = LoggerFactory.getLogger( HubConnectionPool.class );
    private String hubIp;
    private Integer hubPort;
    
    static AtomicLong instance = new AtomicLong(0);

    public PoolObject(String hubIp, Integer hubPort)
    {
    	this.hubIp = hubIp;
    	this.hubPort = hubPort;
    }

    @Override
    public RedRatHubCommunicator make() throws PoolException
    {
        long tmp = instance.incrementAndGet();
        logger.info("RedRatHubCommunicator.make({}) Start", tmp);
        RedRatHubCommunicator telnetConnection = new RedRatHubCommunicator( hubIp,
        		hubPort , RedRatConstants.REDRAT_PROMPT_STRING_1, tmp );
        logger.info("RedRatHubCommunicator.make({}) Complete[{}]", tmp, telnetConnection);
        return telnetConnection;
    }

    @Override
    public void activate( RedRatHubCommunicator t ) throws PoolException
    {
    	if(t != null){
	        try
	        {
                logger.info("telnetConnection.activate[{}]", t.getInstanceId());
	            t.connect( false );
	        } catch( Exception e) {
                logger.warn("Could not connect telnet by pool[{}] - {} ", t.getInstanceId(), e.getMessage());
            }
    	} else {
    	    logger.error("RedRatHubCommunicator is NULL");
        }
    }
    
    @Override
	public void passivate(RedRatHubCommunicator t) 
    {
        logger.info("telnetConnection.passivate[{}]", t.getInstanceId());
 	}
}
