package org.geotools.process.spatialstatistics;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.process.spatialstatistics.enumeration.ThiessenAttributeMode;
import org.geotools.process.spatialstatistics.process.ThiessenPolygonProcess;
import org.geotools.process.spatialstatistics.storage.ShapeExportOperation;
import org.junit.Test;

import java.io.IOException;

/**
 * @author houzhiwei
 * @date 2020/5/18 17:43
 */
public class ThiessenPolygon extends SpatialStatisticsTestCase {

    @Test
    public void test() throws IOException {
        SimpleFeatureSource source = dataStore.getFeatureSource("point");

        SimpleFeatureCollection poligonos = ThiessenPolygonProcess.process(source.getFeatures(),
                ThiessenAttributeMode.All, null, null);
        ShapeExportOperation shapeExportOperation = new ShapeExportOperation();

    }
}
