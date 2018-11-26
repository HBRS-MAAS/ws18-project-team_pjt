package org.team_pjt.doughprep.mas_maas.messages;

import java.util.Vector;

public class KneadingNotification extends GenericGuidMessage {

    public KneadingNotification(Vector<String> guids, String productType) {
        super(guids, productType);
    }
}
