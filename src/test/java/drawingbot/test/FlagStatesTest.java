package drawingbot.test;

import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import junit.framework.TestCase;

public class FlagStatesTest extends TestCase {

    FlagStates flagStates;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        flagStates = new FlagStates(Flags.RENDER_CATEGORY);
    }

    public void testSetFlag(){
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, true);
        assertTrue(flagStates.getFlag(Flags.CLEAR_DRAWING_JFX));
    }

    public void testAnyMatchTrue(){
        flagStates.setFlag(Flags.FORCE_REDRAW, false);
        flagStates.setFlag(Flags.ACTIVE_TASK_CHANGED, false);
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, true);
        assertTrue(flagStates.anyMatch(Flags.FORCE_REDRAW, Flags.ACTIVE_TASK_CHANGED, Flags.CLEAR_DRAWING_JFX));
    }

    public void testAnyMatchFalse(){
        flagStates.setFlag(Flags.FORCE_REDRAW, false);
        flagStates.setFlag(Flags.ACTIVE_TASK_CHANGED, false);
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, false);
        assertFalse(flagStates.anyMatch(Flags.FORCE_REDRAW, Flags.ACTIVE_TASK_CHANGED, Flags.CLEAR_DRAWING_JFX));
    }

    public void testAllMatchTrue(){
        flagStates.setFlag(Flags.FORCE_REDRAW, true);
        flagStates.setFlag(Flags.ACTIVE_TASK_CHANGED, true);
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, true);
        assertTrue(flagStates.allMatch(Flags.FORCE_REDRAW, Flags.ACTIVE_TASK_CHANGED, Flags.CLEAR_DRAWING_JFX));
    }

    public void testAllMatchFalse(){
        flagStates.setFlag(Flags.FORCE_REDRAW, true);
        flagStates.setFlag(Flags.ACTIVE_TASK_CHANGED, true);
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, false);
        assertFalse(flagStates.allMatch(Flags.FORCE_REDRAW, Flags.ACTIVE_TASK_CHANGED, Flags.CLEAR_DRAWING_JFX));
    }

    public void testMarkClear(){
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, true);
        flagStates.markForClear(Flags.CLEAR_DRAWING_JFX);
        flagStates.applyMarkedChanges();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING_JFX));
    }

    public void testMarkReset(){
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, true);
        flagStates.markForReset(Flags.CLEAR_DRAWING_JFX);
        flagStates.applyMarkedChanges();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING_JFX));
    }

    public void testClear(){
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, true);
        flagStates.clear();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING_JFX));
    }

    public void testReset(){
        flagStates.setFlag(Flags.CLEAR_DRAWING_JFX, true);
        flagStates.reset();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING_JFX));
    }

}