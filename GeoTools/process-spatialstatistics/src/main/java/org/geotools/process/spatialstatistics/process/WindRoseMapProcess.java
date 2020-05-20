/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.process.spatialstatistics.process;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.transformation.WindroseAnchorFeatureCollection;
import org.geotools.process.spatialstatistics.transformation.WindroseFeatureCollection;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.util.ProgressListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a wind rose map from features.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class WindRoseMapProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(WindRoseMapProcess.class);

    public WindRoseMapProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static SimpleFeatureCollection process(SimpleFeatureCollection inputFeatures,
            String weightField, Geometry center, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(WindRoseMapProcessFactory.inputFeatures.key, inputFeatures);
        map.put(WindRoseMapProcessFactory.weightField.key, weightField);
        map.put(WindRoseMapProcessFactory.center.key, center);

        Process process = new WindRoseMapProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);
            return (SimpleFeatureCollection) resultMap.get(WindRoseMapProcessFactory.windRose.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                WindRoseMapProcessFactory.inputFeatures, null);
        String weightField = (String) Params.getValue(input, WindRoseMapProcessFactory.weightField,
                null);
        Geometry center = (Geometry) Params.getValue(input, WindRoseMapProcessFactory.center, null);
        if (inputFeatures == null) {
            throw new NullPointerException("inputFeatures parameters required");
        }

        // start process
        Point centeroid = null;
        if (center == null) {
            centeroid = new GeometryFactory().createPoint(inputFeatures.getBounds().centre());
        } else {
            centeroid = center.getCentroid();
        }

        SimpleFeatureCollection windRoseFc = new WindroseFeatureCollection(inputFeatures,
                weightField, centeroid);

        SimpleFeatureCollection anchorFc = new WindroseAnchorFeatureCollection(inputFeatures,
                centeroid);
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(WindRoseMapProcessFactory.windRose.key, windRoseFc);
        resultMap.put(WindRoseMapProcessFactory.anchor.key, anchorFc);
        return resultMap;
    }

}
