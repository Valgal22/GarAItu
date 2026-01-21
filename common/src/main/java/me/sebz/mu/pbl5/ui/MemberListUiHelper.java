package me.sebz.mu.pbl5.ui;

import com.codename1.components.ToastBar;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Label;

import java.util.List;
import java.util.Map;

public final class MemberListUiHelper {

    @FunctionalInterface
    public interface MemberRowAdder {
        void addRow(Map<String, Object> member);
    }

    private MemberListUiHelper() {
    }

    public static void renderMembers(Container memberListContainer,
                                     List<Map<String, Object>> list,
                                     MemberRowAdder rowAdder,
                                     String emptyMessage) {
        Display.getInstance().callSerially(() -> {
            memberListContainer.removeAll();
            if (list != null && !list.isEmpty()) {
                for (Map<String, Object> member : list) {
                    rowAdder.addRow(member);
                }
            } else {
                memberListContainer.add(new Label(emptyMessage));
            }
            memberListContainer.revalidate();
        });
    }

    public static void showLoadError(String messagePrefix, String errorMessage) {
        Display.getInstance().callSerially(() ->
                ToastBar.showErrorMessage(messagePrefix + errorMessage)
        );
    }
}
