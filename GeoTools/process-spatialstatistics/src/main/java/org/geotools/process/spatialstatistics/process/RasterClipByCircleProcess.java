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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.gridcoverage.RasterClipOperation;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * Extracts the subset of a raster based on a defined circle.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class RasterClipByCircleProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(RasterClipByCircleProcess.class);

    public RasterClipByCircleProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static GridCoverage2D process(GridCoverage2D inputCoverage, Geometry center,
            Double radius, Boolean inside, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(RasterClipByCircleProcessFactory.inputCoverage.key, inputCoverage);
        map.put(RasterClipByCircleProcessFactory.center.key, center);
        map.put(RasterClipByCircleProcessFactory.radius.key, radius);
        map.put(RasterClipByCircleProcessFactory.inside.key, inside);

        Process process = new RasterClipByCircleProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);

            return (GridCoverage2D) resultMap.get(RasterClipByCircleProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        GridCoverage2D inputCoverage = (GridCoverage2D) Params.getValue(input,
                RasterClipByCircleProcessFactory.inputCoverage, null);
        Geometry center = (Geometry) Params.getValue(input,
                RasterClipByCircleProcessFactory.center, null);
        Double radius = (Double) Params.getValue(input, RasterClipByCircleProcessFactory.radius,
                null);
        Boolean inside = (Boolean) Params.getValue(input, RasterClipByCircleProcessFactory.inside,
                RasterClipByCircleProcessFactory.inside.sample);
        if (inputCoverage == null || center == null || radius == null || radius <= 0d) {
            throw new NullPointerException("inputCoverage, center parameters required");
        }

        // start process
        CoordinateReferenceSystem tCrs = inputCoverage.getCoordinateReferenceSystem();
        center = transformGeometry(center, tCrs);

        Geometry cropShape = center.buffer(radius, 24);
        if (inside == Boolean.FALSE) {
            ReferencedEnvelope extent = new ReferencedEnvelope(inputCoverage.getEnvelope());
            cropShape = cropShape.getFactory().toGeometry(extent).difference(cropShape);
            cropShape.setUserData(tCrs);
        }

        RasterClipOperation cropOperation = new RasterClipOperation();
        GridCoverage2D cropedCoverage = cropOperation.execute(inputCoverage, cropShape);
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(RasterClipByCircleProcessFactory.RESULT.key, cropedCoverage);
        return resultMap;
    }
}
