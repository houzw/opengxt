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
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.DataHistogram;
import org.geotools.process.spatialstatistics.core.FormatUtils;
import org.geotools.process.spatialstatistics.core.HistogramGridCoverage;
import org.geotools.process.spatialstatistics.core.HistogramProcessResult;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.gridcoverage.RasterCropOperation;
import org.geotools.process.spatialstatistics.gridcoverage.RasterHelper;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.opengis.util.ProgressListener;

/**
 * Interpolates a surface from points using an inverse distance weighted (IDW) technique.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class HistogramGridCoverageProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(HistogramGridCoverageProcess.class);

    public HistogramGridCoverageProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static HistogramProcessResult process(GridCoverage2D inputCoverage, Geometry cropShape,
            Integer bandIndex, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(HistogramGridCoverageProcessFactory.inputCoverage.key, inputCoverage);
        map.put(HistogramGridCoverageProcessFactory.cropShape.key, cropShape);
        map.put(HistogramGridCoverageProcessFactory.bandIndex.key, bandIndex);

        Process process = new HistogramGridCoverageProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);

            return (HistogramProcessResult) resultMap
                    .get(HistogramGridCoverageProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        GridCoverage2D inputCoverage = (GridCoverage2D) Params.getValue(input,
                HistogramGridCoverageProcessFactory.inputCoverage, null);
        if (inputCoverage == null) {
            throw new NullPointerException("inputCoverage parameters required");
        }

        Geometry cropShape = (Geometry) Params.getValue(input,
                HistogramGridCoverageProcessFactory.cropShape, null);
        Integer bandIndex = (Integer) Params.getValue(input,
                HistogramGridCoverageProcessFactory.bandIndex,
                HistogramGridCoverageProcessFactory.bandIndex.sample);

        // start process
        // 1. crop raster
        GridCoverage2D cropedCoverage = inputCoverage;
        if (cropShape != null) {
            RasterCropOperation cropOperation = new RasterCropOperation();
            cropedCoverage = cropOperation.execute(inputCoverage, cropShape);
        }

        // 2. histogram
        String typeName = inputCoverage.getName().toString();
        HistogramProcessResult result = new HistogramProcessResult(typeName, "Value");
        if (cropedCoverage != null) {
            double noData = RasterHelper.getNoDataValue(cropedCoverage);

            if (cropShape != null) {
                result.setArea(FormatUtils.format(cropShape.getArea())); // 6 digit
            }

            double cellSize = RasterHelper.getCellSize(cropedCoverage);
            result.setCellSize(FormatUtils.format(cellSize)); // 6 digit

            DataHistogram process = new HistogramGridCoverage();
            process.calculateHistogram(cropedCoverage, bandIndex, noData);
            result.putValues(process.getArrayValues(), process.getArrayFrequencies());
        }
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(HistogramGridCoverageProcessFactory.RESULT.key, result);
        return resultMap;
    }
}
