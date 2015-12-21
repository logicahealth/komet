package gov.vha.isaac.ochre.query.provider;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Created by kec on 10/30/14.
 */
public class QuerySerializer {
    public static String marshall(Query q) throws JAXBException, IOException {
        JAXBContext ctx = JaxbForQuery.get();
        q.setup();
        Marshaller marshaller = JaxbForQuery.get().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter builder = new StringWriter();
        marshaller.marshal(q, builder);
        return builder.toString();
    }

    public static Query unmarshall(Reader xmlData) throws JAXBException {
        JAXBContext ctx = JaxbForQuery.get();

        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        Query query = (Query) unmarshaller.unmarshal(xmlData);
        return query;
    }
}
