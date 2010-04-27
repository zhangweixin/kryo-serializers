/*
* Copyright 2010 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.javakaffee.kryoserializers.wicket;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import com.esotericsoftware.kryo.serialize.ReferenceFieldSerializer;

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import de.javakaffee.kryoserializers.KryoTest;

/**
 * A general test for several wicket serializations that don't require
 * specific serializers.
 * 
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public class WicketTest {

    public static final String SERIALIZED_CLASS_NAME = MarkupContainer.class.getName() + "$ChildList";
    
    private Kryo _kryo;
    private WicketTester _wicketTester;

    @BeforeTest
    @SuppressWarnings( "unchecked" )
    protected void beforeTest() {
        _kryo = new KryoReflectionFactorySupport() {
            
            @Override
            public Serializer newSerializer( final Class type ) {
                if ( SERIALIZED_CLASS_NAME.equals( type.getName() ) ) {
                    return new FieldSerializer( this, type );
                }
                return super.newSerializer( type );
            }
            
            @Override
            protected Serializer newDefaultSerializer( final Class type ) {
                return new ReferenceFieldSerializer( this, type );
            }
            
        };
        _kryo.setRegistrationOptional( true );
        
        final WebApplication application = new WebApplication() {
            
            @Override
            public Class<? extends Page> getHomePage() {
                return null;
            }
            
        };

        _wicketTester = new WicketTester( application );
    }
    
    @AfterTest
    protected void afterTest() {
        _wicketTester.destroy();
    }

    /**
     * Tests that MarkupContainer.ChildList is serialized/deserialized correctly.
     * It needs ReflectionFactory support, ReferenceFieldSerializer as default
     * serializer and the FieldSerializer for MarkupContainer.ChildList (instead of
     * default CollectionSerializer).
     * 
     * @throws Exception
     */
    @Test( enabled = true )
    public void testMarkupContainerChildList() throws Exception {
        final MarkupContainer markupContainer = new WebMarkupContainer("foo");
        markupContainer.add( new Label( "label1", "foo" ) );
        markupContainer.add( new Label( "label", "hello" ) );
        final byte[] serialized = new ObjectBuffer( _kryo, 1024 * 1024 ).writeObject( markupContainer );
        final MarkupContainer deserialized = new ObjectBuffer( _kryo, 1024 * 1024 ).readObject( serialized, markupContainer.getClass() );
        KryoTest.assertDeepEquals( deserialized, markupContainer );
    }

}
