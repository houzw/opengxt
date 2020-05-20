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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.FeatureTypes;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.gridcoverage.RasterFunctionalSurface;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * Determines the visibility, based on the elevation, of all the points in a straight line on a surface between observer and target points.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class RasterLinearLOSProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(RasterLinearLOSProcess.class);

    static final String VALUE_FIELD = "Visible";

    public RasterLinearLOSProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static SimpleFeatureCollection process(GridCoverage2D inputCoverage,
            Geometry observerPoint, Double observerOffset, Geometry targetPoint,
            Boolean useCurvature, Boolean useRefraction, Double refractionFactor,
            ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(RasterLinearLOSProcessFactory.inputCoverage.key, inputCoverage);
        map.put(RasterLinearLOSProcessFactory.observerPoint.key, observerPoint);
        map.put(RasterLinearLOSProcessFactory.observerOffset.key, observerOffset);
        map.put(RasterLinearLOSProcessFactory.targetPoint.key, targetPoint);
        map.put(RasterLinearLOSProcessFactory.useCurvature.key, useCurvature);
        map.put(RasterLinearLOSProcessFactory.useRefraction.key, useRefraction);
        map.put(RasterLinearLOSProcessFactory.refractionFactor.key, refractionFactor);

        Process process = new RasterLinearLOSProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);

            return (SimpleFeatureCollection) resultMap
                    .get(RasterLinearLOSProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        GridCoverage2D inputCoverage = (GridCoverage2D) Params.getValue(input,
                RasterLinearLOSProcessFactory.inputCoverage, null);
        Geometry observerPoint = (Geometry) Params.getValue(input,
                RasterLinearLOSProcessFactory.observerPoint, null);
        Double observerOffset = (Double) Params.getValue(input,
                RasterLinearLOSProcessFactory.observerOffset,
                RasterLinearLOSProcessFactory.observerOffset.sample);
        Geometry targetPoint = (Geometry) Params.getValue(input,
                RasterLinearLOSProcessFactory.targetPoint, null);
        Boolean useCurvature = (Boolean) Params.getValue(input,
                RasterLinearLOSProcessFactory.useCurvature,
                RasterLinearLOSProcessFactory.useCurvature.sample);
        Boolean useRefraction = (Boolean) Params.getValue(input,
                RasterLinearLOSProcessFactory.useRefraction,
                RasterLinearLOSProcessFactory.useRefraction.sample);
        Double refractionFactor = (Double) Params.getValue(input,
                RasterLinearLOSProcessFactory.refractionFactor,
                RasterLinearLOSProcessFactory.refractionFactor.sample);

        if (inputCoverage == null || observerPoint == null || targetPoint == null) {
            throw new NullPointerException(
                    "inputCoverage, observerPoint, targetPoint parameters required");
        }

        if (observerOffset < 0) {
            throw new NullPointerException("observerOffset parameter must be a positive value");
        }

        // start process
        CoordinateReferenceSystem crs = inputCoverage.getCoordinateReferenceSystem();
        observerPoint = transformGeometry(observerPoint, crs);
        targetPoint = transformGeometry(targetPoint, crs);

        LineSegment segment = new LineSegment(observerPoint.getCoordinate(),
                targetPoint.getCoordinate());
        LineString userLine = segment.toGeometry(observerPoint.getFactory());

        RasterFunctionalSurface process = new RasterFunctionalSurface(inputCoverage);
        LineString los = process.getLineOfSight(userLine, observerOffset, useCurvature,
                useRefraction, refractionFactor);

        // prepare feature type
        SimpleFeatureType featureType = FeatureTypes.getDefaultType("LinearLineOfSight",
                LineString.class, crs);
        featureType = FeatureTypes.add(featureType, VALUE_FIELD, Integer.class, 5);

        // prepare transactional feature store
        ListFeatureCollection resultSfc = new ListFeatureCollection(featureType);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);

        if (los != null) {
            int serialID = 0;

            Map<Integer, Geometry> map = process.mergeLineOfSight(los);
            for (Entry<Integer, Geometry> entry : map.entrySet()) {
                int visible = entry.getKey();
                Geometry lines = entry.getValue();
                for (int idx = 0; idx < lines.getNumGeometries(); idx++) {
                    LineString line = (LineString) lines.getGeometryN(idx);
                    Geometry multiLine = line.getFactory().createMultiLineString(
                            new LineString[] { line });

                    String fid = featureType.getTypeName() + "." + (++serialID);
                    SimpleFeature newFeature = builder.buildFeature(fid);
                    newFeature.setDefaultGeometry(multiLine);
                    newFeature.setAttribute(VALUE_FIELD, visible);
                    resultSfc.add(newFeature);
                }
            }
        }
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(RasterLinearLOSProcessFactory.RESULT.key, resultSfc);
        return resultMap;
    }
}
