package org.acme.xml;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private static final String NAMESPACE = "http://xml.acme.org/";

    private static final QName QNAME = new QName(NAMESPACE, "object");

    public ObjectFactory() {
    }

    public MyObject createMyObject() {
        return new MyObject();
    }

    @XmlElementDecl(namespace = NAMESPACE, name = "object")
    public JAXBElement<MyObject> createMyObject(MyObject value) {
        return new JAXBElement<>(QNAME, MyObject.class, null, value);
    }

}
