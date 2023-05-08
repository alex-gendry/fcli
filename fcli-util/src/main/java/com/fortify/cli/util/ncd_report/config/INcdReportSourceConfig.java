package com.fortify.cli.util.ncd_report.config;

import com.fortify.cli.common.report.config.IReportSourceConfig;
import com.fortify.cli.util.ncd_report.collector.NcdReportResultsCollector;

/**
 * Interface to be implemented by source-specific configuration classes
 * that describe a source configuration, providing a single method to
 * retrieve a source-specific {@link Runnable} generator.
 * 
 * @author rsenden
 *
 */
public interface INcdReportSourceConfig extends IReportSourceConfig<NcdReportResultsCollector>, INcdReportRepoSelectorConfig {
}
