package me.sebz.mu.pbl5.ui;

import com.codename1.testing.AbstractTest;

import me.sebz.mu.pbl5.PatientDashboard;

public class PatientDashboardVoiceQueryUiTest extends AbstractTest {

    @Override
    public boolean runTest() throws Exception {
        new PatientDashboard().show();

        clickButtonByName("pat_voice");

        // recordVoice() abre un Dialog con título "Voice Query"
        waitForFormTitle("Voice Query");
        return true;
    }
}
