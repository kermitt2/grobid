package org.grobid.core.utilities;

import com.google.common.collect.Lists;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;

import java.util.List;

/**
 * Created by zholudev on 18/08/15.
 * Utilities to calculate bounding boxes from coordinates
 */
public class BoundingBoxCalculator {
    private static final double EPS_X = 10;
    private static final double EPS_Y = 3;

    public static List<BoundingBox> calculate(List<LayoutToken> tokens) {
        List<BoundingBox> result = Lists.newArrayList();

        if (tokens == null || tokens.isEmpty()) {
            return result;
        }

        BoundingBox firstBox = BoundingBox.fromLayoutToken(tokens.get(0));
        result.add(firstBox);
        BoundingBox lastBox = firstBox;
        for (int i = 1; i < tokens.size(); i++) {
            BoundingBox b = BoundingBox.fromLayoutToken(tokens.get(i));

            if (near(lastBox, b)) {
                result.set(result.size() - 1, result.get(result.size() - 1).boundBox(b));
            } else {
                result.add(b);
            }
            lastBox = b;
        }
        return result;
    }

    private static boolean near(BoundingBox b1, BoundingBox b2) {
        return Math.abs(b1.getY() - b2.getY()) < EPS_Y && Math.abs(b1.getY2() - b2.getY2()) < EPS_Y && b2.getX() - b1.getX2() < EPS_X;
    }

}
