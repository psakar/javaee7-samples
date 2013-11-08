/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javaee7.jaxrs.client;

import static org.junit.Assert.*;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Arquillian.class)
@RunAsClient
public class MyResourceTest {


    private static final String DEPLOYMENT = "jaxrs-client";

    @Deployment
    public static WebArchive getDeployment() {

        /*
        File[] runtimeDependencies = Maven.resolver().resolve().withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class,  DEPLOYMENT + ".war")
            .addClass(SimpleResource.class)
            .addClass(LocatingResource.class)
            .addAsLibraries(runtimeDependencies)
            .addAsWebResource(new File("src/main/webapp/index.jsp"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/applicationContext.xml"))
            ;
        archive.as(ZipExporter.class).exportTo(new File(DEPLOYMENT + ".war"), true);
        */
        WebArchive archive =  ShrinkWrap.create(ZipImporter.class,  DEPLOYMENT + ".war").importFrom(new File("target/" + DEPLOYMENT + ".war"))
            .as(WebArchive.class);

        return archive;
    }

    private WebTarget target;
    private Client client;

    @Before
    public void setUp() {
        client = ClientBuilder.newClient();
        target = client.target("http://localhost:8080/" + DEPLOYMENT + "/webresources/persons");
    }

    /**
     * Test of getList method, of class MyResource.
     */
    @Test
    public void test1PostAndGet() {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
        map.add("name", "Penny");
        map.add("age", "1");
        target.request().post(Entity.form(map));

        map.clear();
        map.add("name", "Leonard");
        map.add("age", "2");
        target.request().post(Entity.form(map));

        map.clear();
        map.add("name", "Sheldon");
        map.add("age", "3");
        target.request().post(Entity.form(map));

        Person[] list = target.request().get(Person[].class);
        assertEquals(3, list.length);

        assertEquals("Penny", list[0].getName());
        assertEquals(1, list[0].getAge());

        assertEquals("Leonard", list[1].getName());
        assertEquals(2, list[1].getAge());

        assertEquals("Sheldon", list[2].getName());
        assertEquals(3, list[2].getAge());
    }

    /**
     * Test of getPerson method, of class MyResource.
     */
    @Test
    public void test2GetSingle() {
        Person p = target
                .path("{id}")
                .resolveTemplate("id", "1")
                .request(MediaType.APPLICATION_XML)
                .get(Person.class);
        assertEquals("Leonard", p.getName());
        assertEquals(2, p.getAge());
    }

    /**
     * Test of putToList method, of class MyResource.
     */
    @Test
    public void test3Put() {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
        map.add("name", "Howard");
        map.add("age", "4");
        target.request().post(Entity.form(map));

        Person[] list = target.request().get(Person[].class);
        assertEquals(4, list.length);

        assertEquals("Howard", list[3].getName());
        assertEquals(4, list[3].getAge());
    }

    /**
     * Test of deleteFromList method, of class MyResource.
     */
    @Test
    public void test4Delete() {
        target
                .path("{name}")
                .resolveTemplate("name", "Howard")
                .request()
                .delete();
        Person[] list = target.request().get(Person[].class);
        assertEquals(3, list.length);
    }

    @Test
    public void test5ClientSideNegotiation() {
        String json = target.request().accept(MediaType.APPLICATION_JSON).get(String.class);
        assertEquals("[{\"name\":\"Penny\",\"age\":1},{\"name\":\"Leonard\",\"age\":2},{\"name\":\"Sheldon\",\"age\":3}]", json);
    }

    @Test
    public void test6DeleteAll() {
        Person[] list = target.request().get(Person[].class);
        for (Person p : list) {
            target
                    .path("{name}")
                    .resolveTemplate("name", p.getName())
                    .request()
                    .delete();
        }
        list = target.request().get(Person[].class);
        assertEquals(0, list.length);
    }

}
