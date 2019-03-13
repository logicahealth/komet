package sh.isaac.solor.direct.clinvar.model.fields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface DescriptionFields {

    //conceptId	languageCode	typeId	term	caseSignificanceId

    int getConcept();
    void setConcept(int concept);

    int getLanguageCode();
    void setLanguageCode(int languageCode);

    int getType();
    void setType(int type);

    String getTerm();
    void setTerm(String term);

    int getCaseSignificance();
    void setCaseSignificance(int caseSignificance);


}
