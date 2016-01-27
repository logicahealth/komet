package gov.vha.isaac.ochre.model.logic.definition;

import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by kec on 1/21/16.
 */
public class LogicalExpressionBuilderTest {

    LogicalExpressionBuilderOchreProvider builderProvider = new LogicalExpressionBuilderOchreProvider();

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void testAddToRoot() throws Exception {
        LogicalExpressionBuilder builder = builderProvider.getLogicalExpressionBuilder();
    }
}