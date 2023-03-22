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
        var recordingData = DbRecordingSetupTest.downloadRecording(driver);

        //Do Ui test
        restart();
        String uiTestId = DbRecordingSetupTest.prepareUiTest(driver,recordingData,"UiTest");
        DbRecordingSetupTest.startPlaying(driver,uiTestId);
        DbRecordingUiActions.fullNavigation(driver);
        DbRecordingSetupTest.stopAction(driver, uiTestId);

        //Do Gateway null test
        restart();
        String gatewayTestId = DbRecordingSetupTest.prepareGatewayNullTest(driver,recordingData,"GatewayNullTest");
        DbRecordingSetupTest.startNullPlaying(driver,gatewayTestId);
        DbRecordingSetupTest.loadResults(driver,gatewayTestId);

        //Do Be fake db test
        restart();
        String dbTest = DbRecordingSetupTest.prepareFakeDbTest(driver,recordingData,"DbTest");
        DbRecordingSetupTest.startNullPlaying(driver,dbTest);
        DbRecordingSetupTest.loadResults(driver,dbTest);

        close();
    }
}
