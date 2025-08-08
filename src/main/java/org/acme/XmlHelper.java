package org.acme;

import jakarta.transaction.SystemException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class XmlHelper {

    static ConcurrentHashMap<String, JAXBContext> contexts = new ConcurrentHashMap<String, JAXBContext>();

    public static <T> T unmarshal(Class<T> tClass, String xml) throws JAXBException {
        return unmarshal(tClass, new StringReader(xml));
    }

    public static <T> T unmarshal(Class<T> tClass, Reader in) throws JAXBException {
        return unmarshall(tClass, new StreamSource(in));
    }

    private static <T> T unmarshall(Class<T> tClass, Source source) throws JAXBException {
        Unmarshaller unmarshaller = getContextFromPackage(tClass).createUnmarshaller();
        return unmarshaller.unmarshal(source, tClass).getValue();
    }

    public static JAXBContext getContextFromPackage(Class<?> c) {
        final String pkg = c.getPackage().getName();
        String errorMsg = "unable to create jaxb context for " + c + ". Make sure to have a package " + pkg
                + " under src/main/resources/ that includes a file jaxb.index and with " + c.getSimpleName()
                + " on a seperate line";

        return getContext(pkg, errorMsg, () -> JAXBContext.newInstance(pkg));
    }

    static JAXBContext getContext(String key, String errorMsg, Callable<JAXBContext> lambda) {
        JAXBContext context = contexts.get(key);
        if (context != null)
            return context;

        try {
            context = lambda.call();
        } catch (Exception e) {
            throw new RuntimeException(errorMsg, e);
        }

        JAXBContext current = contexts.putIfAbsent(key, context);
        return current != null ? current : context;
    }


}
