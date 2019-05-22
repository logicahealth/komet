package sh.isaac.solor.direct.umls.apis.umls;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.restassured.response.Response;
import org.apache.http.client.utils.URIBuilder;
import sh.isaac.solor.direct.umls.Terminologies;
import sh.isaac.solor.direct.umls.apis.ApiClient;
import sh.isaac.solor.direct.umls.model.TerminologyCode;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;

/**
 * 4/10/2019
 *
 * @author kmaulden
 */
public class UmlsApiClient extends ApiClient {

    private final String username;
    private final String password;
    private RestTicketClient rTC;
    private String tgt;

    public UmlsApiClient(Terminologies sourceTerminology, List<Terminologies> targetTerminologies, String username, String password) {
        super(sourceTerminology, targetTerminologies);
        this.username = username;
        this.password = password;
        this.rTC = new RestTicketClient(this.username, this.password);
        this.tgt = rTC.getTgt();
    }

    @Override
    public List<TerminologyCode> getTargetCodes(String code) {
        try {
            //TODO refine search to include targetTerminolgies -> problem with including more than one targetTerminology
            //TODO change http client to same as fluffy enigma
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("uts-ws.nlm.nih.gov/rest")
                    .setPath("/crosswalk/current/source/" + super.getSourceTerminology().toString() + "/" + code)
                    .setParameter("ticket", this.rTC.getST(this.tgt))
//                    .setParameter("targetSource", super.getTargetTerminolgies().get(0).toString())
                    .build();

            Response response = given().get(uri);
            String output = response.getBody().asString();
            com.jayway.jsonpath.Configuration config = Configuration.builder().mappingProvider(new JacksonMappingProvider()).build();

            DocumentContext parsedOutput = JsonPath.using(config).parse(output);
            TerminologyCode[] terminologyCodes = parsedOutput.read("$.result", TerminologyCode[].class);


            return Arrays.asList(terminologyCodes);

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
