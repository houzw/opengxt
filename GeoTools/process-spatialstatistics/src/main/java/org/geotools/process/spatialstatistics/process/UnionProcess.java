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

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.transformation.DifferenceFeatureCollection;
import org.geotools.process.spatialstatistics.transformation.IntersectFeatureCollection;
import org.geotools.process.spatialstatistics.transformation.MergeFeatureCollection;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a features by overlaying the Input Features with the polygons of the difference features.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class UnionProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(UnionProcess.class);

    public UnionProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static SimpleFeatureCollection process(SimpleFeatureCollection inputFeatures,
            SimpleFeatureCollection overlayFeatures, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(UnionProcessFactory.inputFeatures.key, inputFeatures);
        map.put(UnionProcessFactory.overlayFeatures.key, overlayFeatures);

        Process process = new UnionProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);
            return (SimpleFeatureCollection) resultMap.get(UnionProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                UnionProcessFactory.inputFeatures, null);

        SimpleFeatureCollection overlayFeatures = (SimpleFeatureCollection) Params.getValue(input,
                UnionProcessFactory.overlayFeatures, null);
        if (inputFeatures == null || overlayFeatures == null) {
            throw new NullPointerException("inputFeatures, overlayFeatures parameters required");
        }

        // start process
        SimpleFeatureCollection intersect = DataUtilities.simple(new IntersectFeatureCollection(
                inputFeatures, overlayFeatures));

        SimpleFeatureCollection difference1 = DataUtilities.simple(new DifferenceFeatureCollection(
                inputFeatures, overlayFeatures));

        SimpleFeatureCollection difference2 = DataUtilities.simple(new DifferenceFeatureCollection(
                overlayFeatures, inputFeatures));

        SimpleFeatureCollection merge1 = DataUtilities.simple(new MergeFeatureCollection(intersect,
                difference1));

        SimpleFeatureCollection resultFc = DataUtilities.simple(new MergeFeatureCollection(merge1,
                difference2));
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(UnionProcessFactory.RESULT.key, resultFc);
        return resultMap;
    }

}
