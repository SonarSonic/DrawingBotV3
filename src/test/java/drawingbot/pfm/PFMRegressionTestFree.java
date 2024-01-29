package drawingbot.pfm;

import drawingbot.JUnitDBV3ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import static drawingbot.TestUtils.*;

@RunWith(JUnitDBV3ClassRunner.class)
public class PFMRegressionTestFree {

    @Test
    public void testPathFindingModules() {
        runPFMTests(pfm -> true);
    }
}