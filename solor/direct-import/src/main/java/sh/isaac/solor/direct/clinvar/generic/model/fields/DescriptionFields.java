package sh.isaac.solor.direct.clinvar.generic.model.fields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface DescriptionFields {

    //conceptId	languageCode	typeId	term	caseSignificanceId

    String getConcept();
    void setConcept(String concept);

    String getLanguageCode();
    void setLanguageCode(String languageCode);

    String getType();
    void setType(String type);

    String getTerm();
    void setTerm(String term);

    String getCaseSignificance();
    void setCaseSignificance(String caseSignificance);


}
