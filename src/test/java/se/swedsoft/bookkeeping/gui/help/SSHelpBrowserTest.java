package se.swedsoft.bookkeeping.gui.help;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SSHelpBrowserTest {

    @Test
    void createsUrlForKnownPageFilename() {
        assertThat(SSHelpBrowser.helpUrl("Invoices.html"))
                .isEqualTo("https://bokfri.viblo.se/help/Invoices.html");
    }

    @Test
    void fallsBackToHelpIndexForUnsafeOrMissingFilename() {
        assertThat(SSHelpBrowser.helpUrl(null)).isEqualTo(SSHelpBrowser.HELP_BASE_URL);
        assertThat(SSHelpBrowser.helpUrl("../index.html")).isEqualTo(SSHelpBrowser.HELP_BASE_URL);
        assertThat(SSHelpBrowser.helpUrl("https://example.com"))
                .isEqualTo(SSHelpBrowser.HELP_BASE_URL);
    }
}
