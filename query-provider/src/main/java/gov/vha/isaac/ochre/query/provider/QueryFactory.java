package gov.vha.isaac.ochre.query.provider;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by kec on 11/4/14.
 */
public class QueryFactory {


    public static Query createQuery() {
        return new QueryFromFactory();
    }

    @XmlRootElement(name = "query")
    public static class QueryFromFactory extends Query {
        @Override
        protected ForSetSpecification ForSetSpecification() {
            ForSetSpecification forSetSpec = new ForSetSpecification();
            forSetSpec.setForCollectionTypes(forCollectionTypes);
            forSetSpec.setCustomCollection(customCollection);
            return forSetSpec;
        }

        @Override
        public void Let() {
            // Set directly by Jaxb
        }

        @Override
        public Clause Where() {
            return rootClause[0];
        }
    }

}
