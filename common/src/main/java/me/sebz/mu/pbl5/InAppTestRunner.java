package me.sebz.mu.pbl5;

import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import java.util.ArrayList;
import java.util.List;

public class InAppTestRunner {

    // List of test classes to run
    private static final String[] TEST_CLASSES = {
            "me.sebz.mu.pbl5.tests.CreateAdminSuccessTest",
            "me.sebz.mu.pbl5.tests.CreateGroupAdminTest",
            "me.sebz.mu.pbl5.tests.CreateMemberSuccessTest",
            "me.sebz.mu.pbl5.tests.CreatePatientSuccessTest",
            "me.sebz.mu.pbl5.tests.JoinGroupMemberTest",
            "me.sebz.mu.pbl5.tests.JoinGroupPatientTest",
            "me.sebz.mu.pbl5.tests.ListMembersEditProfileTest",
            "me.sebz.mu.pbl5.tests.LoginBadEmailTest",
            "me.sebz.mu.pbl5.tests.LoginBadPasswordTest",
            "me.sebz.mu.pbl5.tests.UploadMemberImageErrorTest",
            "me.sebz.mu.pbl5.tests.MemberUploadImageSuccessTest",
            "me.sebz.mu.pbl5.tests.UploadNoFaceImageTest",
            "me.sebz.mu.pbl5.tests.UploadRecognizedImageTest",
            "me.sebz.mu.pbl5.tests.UploadUnknownImageTest",
            "me.sebz.mu.pbl5.tests.ListMembersAdminDeleteMemberTest",

    };

    public static void runAllTests() {
        new Thread(() -> {
            StringBuilder report = new StringBuilder();
            int passed = 0;
            int failed = 0;
            int total = 0;

            report.append("Starting Tests...\n\n");

            for (String className : TEST_CLASSES) {
                total++;
                report.append(total).append(". ").append(shortName(className)).append(": ");
                try {
                    Class<?> clz = Class.forName(className);
                    Object testInstance = clz.newInstance();
                    java.lang.reflect.Method runTest = clz.getMethod("runTest");

                    // Run the test
                    boolean result = (Boolean) runTest.invoke(testInstance);

                    if (result) {
                        passed++;
                        report.append("PASS\n");
                    } else {
                        failed++;
                        report.append("FAIL\n");
                    }
                } catch (Throwable t) {
                    failed++;
                    report.append("ERROR (").append(t.getMessage()).append(")\n");
                    t.printStackTrace();
                }

                // Wait for logout/cleanup to stabilize on EDT
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }

            report.append("\nSummary: \n");
            report.append("Total: ").append(total).append("\n");
            report.append("Passed: ").append(passed).append("\n");
            report.append("Failed: ").append(failed).append("\n");

            final String finalReport = report.toString();
            Display.getInstance().callSerially(() -> {
                Dialog.show("Test Suite Results", finalReport, "OK", null);
            });

        }).start();
    }

    private static String shortName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf('.');
        return lastDot > -1 ? fullClassName.substring(lastDot + 1) : fullClassName;
    }
}
