package org.acme.xml;

import jakarta.xml.bind.annotation.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "object", propOrder = {})
public class MyObject {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    private Action action;

    private String name;

    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar date;

    @XmlSchemaType(name = "positiveInteger")
    private BigInteger value;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public XMLGregorianCalendar getDate() {
        return date;
    }

    public void setDate(XMLGregorianCalendar date) {
        this.date = date;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
