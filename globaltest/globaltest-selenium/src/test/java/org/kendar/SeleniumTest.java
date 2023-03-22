package org.kendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SeleniumBase.class})
public class SeleniumTest extends SeleniumBase{



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
    void dbRecording() throws Throwable {
        runHamJar(SeleniumTest.class);
        var driver = SeleniumBase.getDriver();
        DbRecordingSetupTest.startup(driver);


        //Create recording
        String mainId = DbRecordingSetupTest.startRecording(driver,"Main");
        DbRecordingUiActions.fullNavigation(driver);
        DbRecordingSetupTest.stopAction(driver,mainId);
        DbRecordingSetupTest.analyzeRecording(driver,mainId);

        //Do Ui test
        restart();
        String uiTestId = DbRecordingPrepareTest.cloneTo(driver,mainId,"UiTest");
        DbRecordingPrepareTest.prepareUiTest(driver,uiTestId);
        DbRecordingSetupTest.startPlaying(driver,uiTestId);
        DbRecordingUiActions.fullNavigation(driver);
        DbRecordingSetupTest.stopAction(driver, uiTestId);

        //Do Gateway null test
        restart();
        String gatewayTestId = DbRecordingPrepareTest.cloneTo(driver,mainId,"GatewayNullTest");
        DbRecordingPrepareTest.prepareGatewayNullTest(driver,gatewayTestId);
        DbRecordingSetupTest.startNullPlaying(driver,gatewayTestId);
        DbRecordingSetupTest.loadResults(driver,gatewayTestId);

        //Do Be fake db test
        restart();
        String dbNullTest = DbRecordingPrepareTest.cloneTo(driver,mainId,"DbNullTest");
        DbRecordingPrepareTest.prepareDbNullTest(driver,dbNullTest);
        DbRecordingSetupTest.startNullPlaying(driver,dbNullTest);
        DbRecordingSetupTest.loadResults(driver,dbNullTest);

        close();
    }
}
