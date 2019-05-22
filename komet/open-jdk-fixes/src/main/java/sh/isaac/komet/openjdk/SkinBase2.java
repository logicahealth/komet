package sh.isaac.komet.openjdk;

import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;

public abstract class SkinBase2<C extends Control> extends SkinBase<C> {
    public SkinBase2(C c) {
        super(c);
    }
}
