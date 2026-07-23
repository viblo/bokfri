package se.swedsoft.bookkeeping.gui.help;

import se.swedsoft.bookkeeping.util.BrowserLaunch;

/** Opens Bokfri's online help in the system web browser. */
public final class SSHelpBrowser {

    static final String HELP_BASE_URL = "https://bokfri.viblo.se/help/";

    private SSHelpBrowser() {
    }

    /** Opens the help start page. */
    public static void openHelp() {
        BrowserLaunch.openURL(HELP_BASE_URL);
    }

}
