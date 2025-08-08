package org.acme;

import java.io.IOException;

import jakarta.xml.bind.JAXBException;

import org.acme.xml.Action;
import org.acme.xml.MyObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MyTest {

    @Test
    void test() throws IOException, JAXBException {
        String xmlMessage = FileHelper.readFileContent("test.xml");
        MyObject myObject = XmlHelper.unmarshal(MyObject.class, xmlMessage);
        Assertions.assertEquals("test", myObject.getName());
        Assertions.assertEquals(Action.CREATE, myObject.getAction());
    }

}