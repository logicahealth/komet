package sh.isaac.convert.mojo.rf2Direct.solor;

import sh.isaac.converters.sharedUtils.config.ConfigOptionsDescriptor;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.ConverterOptionParamSuggestedValue;

/**
 * 2019-08-02
 * aks8m - https://github.com/aks8m
 */
public class RF2SolorDirectConfigOptions implements ConfigOptionsDescriptor {


    @Override
    public ConverterOptionParam[] getConfigOptions() {
        return new ConverterOptionParam[] {
                new ConverterOptionParam("Classifiers to process", ContentConverterCreator.CLASSIFIERS_OPTION,
                        "The classifiers to process.  Defaults to 'Snapshot, Full' in pom mode.  Defaults to 'Snapshot' in direct mode",
                        true,
                        true,
                        false,
                        new String[] {"Snapshot"},
                        new ConverterOptionParamSuggestedValue("Snapshot", "Process the Snapshot portion of the RF2 content"),
                        new ConverterOptionParamSuggestedValue("Full", "Process the Full portion of the RF2 content"),
                        new ConverterOptionParamSuggestedValue("Snapshot-Active-Only", "Process the Snapshot portion of the RF2 content, and only maintain the active components")
                )};
    }

    @Override
    public String getName() {
        return "solor-direct-RF2-import";
    }
}
