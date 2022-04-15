package zxc.rich.client.features.player;

import zxc.rich.client.features.Feature;
import zxc.rich.client.features.FeatureCategory;
import zxc.rich.client.ui.settings.impl.NumberSetting;

public class ItemScroller extends Feature {

    public static NumberSetting scrollerDelay;

    public ItemScroller() {
        super("ItemScroller", "��������� ������ ������ ������� ��� ������� �� ���� � ���", FeatureCategory.PLAYER);

        scrollerDelay = new NumberSetting("Scroller Delay", 80, 0, 1000, 1, () -> true);
        addSettings(scrollerDelay);

    }
}