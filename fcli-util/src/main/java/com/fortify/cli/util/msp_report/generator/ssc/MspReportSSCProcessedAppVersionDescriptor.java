package com.fortify.cli.util.msp_report.generator.ssc;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public class MspReportSSCProcessedAppVersionDescriptor {
    private final MspReportSSCAppVersionDescriptor appVersionDescriptor;
    private final MspReportProcessingStatus status;
    private final String reason;
    
    public ObjectNode updateReportRecord(ObjectNode objectNode) {
        return appVersionDescriptor.updateReportRecord(objectNode)
                    .put("status", status.name())
                    .put("reson", reason)
                ;
    }
}
