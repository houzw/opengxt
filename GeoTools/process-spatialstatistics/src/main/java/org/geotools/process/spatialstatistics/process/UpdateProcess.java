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
import org.geotools.process.spatialstatistics.core.FeatureTypes;
import org.geotools.process.spatialstatistics.core.FeatureTypes.SimpleShapeType;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.transformation.DifferenceFeatureCollection;
import org.geotools.process.spatialstatistics.transformation.MergeFeatureCollection;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Computes the geometric intersection of the input Features and update Features. The attributes and geometry of the input features are updated by the
 * update features in the output feature class.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class UpdateProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(UpdateProcess.class);

    public UpdateProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static SimpleFeatureCollection process(SimpleFeatureCollection inputFeatures,
            SimpleFeatureCollection updateFeatures, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(UpdateProcessFactory.inputFeatures.key, inputFeatures);
        map.put(UpdateProcessFactory.updateFeatures.key, updateFeatures);

        Process process = new UpdateProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);
            return (SimpleFeatureCollection) resultMap.get(UpdateProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                UpdateProcessFactory.inputFeatures, null);
        SimpleFeatureCollection updateFeatures = (SimpleFeatureCollection) Params.getValue(input,
                UpdateProcessFactory.updateFeatures, null);
        if (inputFeatures == null || updateFeatures == null) {
            throw new NullPointerException("inputFeatures, updateFeatures parameters required");
        }

        // check polygon type
        if (FeatureTypes.getSimpleShapeType(inputFeatures) != SimpleShapeType.POLYGON) {
            throw new ProcessException("inputFeatures must be a polygon features!");
        }

        if (FeatureTypes.getSimpleShapeType(updateFeatures) != SimpleShapeType.POLYGON) {
            throw new ProcessException("updateFeatures must be a polygon features!");
        }

        // start process
        SimpleFeatureCollection diff1 = DataUtilities.simple(new DifferenceFeatureCollection(
                inputFeatures, updateFeatures));

        SimpleFeatureCollection resultFc = DataUtilities.simple(new MergeFeatureCollection(diff1,
                updateFeatures));
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(UpdateProcessFactory.RESULT.key, resultFc);
        return resultMap;
    }
}
