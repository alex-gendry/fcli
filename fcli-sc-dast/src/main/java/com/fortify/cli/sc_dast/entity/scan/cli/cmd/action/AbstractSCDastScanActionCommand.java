/*******************************************************************************
 * Copyright 2021, 2023 Open Text.
 *
 * The only warranties for products and services of Open Text 
 * and its affiliates and licensors ("Open Text") are as may 
 * be set forth in the express warranty statements accompanying 
 * such products and services. Nothing herein should be construed 
 * as constituting an additional warranty. Open Text shall not be 
 * liable for technical or editorial errors or omissions contained 
 * herein. The information contained herein is subject to change 
 * without notice.
 *******************************************************************************/
package com.fortify.cli.sc_dast.entity.scan.cli.cmd.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.output.cli.cmd.IJsonNodeSupplier;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.sc_dast.entity.scan.cli.cmd.AbstractSCDastScanOutputCommand;
import com.fortify.cli.sc_dast.entity.scan.cli.mixin.SCDastScanResolverMixin;
import com.fortify.cli.sc_dast.entity.scan.helper.SCDastScanDescriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Mixin;

/**
 * Abstract base class for commands that call the SC DAST scan-action endpoint to perform
 * some action on a scan.
 * 
 * TODO Should we provide functionality for waiting until the scan has actually changed state,
 *      or will we rely on a generic 'block' or 'wait' command to wait for scan statuc change?
 * @author rsenden
 *
 */
public abstract class AbstractSCDastScanActionCommand extends AbstractSCDastScanOutputCommand implements IJsonNodeSupplier, IActionCommandResultSupplier {
    @Mixin private SCDastScanResolverMixin.PositionalParameter scanResolver;
    
    @Override
    public final JsonNode getJsonNode() {
        var unirest = getUnirestInstance();
        SCDastScanDescriptor descriptor = scanResolver.getScanDescriptor(unirest);
        ObjectNode body = new ObjectMapper().createObjectNode()
                .put("scanActionType", getAction().name());
        unirest.post("/api/v2/scans/{id}/scan-action")
            .routeParam("id", descriptor.getId())
            .body(body)
            .asString().getBody(); // TODO Does SC DAST return proper HTTP codes if there are any errors, or should we parse the response?
        return descriptor.asJsonNode();
    }
    
    @Override
    public final String getActionCommandResult() {
        return getAction().getActionResult();
    }
    
    @Override
    public boolean isSingular() {
        return true;
    }
    
    protected abstract SCDastScanAction getAction();
    
    @RequiredArgsConstructor
    protected static enum SCDastScanAction {
        PauseScan("PAUSE_REQUESTED"), 
        ResumeScan("RESUME_REQUESTED"), 
        DeleteScan("DELETE_REQUESTED"), 
        ClearTrackedScan("CLEAR_REQUESTED"), 
        RetryImportScanResults("PUBLISH_REQUESTED"), 
        CompleteScan("COMPLETE_REQUESTED"), 
        RetryImportScanFindings("RETRY_IMPORT_FINDINGS_REQUESTED");
        
        @Getter private final String actionResult;
    }
    
    // Old code from SCDastScanActionsHandler; still need to determine how to best
    // add polling functionality, after which we may be able to re-use some of
    // this code.
    /*
    @SneakyThrows
    private void waitWhileScanStatus(int scanId, List<String> waitingStatus, int waitInterval) {
        String scanStatus =  getScanStatus(scanId).get("scanStatusTypeString")
                .toString()
                .replace("\"","");
        int i = 0;

        while (waitingStatus.contains(scanStatus)) {
            System.out.println(i + ") Scan status: "+scanStatus);
            TimeUnit.SECONDS.sleep(waitInterval);
            scanStatus =  getScanStatus(scanId).get("scanStatusTypeString")
                    .toString()
                    .replace("\"","");
            i += 1;
        }

        System.out.println(i + ") Scan status: "+scanStatus);
    }

    @SneakyThrows
    private void waitUntilScanStatus(int scanId, List<String> waitingStatus, int waitInterval) {
        String scanStatus =  getScanStatus(scanId).get("scanStatusTypeString")
                .toString()
                .replace("\"","");
        int i = 0;

        while (! waitingStatus.contains(scanStatus)) {
            System.out.println(i + ") Scan status: "+scanStatus);
            TimeUnit.SECONDS.sleep(waitInterval);
            scanStatus =  getScanStatus(scanId).get("scanStatusTypeString")
                    .toString()
                    .replace("\"","");
            i += 1;
        }

        System.out.println(i + ") Scan status: "+scanStatus);
    }

    public void waitCompletion(int scanId, int waitInterval)
    {
        List<String> waitingStatus = Arrays.asList("Pending", "Queued", "Running");

        waitWhileScanStatus(scanId, waitingStatus, waitInterval);
    }

    public void waitCompletionWithDetails(int scanId, int waitInterval)
    {
        List<String> waitingStatus = Arrays.asList("Pending", "Queued", "Running");

        waitWhileScanStatus(scanId, waitingStatus, waitInterval);
    }

    public void waitPaused(int scanId, int waitInterval)
    {
        List<String> waitingStatus = Arrays.asList("Paused","Unknown");

        waitUntilScanStatus(scanId, waitingStatus, waitInterval);
    }

    public void waitResumed(int scanId, int waitInterval)
    {
        List<String> waitingStatus = Arrays.asList("Running","FailedToResume","FailedToStart","LicenseUnavailable","Unknown");

        waitUntilScanStatus(scanId, waitingStatus, waitInterval);
    }

    public void waitCompleted(int scanId, int waitInterval)
    {
        List<String> waitingStatus = Arrays.asList("Complete","ForcedComplete","LicenseUnavailable","Unknown");

        waitUntilScanStatus(scanId, waitingStatus, waitInterval);
    }
    */
}
