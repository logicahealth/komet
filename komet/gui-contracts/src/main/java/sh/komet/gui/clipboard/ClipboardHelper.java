package sh.komet.gui.clipboard;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.util.UUIDUtil;
import sh.komet.gui.util.FxGet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ClipboardHelper {
    public static void copyToClipboard(Collection<? extends IdentifiedObject> identifiedObjectCollection) {
        final StringBuilder strb = new StringBuilder();
        for (IdentifiedObject identifiedObject: identifiedObjectCollection) {
            strb.append(identifiedObject.getPrimordialUuid().toString()).append("\t");
            strb.append(identifiedObject.toUserString()).append("\n");
        }
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(strb.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    public static void copyToClipboard(CharSequence charSequence) {
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(charSequence.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }


    public static List<UUID> getUuidsFromClipboard() {
        ArrayList<UUID> uuids = new ArrayList<>();
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            String[] clipboardRows = clipboard.getString().split("\\r?\\n");
            if (clipboardRows.length > 0) {
                String[] testColumns = clipboardRows[0].split("\t");
                if (UUIDUtil.isUUID(testColumns[0])) {
                    for (String clipboardRow: clipboardRows) {
                        String[] rowColumns = clipboardRow.split("\t");
                        uuids.add(UUID.fromString(rowColumns[0]));
                    }
                } else {
                    FxGet.statusMessageService().reportStatus("Attempt to paste, but clipboard does not contain UUID: " + clipboardRows[0]);
                }
            } else {
                FxGet.statusMessageService().reportStatus("Attempt to paste, but clipboard is empty.");
            }
        } else {
            FxGet.statusMessageService().reportStatus("Attempt to paste, but no plain text on clipboard.");
        }
        return uuids;
    }
}
