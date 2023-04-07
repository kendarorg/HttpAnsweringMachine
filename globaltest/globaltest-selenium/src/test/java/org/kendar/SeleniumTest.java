package org.kendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// [\t ]+(driver.findElement)([\(a-zA-Z0-9\.\":\-\ )]+)(.click\(\))
// doClick(()->$1$1)

public class SeleniumTest extends SeleniumBase {

    @BeforeEach
    public void beforeEach(){
        this.beforeEach(null);
    }

    @AfterEach
    public void afterEach() throws Exception {
        this.afterEach(null);
    }
    @Test
    void googleHack() throws Throwable {
            runHamJar(SeleniumTest.class);
            var driver = SeleniumBase.getDriver();
            GoogleHackSetupTest.setup(driver);
            restart();
            driver = SeleniumBase.getDriver();
            GoogleHackSetupTest.verify(driver);
            close();
    }

    @Test
    void dbRecordingJdbc() throws Throwable {
        runHamJar(SeleniumTest.class);
        var driver = SeleniumBase.getDriver();
        DbRecordingSetupTest.startup(driver);


        //Create recording
        String mainId = DbRecordingSetupTest.startRecording(driver, "Main");
        DbRecordingUiActions.fullNavigation(driver);
        DbRecordingSetupTest.stopAction(driver, mainId);
        //TODODbRecordingSetupTest.analyzeRecording(driver,mainId);


        //Do Ui test
        String uiTestId = DbRecordingPrepareTest.cloneTo(driver, mainId, "UiTest");
        DbRecordingPrepareTest.prepareUiTest(driver, uiTestId);
        DbRecordingSetupTest.startPlaying(driver, uiTestId);
        DbRecordingUiActions.fullNavigation(driver);
        DbRecordingSetupTest.stopAction(driver, uiTestId);

        //Do Gateway null test
        String gatewayTestId = DbRecordingPrepareTest.cloneTo(driver, mainId, "GatewayNullTest");
        DbRecordingPrepareTest.prepareGatewayNullTest(driver, gatewayTestId);
        DbRecordingSetupTest.startNullPlaying(driver, gatewayTestId);
        DbRecordingSetupTest.loadResults(driver, gatewayTestId,true);

        //Do Gateway null test fail
        String gatewayFailTestId = DbRecordingPrepareTest.cloneTo(driver, mainId, "GatewayNullTestFail");
        DbRecordingPrepareTest.prepareGatewayNullTest(driver, gatewayFailTestId);
        DbRecordingPrepareTest.prepareGatewayNullTestFail(driver, gatewayFailTestId);
        DbRecordingSetupTest.startNullPlaying(driver, gatewayFailTestId);
        DbRecordingSetupTest.loadResults(driver, gatewayFailTestId,false);

        //Do Be fake db test
        String dbNullTest = DbRecordingPrepareTest.cloneTo(driver, mainId, "DbNullTest");
        DbRecordingPrepareTest.prepareDbNullTest(driver, dbNullTest);
        DbRecordingSetupTest.initializeNullPlayingDb(driver, dbNullTest);
        DbRecordingSetupTest.startNullPlaying(driver, dbNullTest);
        DbRecordingSetupTest.loadResults(driver, dbNullTest,true);

        close();
    }



    @Test
    void dbRecordingMongo() throws Throwable {
        runHamJar(SeleniumTest.class);
        var driver = SeleniumBase.getDriver();
        MongoRecordingSetupTest.startup(driver);


        //Create recording
        String mainId = MongoRecordingSetupTest.startRecording(driver, "MainMongo");
        DbRecordingUiActions.fullNavigation(driver);
        DbRecordingSetupTest.stopAction(driver, mainId);


        //Do Be fake db test
        String dbNullTest = DbRecordingPrepareTest.cloneTo(driver, mainId, "DbNullTestMongo");
        DbRecordingPrepareTest.prepareDbNullTest(driver, dbNullTest);

        MongoRecordingSetupTest.initializeNullPlayingDb(driver, dbNullTest);

        DbRecordingSetupTest.startNullPlaying(driver, dbNullTest);
        DbRecordingSetupTest.loadResults(driver, dbNullTest,true);

        close();
    }
}
