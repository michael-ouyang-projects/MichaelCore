package tw.framework.michaelcore.test.utils;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import tw.framework.michaelcore.ioc.Core;
import tw.framework.michaelcore.ioc.CoreContainer;

public class MichaelcoreExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started;
    private static CoreContainer coreContext;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!started) {
            started = true;
            coreContext = Core.start();
        }
    }

    @Override
    public void close() throws Throwable {
        Core.stop(coreContext);
    }

    public static CoreContainer getCoreContext() {
        return coreContext;
    }

}
