import org.junit.jupiter.api.BeforeAll;
import org.kendar.ham.GlobalSettings;
import org.kendar.ham.HamException;

public class HamReplayerIT {

    @BeforeAll
    public static void beforeAll() throws HamException {
        GlobalSettings.runHamJar();
    }
}
