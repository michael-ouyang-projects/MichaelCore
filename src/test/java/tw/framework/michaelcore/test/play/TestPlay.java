package tw.framework.michaelcore.test.play;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tw.framework.michaelcore.ioc.Core;
import tw.framework.michaelcore.ioc.CoreContext;

public class TestPlay {

    private static CoreContext coreContext;

    @BeforeAll
    public static void beforeAll() {
        coreContext = Core.start();
    }

    @AfterAll
    public static void afterAll() {
        coreContext.close();
    }

    @Test
    public void testing() {
    }

}
