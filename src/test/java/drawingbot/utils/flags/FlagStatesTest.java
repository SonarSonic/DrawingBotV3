package drawingbot.utils.flags;

import junit.framework.TestCase;

public class FlagStatesTest extends TestCase {

    FlagStates flagStates;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        flagStates = new FlagStates(Flags.RENDER_CATEGORY);
    }

    public void testSetFlag(){
        flagStates.setFlag(Flags.CLEAR_DRAWING, true);
        assertTrue(flagStates.getFlag(Flags.CLEAR_DRAWING));
    }

    public void testAnyMatchTrue(){
        flagStates.setFlag(Flags.FORCE_REDRAW, false);
        flagStates.setFlag(Flags.TASK_CHANGED, false);
        flagStates.setFlag(Flags.CLEAR_DRAWING, true);
        assertTrue(flagStates.anyMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.CLEAR_DRAWING));
    }

    public void testAnyMatchFalse(){
        flagStates.setFlag(Flags.FORCE_REDRAW, false);
        flagStates.setFlag(Flags.TASK_CHANGED, false);
        flagStates.setFlag(Flags.CLEAR_DRAWING, false);
        assertFalse(flagStates.anyMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.CLEAR_DRAWING));
    }

    public void testAllMatchTrue(){
        flagStates.setFlag(Flags.FORCE_REDRAW, true);
        flagStates.setFlag(Flags.TASK_CHANGED, true);
        flagStates.setFlag(Flags.CLEAR_DRAWING, true);
        assertTrue(flagStates.allMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.CLEAR_DRAWING));
    }

    public void testAllMatchFalse(){
        flagStates.setFlag(Flags.FORCE_REDRAW, true);
        flagStates.setFlag(Flags.TASK_CHANGED, true);
        flagStates.setFlag(Flags.CLEAR_DRAWING, false);
        assertFalse(flagStates.allMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.CLEAR_DRAWING));
    }

    public void testMarkClear(){
        flagStates.setFlag(Flags.CLEAR_DRAWING, true);
        flagStates.markForClear(Flags.CLEAR_DRAWING);
        flagStates.applyMarkedChanges();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING));
    }

    public void testMarkReset(){
        flagStates.setFlag(Flags.CLEAR_DRAWING, true);
        flagStates.markForReset(Flags.CLEAR_DRAWING);
        flagStates.applyMarkedChanges();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING));
    }

    public void testClear(){
        flagStates.setFlag(Flags.CLEAR_DRAWING, true);
        flagStates.clear();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING));
    }

    public void testReset(){
        flagStates.setFlag(Flags.CLEAR_DRAWING, true);
        flagStates.reset();
        assertFalse(flagStates.getFlag(Flags.CLEAR_DRAWING));
    }

}