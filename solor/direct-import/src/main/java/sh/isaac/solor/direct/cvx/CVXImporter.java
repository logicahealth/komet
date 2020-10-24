package sh.isaac.solor.direct.cvx;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Status;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.solor.direct.generic.GenericImporter;
import sh.isaac.solor.direct.generic.artifact.ConceptArtifact;
import sh.isaac.solor.direct.generic.artifact.DescriptionArtifact;
import sh.isaac.solor.direct.generic.artifact.IdentifierArtifact;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * 2019-05-19
 * aks8m - https://github.com/aks8m
 */
public class CVXImporter extends GenericImporter<InputStream> {

    /**
     * CVX Code
     * CVX Short Description
     * Full Vaccine Name
     * Note
     * Vaccine Status
     * internalID
     * nonvaccine
     * update_date
     */

    /**
     * Below are known UMLS equivalencies to be used to try and homogenize CVX into the Solor common model :)
     */
    private final ConceptProxy poliovirusVaccineProduct = new ConceptProxy("Poliovirus vaccine (product)", UUID.fromString("a9e4cb66-0e0d-3aad-b618-cfe53861b81a"));
    private final ConceptProxy livePoliovirusVaccineSubstance = new ConceptProxy("Live Poliovirus vaccine (substance)", UUID.fromString("06dcdcf2-01b7-3b61-8e95-82cfa94ac885"));
    private final ConceptProxy livePoliovirusVaccineProduct = new ConceptProxy("Live poliovirus vaccine (product)", UUID.fromString("7a8c4049-e94b-3439-8a89-995f4f82981b"));
    private final ConceptProxy measlesVaccineSubstance = new ConceptProxy("Measles vaccine (substance)", UUID.fromString("5d0d0f44-647c-33ea-a441-dac7970d56ae"));
    private final ConceptProxy measlesVaccineProduct = new ConceptProxy("Measles vaccine (product)", UUID.fromString("3416deb9-7619-36ab-bc24-56dd9eea5398"));
    private final ConceptProxy rubellaVaccineSubstance = new ConceptProxy("Rubella vaccine (substance)", UUID.fromString("95732f71-6587-3068-942e-b41f80784503"));
    private final ConceptProxy rubellaVaccineProduct = new ConceptProxy("Rubella vaccine (product)", UUID.fromString("9a02a33b-ffd1-39bc-b4fd-79b1f7e26c5a"));
    private final ConceptProxy mumpsLiveVirusVaccineProduct = new ConceptProxy("Mumps live virus vaccine (product)", UUID.fromString("182689d4-ecb8-3c4e-a5cd-8f864a6f63fb"));
    private final ConceptProxy mumpsVaccineSubstance = new ConceptProxy("Mumps vaccine (substance)", UUID.fromString("c32552c1-7733-384f-adfa-4dfc8fed3fee"));
    private final ConceptProxy pertussisVaccineSubstance = new ConceptProxy("Pertussis vaccine (substance)", UUID.fromString("d6e1c4e9-c6ba-3e59-9866-a5d5f0e3f911"));
    private final ConceptProxy pertussisVaccineProduct = new ConceptProxy("Pertussis vaccine (product)", UUID.fromString("66bb12ff-5ed8-38e7-ac04-7fe37cb94707"));
    private final ConceptProxy diphtheriaAntitoxinMedicinalProduct = new ConceptProxy("Product containing diphtheria antitoxin (medicinal product)", UUID.fromString("6f38ed23-b798-3c53-b21b-ac2454ae6296"));
    private final ConceptProxy diphtheriaAntitoxinSubstance = new ConceptProxy("Diphtheria antitoxin (substance)", UUID.fromString("80d95240-7fa3-381f-bf98-e6f716bbf78e"));
    private final ConceptProxy tetanusImmunoglobulinSubstance = new ConceptProxy("Tetanus immunoglobulin (substance)", UUID.fromString("4adce2c9-0b14-3083-b412-ce8ecc4428be"));
    private final ConceptProxy tetanusImmunoglobulinMedicinalProduct = new ConceptProxy("Product containing tetanus immunoglobulin of human origin (medicinal product)", UUID.fromString("fcf8caab-85fa-3384-9a27-e9c55a7fe7f8"));
    private final ConceptProxy haemophilusInfluenzaeTypeBProduct = new ConceptProxy("Haemophilus influenzae Type b vaccine (product)", UUID.fromString("5aeb1c37-bba9-30a6-824e-a504c12f2401"));
    private final ConceptProxy haemophilusInfluenzaeTypeBSubstance = new ConceptProxy("Haemophilus influenzae type b vaccine (substance)", UUID.fromString("031686b0-d142-31ba-b884-76fcefd2f8c8"));
    private final ConceptProxy varicellaVirusVaccineSubstance = new ConceptProxy("Varicella virus vaccine (substance)", UUID.fromString("41053779-657f-39b4-9732-4b8b013e0079"));
    private final ConceptProxy varicellaVirusVaccineProduct = new ConceptProxy("Varicella virus vaccine (product)", UUID.fromString("e23a57fd-fff8-3071-8b37-ea8eb2cc2690"));
    private final ConceptProxy diphtheriaPertussisTetanusHaemophilusProduct = new ConceptProxy("Diphtheria + pertussis + tetanus + Haemophilus influenzae type b vaccine (product)", UUID.fromString("e405b09b-07d6-3c96-880e-cbfbf8bd9f5f"));
    private final ConceptProxy haemophilusBPolysaccharideProduct = new ConceptProxy("Haemophilus b polysaccharide conjugate (diphtheria toxoid conjugate) vaccine (product)", UUID.fromString("4f84663f-b75c-3176-980b-383970cc0977"));
    private final ConceptProxy plagueVaccineSubstance = new ConceptProxy("Plague vaccine (substance)", UUID.fromString("7e9c917b-e61a-3a5d-8bc4-79415a9cbf42"));
    private final ConceptProxy plagueVaccineProduct = new ConceptProxy("Plague vaccine (product)", UUID.fromString("c1281f11-b016-39f8-a115-447374f25b47"));
    private final ConceptProxy anthraxVaccineSubstance = new ConceptProxy("Anthrax vaccine (substance)", UUID.fromString("6c2d1e28-49f2-355d-b0fc-d14ada8bda90"));
    private final ConceptProxy anthraxVaccineProduct = new ConceptProxy("Anthrax vaccine (product)", UUID.fromString("267a1da1-8f32-3dc4-ba7e-e5f7bdd2afa9"));
    private final ConceptProxy typhoidLiveOralVaccineProduct = new ConceptProxy("Typhoid live oral vaccine (product)", UUID.fromString("b29925cd-529f-30e6-b013-2dbc2f5fd0a3"));
    private final ConceptProxy choleraVaccineProduct = new ConceptProxy("Cholera vaccine (product)", UUID.fromString("a5467112-bfb8-3135-9b83-d5a518fb650c"));
    private final ConceptProxy choleraVaccineSubstance = new ConceptProxy("Cholera vaccine (substance)", UUID.fromString("ef293148-6f44-36c2-9efe-cd22a8d2ea0f"));
    private final ConceptProxy botulinumAntitoxinSubstance = new ConceptProxy("Botulinum antitoxin (substance)", UUID.fromString("53bdba32-9f16-3fb8-8010-f12bb15e79ec"));
    private final ConceptProxy botulinumAntitoxinMedicinalProduct = new ConceptProxy("Product containing botulinum antitoxin (medicinal product))", UUID.fromString("84dcf3e5-883e-31a7-b148-d96289705eb8"));
    private final ConceptProxy hepatitusBSurfaceAntigenImmunoglobulinMedicinalProduct = new ConceptProxy("Product containing hepatitis B surface antigen immunoglobulin (medicinal product)", UUID.fromString("455ff9e0-4d18-36a8-8647-fcd4033e4e60"));
    private final ConceptProxy meningococcalPolysaccharideVaccineSubstance = new ConceptProxy("Meningococcal polysaccharide vaccine (substance)", UUID.fromString("f946a56b-e744-31c1-82cb-139930f74aa9"));
    private final ConceptProxy rabiesHumanImmuneMedicinalProduct = new ConceptProxy("Product containing rabies human immune globulin (medicinal product)", UUID.fromString("d883bf49-85a7-36c8-9d68-e828ed290a06"));
    private final ConceptProxy rabiseHumanImmuneSubstance = new ConceptProxy("Rabies human immune globulin (substance)", UUID.fromString("bb0f2619-ae1b-38bd-ad59-d4e8c71474c2"));
    private final ConceptProxy varicellaZosterVirusMedicinalProduct = new ConceptProxy("Product containing varicella-zoster virus antibody (medicinal product)", UUID.fromString("14412f9a-9ff5-3505-8cf4-e298d4d1b9aa"));
    private final ConceptProxy rubellaMumpsVaccineSubstance = new ConceptProxy("Rubella and mumps vaccine (substance)", UUID.fromString("04e4c204-1c97-35ff-826c-a47d286e5317"));
    private final ConceptProxy hepatitusBVirusVaccineProduct = new ConceptProxy("Hepatitis B virus vaccine (product)", UUID.fromString("f8558299-db4a-3bbf-8633-d990d76555d4"));
    private final ConceptProxy hepatitusBVirusVaccineSubstance = new ConceptProxy("Hepatitis B virus vaccine (substance)", UUID.fromString("ebc90c3e-364f-3ef9-a2aa-d7446bb0b953"));
    private final ConceptProxy haemophilusInfluenzaeTypeBRecombinantProduct = new ConceptProxy("Haemophilus influenzae Type b + recombinant hepatitis B virus vaccine (product)", UUID.fromString("8dc54dbd-5571-3bd1-80b5-df04b23fe327"));
    private final ConceptProxy liveAdenovirusType5VaccineSubstance = new ConceptProxy("Live adenovirus type 4 vaccine (substance)", UUID.fromString("01915da6-08b7-3efe-a0c5-80d090fc7057"));
    private final ConceptProxy liveAdenovirusType7VaccineSubstance = new ConceptProxy("Live adenovirus type 7 vaccine (substance)", UUID.fromString("cef70865-94d3-3e66-954a-257012a0d171"));
    private final ConceptProxy lymeDiseaseVaccineSubstance = new ConceptProxy("Lyme disease vaccine (substance)", UUID.fromString("050ba87d-403d-38b0-97f0-199160d5a352"));
    private final ConceptProxy lymeDiseaseVaccineProduct = new ConceptProxy("Lyme disease vaccine (product)", UUID.fromString("2a6a20ed-042d-3c38-8604-d561e7569494"));
    private final ConceptProxy melanomaVaccineProduct = new ConceptProxy("Melanoma vaccine (product)", UUID.fromString("90e443b3-83e9-388a-a8fe-7312d9a1812e"));
    private final ConceptProxy melanomaVaccineSubstance = new ConceptProxy("Melanoma vaccine (substance)", UUID.fromString("4b538ba0-0d0f-3047-a3af-bb75ea529c1b"));
    private final ConceptProxy qFeverVaccineSubstance = new ConceptProxy("Q fever vaccine (substance)", UUID.fromString("3da03a40-6601-3265-b988-df34d254062c"));
    private final ConceptProxy qFeverVaccineProduct = new ConceptProxy("Q fever vaccine (product)", UUID.fromString("fd8ab946-852a-37ba-b9fe-8dcf075c5c5c"));
    private final ConceptProxy respiratorySyncytialMedicinalProduct = new ConceptProxy("Product containing respiratory syncytial virus immune globulin (medicinal product)", UUID.fromString("a4dd6559-239f-3ff1-aea4-5db01a76158c"));
    private final ConceptProxy respiratorySyncytialVirusAntibodySubstance = new ConceptProxy("Respiratory syncytial virus antibody (substance)", UUID.fromString("ecbe5518-a6df-3af7-89ac-e11e4db518a4"));
    private final ConceptProxy respiratorySyncytialVirusImmuneSubstance = new ConceptProxy("Respiratory syncytial virus immune globulin (substance)", UUID.fromString("cccf20ad-0fc3-38e3-b765-fcfe10d751a5"));
    private final ConceptProxy vacciniaVirusVaccineSubstance = new ConceptProxy("Vaccinia virus vaccine (substance)", UUID.fromString("578ef08b-581d-3d41-ae50-8db67d8d3b1b"));
    private final ConceptProxy vacciniaVirusVaccineProduct = new ConceptProxy("Vaccinia virus vaccine (product)", UUID.fromString("cb641de7-8e48-32ff-a6f4-7c1585f7490a"));
    private final ConceptProxy tickBorneEncephalitisVaccineProduct = new ConceptProxy("Tick-borne encephalitis vaccine (product)", UUID.fromString("711f26cf-e616-3120-8c03-10c0f6c0f62e"));
    private final ConceptProxy tickBorneEncephalitisVaccineSubstance = new ConceptProxy("Tick-borne encephalitis vaccine (substance)", UUID.fromString("fa0086b9-bc2e-3a5a-b6a0-364ce1c762e8"));
    private final ConceptProxy vacciniaHumanImmuneGlobulinMedicinalProduct = new ConceptProxy("Product containing Vaccinia human immune globulin (medicinal product)", UUID.fromString("49026756-712d-341c-aafe-fc9dcb6ee2f0"));
    private final ConceptProxy vacciniaHumanImmuneGlobulinSubstance = new ConceptProxy("Vaccinia human immune globulin (substance)", UUID.fromString("b1b26f55-9d25-30f5-8521-9e3613acbefd"));
    private final ConceptProxy hepatitisAVirusVaccineSubstance = new ConceptProxy("Hepatitis A virus vaccine (substance)", UUID.fromString("8bf590be-a85a-379c-a6aa-7fbf642d1f9c"));
    private final ConceptProxy hepatitisAVirusVaccineProduct = new ConceptProxy("Hepatitis A virus vaccine (product)", UUID.fromString("fdcb5eb9-fea3-32d9-bea1-274919ed5c32"));
    private final ConceptProxy influenzaVirusVaccineProduct = new ConceptProxy("Influenza virus vaccine (product)", UUID.fromString("9a2aa903-a938-362c-8728-16c333596465"));
    private final ConceptProxy influenzaVirusVaccineSubstance = new ConceptProxy("Influenza virus vaccine (substance)", UUID.fromString("89467acb-372e-3d53-b6a3-ddd857a38ded"));
    private final ConceptProxy polioVirusVaccineSubstance = new ConceptProxy("Poliovirus vaccine (substance)", UUID.fromString("9c67e402-8b72-3a14-861c-614cfaa3e5b8"));
    private final ConceptProxy rabiesVaccineSubstance = new ConceptProxy("Rabies vaccine (substance)", UUID.fromString("33b93954-41bd-3b20-aaab-d008b058c60c"));
    private final ConceptProxy rabiesVaccineProduct = new ConceptProxy("Rabies vaccine (product)", UUID.fromString("a047ba4d-b18c-3076-8a6e-08d0f466cc62"));
    private final ConceptProxy typhoidVaccineSubstance = new ConceptProxy("Typhoid vaccine (substance)", UUID.fromString("85f512c0-0915-3b01-8a10-370665d49561"));
    private final ConceptProxy typhoidVaccineProduct = new ConceptProxy("Typhoid vaccine (product)", UUID.fromString("06fab684-c491-3436-a356-86405b96c9ad"));
    private final ConceptProxy measlesMumpsRubellaVaricellaVaccineProduct = new ConceptProxy("Measles + mumps + rubella + varicella vaccine (product)", UUID.fromString("abf8bbed-0e39-30ac-be20-36d1e3e9e617"));
    private final ConceptProxy pneumococcal7ValentVaccineProduct = new ConceptProxy("Pneumococcal 7-valent conjugate vaccine (product)", UUID.fromString("3c67fb6b-89c0-34ff-a501-6337bc8241ed"));
    private final ConceptProxy typhoidVIPolysaccharideSubstance = new ConceptProxy("Typhoid VI polysaccharide vaccine (substance)", UUID.fromString("3ba81d7c-7ad6-34eb-bf73-61144ba69827"));
    private final ConceptProxy typhoidVIPolysaccharideProduct = new ConceptProxy("Typhoid VI polysaccharide vaccine (product)", UUID.fromString("276a5174-1db4-3e37-90d9-c06c6faaa3f6"));
    private final ConceptProxy meningococcalCVaccineProduct = new ConceptProxy("Meningococcal C conjugate vaccine (product)", UUID.fromString("4251295c-2688-3f7b-b50c-c533c50682b7"));
    private final ConceptProxy hepatitisABVaccineProduct = new ConceptProxy("Hepatitis A+B vaccine (product)", UUID.fromString("e2a90118-22f8-3af1-8bec-74acb7bb3004"));
    private final ConceptProxy pneumococcalVaccineProduct = new ConceptProxy("Pneumococcal vaccine (product)", UUID.fromString("c796a2da-686e-3d7f-95d2-18b5ce8d702b"));
    private final ConceptProxy pneumococcalVaccineSubstance = new ConceptProxy("Pneumococcal vaccine (substance)", UUID.fromString("1db45504-6956-3532-8f66-14f77cc82f9f"));
    private final ConceptProxy intranasalInfluenzaLiveVirusVaccineProduct = new ConceptProxy("Intranasal influenza live virus vaccine (product)", UUID.fromString("64a7918a-551e-36b8-bb1f-dd1e73cafa66"));
    private final ConceptProxy tetanusVaccineSubstance = new ConceptProxy("Tetanus vaccine (substance)", UUID.fromString("dd870e32-8c5a-39e0-a2d9-704b92eb0d6d"));
    private final ConceptProxy tetanusVaccineProduct = new ConceptProxy("Tetanus vaccine (product)", UUID.fromString("31532e54-e963-3b42-a344-fac2fec969e4"));
    private final ConceptProxy liveZosterVirusVaccineSubstance = new ConceptProxy("Live Zoster virus vaccine (substance)", UUID.fromString("62269983-e888-3fc4-bd24-66477a150129"));
    private final ConceptProxy rotavirusVaccineSubstance = new ConceptProxy("Rotavirus vaccine (substance)", UUID.fromString("2231c58a-b84b-3d13-ac17-24a8ce6b7223"));
    private final ConceptProxy rotavirusVaccineProduct = new ConceptProxy("Rotavirus vaccine (product)", UUID.fromString("f998a917-5c96-3908-9a42-3b784afdc90e"));
    private final ConceptProxy influenzaAVirusSubtypeH1N1 = new ConceptProxy("Influenza A virus subtype H1N1 monovalent vaccine 0.5mL injection solution (product)", UUID.fromString("d7a5efd4-5e5c-3173-a0ac-e97500c5865c"));
    private final ConceptProxy pneumococcal13ValenConjugateProduct = new ConceptProxy("Pneumococcal 13-valent conjugate vaccine (product)", UUID.fromString("0576469e-cf28-3ef7-aaf4-de37ebf43194"));
    private final ConceptProxy humanPapillomavirusVaccineProduct = new ConceptProxy("Human papillomavirus vaccine (product)", UUID.fromString("75d63b08-50dc-3260-946d-188bd11d840e"));
    private final ConceptProxy humanPapillomavirusVaccineSubstance = new ConceptProxy("Human papillomavirus vaccine (substance)", UUID.fromString("03239c9a-c715-3768-b0ed-47aaa5767df8"));

    private final Semaphore writeSemaphore;
    private final ArrayList<ConceptArtifact> concepts = new ArrayList<>();
    private final ArrayList<IdentifierArtifact> indentiferSemantics = new ArrayList<>();
    private final ArrayList<DescriptionArtifact> descriptions = new ArrayList<>();
    private final ConverterUUID converterUUID = new ConverterUUID(MetaData.CVX_MODULES____SOLOR.getPrimordialUuid(), true);

    public CVXImporter(Semaphore writeSemaphore, int WRITE_PERMITS) {
        super(writeSemaphore, WRITE_PERMITS);
        this.writeSemaphore = writeSemaphore;
    }

    public void runImport(InputStream inputStream){

        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(100)
                .bufferSize(4096)
                .open(inputStream);

        int rowSkip = 1;
        final int startReadingRowNumber = 2;

        for(Row row : workbook.getSheetAt(0)){

            if(rowSkip >= startReadingRowNumber){

                Cell codeCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell shortDescriptionCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell fullVaccineNameCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell noteCell = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell vaccineStatusCell = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell internalIdCell = row.getCell(5, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell nonVacineCell = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Cell updateDate = row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);


                //Create Identifier Semantic or a new Cell
                switch (codeCell.getStringCellValue()){
                        case "02":
                            //poliovirusVaccineProduct
                            //livePoliovirusVaccineSubstance
                            //livePoliovirusVaccineProduct
                            break;
                        case "05":
                            //measlesVaccineSubstance
                            //measlesVaccineProduct
                            break;
                        case "06":
                            //rubellaVaccineSubstance
                            //rubellaVaccineProduct
                            break;
                        case "07":
                            //mumpsLiveVirusVaccineProduct
                            //mumpsVaccineSubstance
                            break;
                        case "11":
                            //pertussisVaccineSubstance
                            //pertussisVaccineProduct
                            break;
                        case "12":
                            //diphtheriaAntitoxinMedicinalProduct
                            //diphtheriaAntitoxinSubstance
                            break;
                        case "13":
                            //tetanusImmunoglobulinSubstance
                            //tetanusImmunoglobulinMedicinalProduct
                            break;
                        case "17":
                            //haemophilusInfluenzaeTypeBProduct
                            //haemophilusInfluenzaeTypeBSubstance
                            break;
                        case "21":
                            //varicellaVirusVaccineSubstance
                            //varicellaVirusVaccineProduct
                            break;
                        case "22":
                            //diphtheriaPertussisTetanusHaemophilusProduct
                            //haemophilusBPolysaccharideProduct
                            break;
                        case "23":
                            //plagueVaccineSubstance
                            //plagueVaccineProduct
                            break;
                        case "24":
                            //anthraxVaccineSubstance
                            //anthraxVaccineProduct
                            break;
                        case "25":
                            //typhoidLiveOralVaccineProduct
                            break;
                        case "26":
                            //choleraVaccineProduct
                            //choleraVaccineSubstance
                            break;
                        case "27":
                            //botulinumAntitoxinSubstance
                            //botulinumAntitoxinMedicinalProduct
                            break;
                        case "30":
                            //hepatitusBSurfaceAntigenImmunoglobulinMedicinalProduct
                            break;
                        case "32":
                            //meningococcalPolysaccharideVaccineSubstance
                            break;
                        case "34":
                            //rabiesHumanImmuneMedicinalProduct
                            //rabiseHumanImmuneSubstance
                            break;
                        case "36":
                            //varicellaZosterVirusMedicinalProduct
                            break;
                        case "38":
                            //rubellaMumpsVaccineSubstance
                            break;
                        case "45":
                            //hepatitusBVirusVaccineProduct
                            //hepatitusBVirusVaccineSubstance
                            break;
                        case "46":
                            //haemophilusBPolysaccharideProduct
                            break;
                        case "51":
                            //haemophilusInfluenzaeTypeBRecombinantProduct
                            break;
                        case "54":
                            //liveAdenovirusType5VaccineSubstance
                            break;
                        case "55":
                            //liveAdenovirusType7VaccineSubstance
                            break;
                        case "66":
                            //lymeDiseaseVaccineSubstance
                            //lymeDiseaseVaccineProduct
                            break;
                        case "68":
                            //melanomaVaccineProduct
                            //melanomaVaccineSubstance
                            break;
                        case "70":
                            //qFeverVaccineSubstance
                            //qFeverVaccineProduct
                            break;
                        case "71":
                            //respiratorySyncytialMedicinalProduct
                            //respiratorySyncytialVirusAntibodySubstance
                            //respiratorySyncytialVirusImmuneSubstance
                            break;
                        case "75":
                            //vacciniaVirusVaccineSubstance
                            //vacciniaVirusVaccineProduct
                            break;
                        case "77":
                            //tickBorneEncephalitisVaccineProduct
                            //tickBorneEncephalitisVaccineSubstance
                            break;
                        case "79":
                            //vacciniaHumanImmuneGlobulinMedicinalProduct
                            //vacciniaHumanImmuneGlobulinSubstance
                            break;
                        case "85":
                            //hepatitisAVirusVaccineSubstance
                            //hepatitisAVirusVaccineProduct
                            break;
                        case "88":
                            //influenzaVirusVaccineProduct
                            //influenzaVirusVaccineSubstance
                            break;
                        case "89":
                            //poliovirusVaccineProduct
                            //polioVirusVaccineSubstance
                            break;
                        case "90":
                            //rabiesVaccineSubstance
                            //rabiesVaccineProduct
                            break;
                        case "91":
                            //typhoidVaccineSubstance
                            //typhoidVaccineProduct
                            break;
                        case "94":
                            //measlesMumpsRubellaVaricellaVaccineProduct
                            break;
                        case "100":
                            //pneumococcal7ValentVaccineProduct
                            break;
                        case "101":
                            //typhoidVIPolysaccharideSubstance
                            //typhoidVIPolysaccharideProduct
                            break;
                        case "103":
                            //meningococcalCVaccineProduct
                            break;
                        case "104":
                            //hepatitisABVaccineProduct
                            break;
                        case "109":
                            //pneumococcalVaccineProduct
                            //pneumococcalVaccineSubstance
                            break;
                        case "111":
                            //intranasalInfluenzaLiveVirusVaccineProduct
                            break;
                        case "112":
                            //tetanusVaccineSubstance
                            //tetanusVaccineProduct
                            break;
                        case "121":
                            //liveZosterVirusVaccineSubstance
                            break;
                        case "122":
                            //rotavirusVaccineSubstance
                            //rotavirusVaccineProduct
                            break;
                        case "126":
                            //influenzaAVirusSubtypeH1N1
                            break;
                        case "127":
                            //influenzaAVirusSubtypeH1N1
                            break;
                        case "133":
                            //pneumococcal13ValenConjugateProduct
                            break;
                        case "137":
                            //humanPapillomavirusVaccineProduct
                            //humanPapillomavirusVaccineSubstance
                            break;
                        default:

                            super.checkForArtifactUniqueness(new ConceptArtifact(
                                    this.converterUUID.createNamespaceUUIDFromString(codeCell.getStringCellValue()),
                                    computeStatus(vaccineStatusCell.getStringCellValue().toUpperCase()),
                                    computeTime(updateDate.getStringCellValue()),
                                    MetaData.USER____SOLOR.getNid(),
                                    MetaData.CVX_MODULES____SOLOR.getNid(),
                                    MetaData.DEVELOPMENT_PATH____SOLOR.getNid(),
                                    MetaData.SOLOR_CONCEPT_ASSEMBLAGE____SOLOR.getNid(),
                                    codeCell.getStringCellValue(),
                                    MetaData.CVX_CODE____SOLOR.getPrimordialUuid(),
                                    MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid(),
                                    MetaData.CVX_DEFINITION_ASSEMBLAGE____SOLOR.getPrimordialUuid()), this.concepts);

                            break;
                    }

                    //Create Description Semantics and Synonyms

                    //Create Dialect Semantics for Description and Synonyms

            }
            rowSkip++;
        }

        //Newly created Concepts

        super.waitForAll();
    }

    private void createIdentifierSemanticToExistingConcept(UUID existingComponent, String cvxCode, String vaccineStatus, String updateTime){

        super.checkForArtifactUniqueness(new IdentifierArtifact(
                this.converterUUID.createNamespaceUUIDFromString(cvxCode),
                computeStatus(vaccineStatus),
                computeTime(updateTime),
                MetaData.USER____SOLOR.getNid(),
                MetaData.CVX_MODULES____SOLOR.getNid(),
                MetaData.DEVELOPMENT_PATH____SOLOR.getNid(),
                existingComponent,
                cvxCode,
                MetaData.CVX_CODE____SOLOR.getPrimordialUuid()), this.indentiferSemantics);
    }

    private void createFQNDescriptionSemantic(UUID referencedComponent, String fullVaccineName, String vaccineStatus, String updateTime){

    }

    private void createSynonymDescriptionSemantic(UUID referencedComponent, String cvxShortDescription, String vaccineStatus, String updateTime){

    }

    private void createDialectSemantic(){

    }

    private Status computeStatus(String vaccineStatus){

        if (vaccineStatus.equals("ACTIVE") || vaccineStatus.equals("INACTIVE")){
            return Status.valueOf(vaccineStatus);
        } else if (vaccineStatus.equals("NEVER ACTIVE")) {
            return Status.INACTIVE;
        } else if (vaccineStatus.equals("NON-US")) {
            return Status.ACTIVE;
        } else if (vaccineStatus.equals("PENDING")) {
            return Status.ACTIVE;
        } else {
            return Status.INACTIVE;
        }
    }

    private long computeTime(String updateTime){

        long time = -1;

        try{
            time = new SimpleDateFormat("d-MMM-yyyy").parse(updateTime).getTime();
        }catch (ParseException pE){
            super.LOG.error(pE);
        }

        return time;
    }

}
