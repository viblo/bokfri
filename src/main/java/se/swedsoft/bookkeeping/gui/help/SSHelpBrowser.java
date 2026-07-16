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

    /**
     * Opens a specific help page. This is available for future contextual help
     * buttons; page names correspond to files in website/help.
     *
     * @param pageName help page filename, for example {@code Invoices.html}
     */
    public static void openHelpPage(String pageName) {
        BrowserLaunch.openURL(helpUrl(pageName));
    }

    static String helpUrl(String pageName) {
        if (pageName == null || !pageName.matches("[A-Za-z0-9_-]+\\.html")) {
            return HELP_BASE_URL;
        }
        return HELP_BASE_URL + pageName;
    }
}
